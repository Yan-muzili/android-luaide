/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.multidex;

import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageManager.*;
import android.os.*;
import android.util.*;

import com.yan.luaide.LuaActivity;
import com.yan.luaide.LuaApplication;

import dalvik.system.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

/**
 * Monkey patches {@link Context#getClassLoader() the application context class
 * loader} in order to load classes from more than one dex file. The primary
 * {@code classes.dex} must contain the classes necessary for calling this
 * class methods. Secondary dex files named classes2.dex, classes3.dex... found
 * in the application apk will be added to the classloader after first call to
 * {@link #install(Context)}.
 *
 * <p/>
 * This library provides compatibility for platforms with API level 4 through 20. This library does
 * nothing on newer versions of the platform which provide built-in support for secondary dex files.
 */
public final class MultiDex {

    static final String TAG = "MultiDex";

    private static final String SECONDARY_FOLDER_NAME = "secondary-dexes";

    private static final int MAX_SUPPORTED_SDK_VERSION = 20;

    private static final int MIN_SDK_VERSION = 4;

    private static final int VM_WITH_MULTIDEX_VERSION_MAJOR = 2;

    private static final int VM_WITH_MULTIDEX_VERSION_MINOR = 1;

    private static final Set<String> installedApk = new HashSet<String>();

    private static final boolean IS_VM_MULTIDEX_CAPABLE =
            isVMMultidexCapable(System.getProperty("java.vm.version"));

    private MultiDex() {}
	
