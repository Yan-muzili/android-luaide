package com.yan.luaide;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.luajava.LuaException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import dalvik.system.DexClassLoader;

public class LuaDexLoader {
    private static HashMap<String, LuaDexClassLoader> dexCache = new HashMap<String, LuaDexClassLoader>();
    private ArrayList<ClassLoader> dexList = new ArrayList<ClassLoader>();
    private HashMap<String, String> libCache = new HashMap<String, String>();

    private LuaContext mContext;

    private String luaDir;

    private AssetManager mAssetManager;

    private LuaResources mResources;
    private Resources.Theme mTheme;
    private String odexDir;

    public LuaDexLoader(LuaContext context) {
        mContext = context;
        luaDir = context.getLuaDir();
        LuaApplication app = LuaApplication.getInstance();
        odexDir = app.getOdexDir();
    }

    public Resources.Theme getTheme() {
        return mTheme;
    }

    public ArrayList<ClassLoader> getClassLoaders() {
        return dexList;
    }

    public LuaDexClassLoader loadApp(String pkg) {
        try {
            LuaDexClassLoader dex = dexCache.get(pkg);
            if (dex == null) {
                PackageManager manager = mContext.getContext().getPackageManager();
                ApplicationInfo info = manager.getPackageInfo(pkg, 0).applicationInfo;
                dex = new LuaDexClassLoader(info.publicSourceDir, LuaApplication.getInstance().getOdexDir(), info.nativeLibraryDir, mContext.getContext().getClassLoader());
                dexCache.put(pkg, dex);
            }
            if (!dexList.contains(dex)) {
                dexList.add(dex);
            }
            return dex;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadLibs() throws LuaException {
        File[] libs = new File(mContext.getLuaDir() + "/libs").listFiles();
        if (libs == null)
            return;
        for (File f : libs) {
            if (f.isDirectory())
                continue;
            if (f.getAbsolutePath().endsWith(".so"))
                loadLib(f.getName());
            else
                loadDex(f.getAbsolutePath());
        }
    }

    public void loadLib(String name) throws LuaException {
        String fn = name;
        int i = name.indexOf(".");
        if (i > 0)
            fn = name.substring(0, i);
        if (fn.startsWith("lib"))
            fn = fn.substring(3);
        String libDir = mContext.getContext().getDir(fn, Context.MODE_PRIVATE).getAbsolutePath();
        String libPath = libDir + "/lib" + fn + ".so";
        File f = new File(libPath);
        if (!f.exists()) {
            f = new File(luaDir + "/libs/lib" + fn + ".so");
            if (!f.exists())
                throw new LuaException("can not find lib " + name);
            LuaUtil.copyFile(luaDir + "/libs/lib" + fn + ".so", libPath);
        }
        libCache.put(fn, libPath);
    }

    public HashMap<String, String> getLibrarys() {
        return libCache;
    }

    public DexClassLoader loadDex(String path) throws LuaException {
        LuaDexClassLoader dex = dexCache.get(path);
        if (dex == null) {
            dex = loadApp(path);
        }
        if (dex == null) {
            String name = path;
            if (path.charAt(0) != '/')
                path = luaDir + "/" + path;
            File dexFile = new File(path);
            if (!dexFile.exists()) {
                if (new File(path + ".dex").exists())
                    path += ".dex";
                else if (new File(path + ".jar").exists())
                    path += ".jar";
                else
                    throw new LuaException(path + " not found");
                dexFile = new File(path);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setFilePermission(dexFile.getAbsolutePath());
            }
            if (!dexFile.setReadOnly()) {
                throw new LuaException("Failed to set dex file read-only: " + path);
            }
            String id = LuaUtil.getFileMD5(path);
            if (id != null && id.equals("0"))
                id = name;
            dex = dexCache.get(id);

            if (dex == null) {
                dex = new LuaDexClassLoader(path, odexDir, LuaApplication.getInstance().getApplicationInfo().nativeLibraryDir, mContext.getContext().getClassLoader());
                dexCache.put(id, dex);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setFilePermission(dex.getDexPath());
        }
        if (!new File(dex.getDexPath()).setReadOnly()) {
            throw new LuaException("Failed to set dex file read-only: " + path);
        }
        if (!dexList.contains(dex)) {
            dexList.add(dex);
            path = dex.getDexPath();
            if (path.endsWith(".jar"))
                loadResources(path);
        }
        return dex;
    }

    public void loadResources(String path) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            int ok = (int) addAssetPath.invoke(assetManager, path);
            if (ok == 0)
                return;
            mAssetManager = assetManager;
            Resources superRes = mContext.getContext().getResources();
            mResources = new LuaResources(mAssetManager, superRes.getDisplayMetrics(),
                    superRes.getConfiguration());
            mResources.setSuperResources(superRes);
            mTheme = mResources.newTheme();
            mTheme.setTo(mContext.getContext().getTheme());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AssetManager getAssets() {
        return mAssetManager;
    }

    public Resources getResources() {
        return mResources;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setFilePermission(String fpath){
        Path path = Paths.get(fpath);
        try {
            // 获取当前文件的权限
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
            // 移除写权限
            permissions.remove(PosixFilePermission.OWNER_WRITE);
            permissions.remove(PosixFilePermission.GROUP_WRITE);
            permissions.remove(PosixFilePermission.OTHERS_WRITE);
            // 设置新的权限
            Files.setPosixFilePermissions(path, permissions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}