	public static void installLibs(LuaActivity context) throws IllegalAccessException, IllegalArgumentException, IOException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
		File d=new File(context.getLuaDir() + "/libs");
		String dir=d.getAbsolutePath();
		if (!d.exists())
			return;
		String[] ls=d.list();
		ArrayList<File> arr=new ArrayList<File>();
		for (String s:ls)
		{
			if(s.endsWith(".dex"))
			{
				arr.add(new File(dir+"/"+s));
				Log.i(TAG,"install "+dir+"/"+s);
			}
			else if(s.endsWith(".jar"))
			{
				arr.add(new File(dir+"/"+s));
				context.loadResources(dir+"/"+s);
				Log.i(TAG,"install "+dir+"/"+s);
			}
		}
		if(arr.isEmpty())
			return;
		File odex=new File(((LuaApplication)context.getApplication()).getOdexDir());
		installDexes(context.getClass().getClassLoader(),odex, arr);
	}

	public static void installDexes(ClassLoader loader, File dexDir, List<File> files)
	throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
	InvocationTargetException, NoSuchMethodException, IOException 
	{
		installSecondaryDexes(loader,dexDir,files);
	}
    /**
     * Patches the application context class loader by appending extra dex files
     * loaded from the application apk. This method should be called in the
     * attachBaseContext of your {@link Application}, see
     * {@link MultiDexApplication} for more explanation and an example.
     *
     * @param context application context.
     * @throws RuntimeException if an error occurred preventing the classloader
     *         extension.
     */
    public static void install(Context context) {
        Log.i(TAG, "install");
        if (IS_VM_MULTIDEX_CAPABLE) {
            Log.i(TAG, "VM has multidex support, MultiDex support library is disabled.");
            try {
                clearOldDexDir(context);
            } catch (Throwable t) {
                Log.w(TAG, "Something went wrong when trying to clear old MultiDex extraction, "
                        + "continuing without cleaning.", t);
            }
            return;
        }

        if (Build.VERSION.SDK_INT < MIN_SDK_VERSION) {
            throw new RuntimeException("Multi dex installation failed. SDK " + Build.VERSION.SDK_INT
                    + " is unsupported. Min SDK version is " + MIN_SDK_VERSION + ".");
        }

        try {
            ApplicationInfo applicationInfo = getApplicationInfo(context);
            if (applicationInfo == null) {
                // Looks like running on a test Context, so just return without patching.
                return;
            }

            synchronized (installedApk) {
                String apkPath = applicationInfo.sourceDir;
                if (installedApk.contains(apkPath)) {
                    return;
                }
                installedApk.add(apkPath);

                if (Build.VERSION.SDK_INT > MAX_SUPPORTED_SDK_VERSION) {
                    Log.w(TAG, "MultiDex is not guaranteed to work in SDK version "
                            + Build.VERSION.SDK_INT + ": SDK version higher than "
                            + MAX_SUPPORTED_SDK_VERSION + " should be backed by "
                            + "runtime with built-in multidex capabilty but it's not the "
                            + "case here: java.vm.version=\""
                            + System.getProperty("java.vm.version") + "\"");
                }

                /* The patched class loader is expected to be a descendant of
                 * dalvik.system.BaseDexClassLoader. We modify its
                 * dalvik.system.DexPathList pathList field to append additional DEX
                 * file entries.
                 */
                ClassLoader loader;
                try {
                    loader = context.getClassLoader();
                } catch (RuntimeException e) {
                    /* Ignore those exceptions so that we don't break tests relying on Context like
                     * a android.test.mock.MockContext or a android.content.ContextWrapper with a
                     * null base Context.
                     */
                    Log.w(TAG, "Failure while trying to obtain Context class loader. " +
                            "Must be running in test mode. Skip patching.", e);
                    return;
                }
                if (loader == null) {
                    // Note, the context class loader is null when running Robolectric tests.
                    Log.e(TAG,
                            "Context class loader is null. Must be running in test mode. "
                            + "Skip patching.");
                    return;
                }

                File dexDir = new File(context.getFilesDir(), SECONDARY_FOLDER_NAME);
                List<File> files = MultiDexExtractor.load(context, applicationInfo, dexDir, false);
                if (checkValidZipFiles(files)) {
                    installSecondaryDexes(loader, dexDir, files);
                } else {
                    Log.w(TAG, "Files were not valid zip files.  Forcing a reload.");
                    // Try again, but this time force a reload of the zip file.
                    files = MultiDexExtractor.load(context, applicationInfo, dexDir, true);

                    if (checkValidZipFiles(files)) {
                        installSecondaryDexes(loader, dexDir, files);
                    } else {
                        // Second time didn't work, give up
                        throw new RuntimeException("Zip files were not valid.");
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Multidex installation failure", e);
            throw new RuntimeException("Multi dex installation failed (" + e.getMessage() + ").");
        }
        Log.i(TAG, "install done");
    }

    private static ApplicationInfo getApplicationInfo(Context context)
            throws NameNotFoundException {
        PackageManager pm;
        String packageName;
        try {
            pm = context.getPackageManager();
            packageName = context.getPackageName();
        } catch (RuntimeException e) {
            /* Ignore those exceptions so that we don't break tests relying on Context like
             * a android.test.mock.MockContext or a android.content.ContextWrapper with a null
             * base Context.
             */
            Log.w(TAG, "Failure while trying to obtain ApplicationInfo from Context. " +
                    "Must be running in test mode. Skip patching.", e);
            return null;
        }
        if (pm == null || packageName == null) {
            // This is most likely a mock context, so just return without patching.
            return null;
        }
        ApplicationInfo applicationInfo =
                pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        return applicationInfo;
    }

    /**
     * Identifies if the current VM has a native support for multidex, meaning there is no need for
     * additional installation by this library.
     * @return true if the VM handles multidex
     */
    /* package visible for test */
    static boolean isVMMultidexCapable(String versionString) {
        boolean isMultidexCapable = false;
        if (versionString != null) {
            Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
            if (matcher.matches()) {
                try {
                    int major = Integer.parseInt(matcher.group(1));
                    int minor = Integer.parseInt(matcher.group(2));
                    isMultidexCapable = (major > VM_WITH_MULTIDEX_VERSION_MAJOR)
                            || ((major == VM_WITH_MULTIDEX_VERSION_MAJOR)
                                    && (minor >= VM_WITH_MULTIDEX_VERSION_MINOR));
                } catch (NumberFormatException e) {
                    // let isMultidexCapable be false
                }
            }
        }
        Log.i(TAG, "VM with version " + versionString +
                (isMultidexCapable ?
                        " has multidex support" :
                        " does not have multidex support"));
        return isMultidexCapable;
    }

	
    private static void installSecondaryDexes(ClassLoader loader, File dexDir, List<File> files)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
            InvocationTargetException, NoSuchMethodException, IOException {
        if (!files.isEmpty()) {
            if (Build.VERSION.SDK_INT >= 19) {
                V19.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 14) {
                V14.install(loader, files, dexDir);
            } else {
                V4.install(loader, files);
            }
        }
    }

    /**
     * Returns whether all files in the list are valid zip files.  If {@code files} is empty, then
     * returns true.
     */
    private static boolean checkValidZipFiles(List<File> files) {
        for (File file : files) {
            if (!MultiDexExtractor.verifyZipFile(file)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Locates a given field anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the field into.
     * @param name field name
     * @return a field object
     * @throws NoSuchFieldException if the field cannot be located
     */
    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);


                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    /**
     * Locates a given method anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the method into.
     * @param name method name
     * @param parameterTypes method parameter types
     * @return a method object
     * @throws NoSuchMethodException if the method cannot be located
     */
    private static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);


                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }

        throw new NoSuchMethodException("Method " + name + " with parameters " +
                Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }

    /**
     * Replace the value of a field containing a non null array, by a new array containing the
     * elements of the original array plus the elements of extraElements.
     * @param instance the instance whose field is to be modified.
     * @param fieldName the field to modify.
     * @param extraElements elements to append at the end of the array.
     */
    private static void expandFieldArray(Object instance, String fieldName,
            Object[] extraElements) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field jlrField = findField(instance, fieldName);
        Object[] original = (Object[]) jlrField.get(instance);
        Object[] combined = (Object[]) Array.newInstance(
                original.getClass().getComponentType(), original.length + extraElements.length);
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        jlrField.set(instance, combined);
    }

    private static void clearOldDexDir(Context context) throws Exception {
        ApplicationInfo applicationInfo = getApplicationInfo(context);
        if (applicationInfo == null) {
            // Looks like running on a test Context, so just return.
            return;
        }

        synchronized (installedApk) {
            String apkPath = applicationInfo.sourceDir;
            if (installedApk.contains(apkPath)) {
                return;
            }
            installedApk.add(apkPath);
        }
        File dexDir = new File(context.getFilesDir(), SECONDARY_FOLDER_NAME);
        if (dexDir.isDirectory()) {
            Log.i(TAG, "Clearing old secondary dex dir (" + dexDir.getPath() + ").");
            File[] files = dexDir.listFiles();
            if (files == null) {
                Log.w(TAG, "Failed to list secondary dex dir content (" + dexDir.getPath() + ").");
                return;
            }
            for (File oldFile : files) {
                Log.i(TAG, "Trying to delete old file " + oldFile.getPath() + " of size "
                        + oldFile.length());
                if (!oldFile.delete()) {
                    Log.w(TAG, "Failed to delete old file " + oldFile.getPath());
                } else {
                    Log.i(TAG, "Deleted old file " + oldFile.getPath());
                }
            }
            if (!dexDir.delete()) {
                Log.w(TAG, "Failed to delete secondary dex dir " + dexDir.getPath());
            } else {
                Log.i(TAG, "Deleted old secondary dex dir " + dexDir.getPath());
            }
        }
    }

    /**
     * Installer for platform versions 19.
     */
    private static final class V19 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                File optimizedDirectory)
                        throws IllegalArgumentException, IllegalAccessException,
                        NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<File>(additionalClassPathEntries), optimizedDirectory,
                    suppressedExceptions));
            if (suppressedExceptions.size() > 0) {
                for (IOException e : suppressedExceptions) {
                    Log.w(TAG, "Exception in makeDexElement", e);
                }
                Field suppressedExceptionsField =
                        findField(loader, "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions =
                        (IOException[]) suppressedExceptionsField.get(loader);

                if (dexElementsSuppressedExceptions == null) {
                    dexElementsSuppressedExceptions =
                            suppressedExceptions.toArray(
                                    new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined =
                            new IOException[suppressedExceptions.size() +
                                            dexElementsSuppressedExceptions.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions, 0, combined,
                            suppressedExceptions.size(), dexElementsSuppressedExceptions.length);
                    dexElementsSuppressedExceptions = combined;
                }

                suppressedExceptionsField.set(loader, dexElementsSuppressedExceptions);
            }
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory,
                ArrayList<IOException> suppressedExceptions)
                        throws IllegalAccessException, InvocationTargetException,
                        NoSuchMethodException {
            Method makeDexElements =
                    findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class,
                            ArrayList.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory,
                    suppressedExceptions);
        }
    }

    /**
     * Installer for platform versions 14, 15, 16, 17 and 18.
     */
    private static final class V14 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                File optimizedDirectory)
                        throws IllegalArgumentException, IllegalAccessException,
                        NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<File>(additionalClassPathEntries), optimizedDirectory));
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory)
                        throws IllegalAccessException, InvocationTargetException,
                        NoSuchMethodException {
            Method makeDexElements =
                    findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory);
        }
    }

    /**
     * Installer for platform versions 4 to 13.
     */
    private static final class V4 {
        private static void install(ClassLoader loader, List<File> additionalClassPathEntries)
                        throws IllegalArgumentException, IllegalAccessException,
                        NoSuchFieldException, IOException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.DexClassLoader. We modify its
             * fields mPaths, mFiles, mZips and mDexs to append additional DEX
             * file entries.
             */
            int extraSize = additionalClassPathEntries.size();

            Field pathField = findField(loader, "path");

            StringBuilder path = new StringBuilder((String) pathField.get(loader));
            String[] extraPaths = new String[extraSize];
            File[] extraFiles = new File[extraSize];
            ZipFile[] extraZips = new ZipFile[extraSize];
            DexFile[] extraDexs = new DexFile[extraSize];
            for (ListIterator<File> iterator = additionalClassPathEntries.listIterator();
                    iterator.hasNext();) {
                File additionalEntry = iterator.next();
                String entryPath = additionalEntry.getAbsolutePath();
                path.append(':').append(entryPath);
                int index = iterator.previousIndex();
                extraPaths[index] = entryPath;
                extraFiles[index] = additionalEntry;
                extraZips[index] = new ZipFile(additionalEntry);
                extraDexs[index] = DexFile.loadDex(entryPath, entryPath + ".dex", 0);
            }

            pathField.set(loader, path.toString());
            expandFieldArray(loader, "mPaths", extraPaths);
            expandFieldArray(loader, "mFiles", extraFiles);
            expandFieldArray(loader, "mZips", extraZips);
            expandFieldArray(loader, "mDexs", extraDexs);
        }
    }

}
