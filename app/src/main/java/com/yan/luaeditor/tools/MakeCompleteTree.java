package com.yan.luaeditor.tools;

import android.content.Context;
import android.content.res.Resources;

import com.yan.luaide.LuaUtil;
import com.yan.luaide.R;
import dalvik.system.DexFile;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MakeCompleteTree {

    private static JSONObject packages;
    private static final Map<String, List<String>> classMap = new HashMap<>();
    private static final List<String> classNames = new ArrayList<>();
    private static final Set<String> processedClasses = new HashSet<>();
    private static final Set<String> processedMethodPaths = new HashSet<>();
    private static final int MAX_RECURSION_DEPTH = 4;

    public static JSONObject scanClassesAndMethods(Context context) {
        processedClasses.clear();
        processedMethodPaths.clear();
        JSONObject classTree = new JSONObject();
        List<String> allClassNames = new ArrayList<>();

        try {
            loadFromCache(context);
        } catch (Exception e) {
            try {
                loadFromRawResource(context);
            } catch (Exception ex) {
                try {
                    DexFile dexFile = new DexFile(context.getPackageCodePath());
                    Enumeration<String> entries = dexFile.entries();
                    Stream<String> stream = Collections.list(entries).stream();
                    allClassNames = stream.collect(Collectors.toList());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        if (packages != null) {
            allClassNames = buildClassNamesFromPackages(packages, "");
        }

        for (String className : allClassNames) {
            try {
                Class<?> clazz = Class.forName("android.widget.Toast");
                buildClassTree(clazz, classTree, "", 0);
            } catch (ClassNotFoundException | NoClassDefFoundError | JSONException e) {
                System.err.println("Failed to load class: " + className);
                e.printStackTrace();
            }
        }
        LuaUtil.save2("/sdcard/Luaide/yyy.log",classTree.toString());
        return classTree;
    }

    private static void buildClassTree(Class<?> clazz, JSONObject classTree, String currentPath, int depth) throws JSONException {
        String className = clazz.getName();
        if (processedClasses.contains(className) || depth >= MAX_RECURSION_DEPTH) {
            return;
        }
        processedClasses.add(className);

        JSONObject classInfo = new JSONObject();
        Method[] declaredMethods = clazz.getMethods();
        for (Method method : declaredMethods) {
            String methodName = method.getName();
            String methodPath = currentPath + className + "." + methodName;
            if (processedMethodPaths.contains(methodPath)) {
                continue;
            }
            processedMethodPaths.add(methodPath);

            Class<?> returnType = method.getReturnType();
            Object methodResult = processMethodReturnType(returnType, methodPath, methodName, depth + 1);
            classInfo.put(methodName, methodResult);
        }
        classTree.put(className, classInfo);
    }

    private static Object processMethodReturnType(Class<?> returnType, String currentPath, String parentMethodName, int depth) throws JSONException {
        if (returnType == void.class || returnType.isPrimitive() || returnType.isArray() || depth >= MAX_RECURSION_DEPTH) {
            return JSONObject.NULL;
        }
        String className = returnType.getName();

        if (processedClasses.contains(className)) {
            return getSubTree(returnType, currentPath, parentMethodName, depth);
        }
        processedClasses.add(className);

        JSONObject subTree = new JSONObject();
        Method[] methods = returnType.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.equals(parentMethodName)) {
                continue;
            }
            String methodPath = currentPath + "." + methodName;
            if (processedMethodPaths.contains(methodPath)) {
                continue;
            }
            processedMethodPaths.add(methodPath);

            Class<?> subReturnType = method.getReturnType();
            Object subResult = processMethodReturnType(subReturnType, methodPath, methodName, depth + 1);
            subTree.put(methodName, subResult);
        }
        return subTree;
    }

    private static JSONObject getSubTree(Class<?> clazz, String currentPath, String parentMethodName, int depth) throws JSONException {
        JSONObject subTree = new JSONObject();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.equals(parentMethodName)) {
                continue;
            }
            String methodPath = currentPath + "." + methodName;
            if (processedMethodPaths.contains(methodPath)) {
                continue;
            }
            processedMethodPaths.add(methodPath);

            Class<?> returnType = method.getReturnType();
            Object result;
            if (returnType == void.class || returnType.isPrimitive() || returnType.isArray() || depth >= MAX_RECURSION_DEPTH) {
                result = JSONObject.NULL;
            } else {
                result = processMethodReturnType(returnType, methodPath, methodName, depth + 1);
            }
            subTree.put(methodName, result);
        }
        return subTree;
    }

    private static void loadFromCache(Context context) throws FileNotFoundException, JSONException {
        File cacheFile = new File(context.getCacheDir(), "package_cache.json");
        if (!cacheFile.exists()) {
            throw new FileNotFoundException("Cache file not found");
        }
        try (FileReader reader = new FileReader(cacheFile)) {
            StringBuilder jsonText = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                jsonText.append((char) c);
            }
            initializePackages(context, jsonText.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadFromRawResource(Context context) throws IOException, JSONException {
        Resources resources = context.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.android);
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            StringBuilder jsonText = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonText.append(line);
            }
            initializePackages(context, jsonText.toString());

            saveToCache(context, jsonText.toString());
        }
    }

    private static void saveToCache(Context context, String jsonContent) {
        File cacheFile = new File(context.getCacheDir(), "package_cache.json");
        try (FileWriter writer = new FileWriter(cacheFile)) {
            writer.write(jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializePackages(Context context, String jsonContent) throws JSONException {
        packages = new JSONObject(jsonContent);

        try {
            DexFile dexFile = new DexFile(context.getPackageCodePath());
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                String entry = entries.nextElement();
                String[] parts = entry.replace("$", ".").split("\\.");
                JSONObject currentJson = packages;
                for (String part : parts) {
                    if (part.length() > 2) {
                        if (currentJson.has(part)) {
                            currentJson = currentJson.getJSONObject(part);
                        } else {
                            JSONObject newJson = new JSONObject();
                            currentJson.put(part, newJson);
                            currentJson = newJson;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        buildImports(packages, "");
    }

    private static void buildImports(JSONObject json, String pkg) {
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject subJson = json.getJSONObject(key);
                if (Character.isUpperCase(key.charAt(0))) {
                    classMap.computeIfAbsent(key, k -> new ArrayList<>()).add(pkg + key);
                }
                if (subJson.length() == 0) {
                    classNames.add(key);
                } else {
                    buildImports(subJson, pkg + key + ".");
                }
            } catch (JSONException e) {
                // 忽略解析错误
            }
        }
    }

    private static List<String> buildClassNamesFromPackages(JSONObject json, String pkg) {
        List<String> classNames = new ArrayList<>();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject subJson = json.getJSONObject(key);
                if (subJson.length() == 0) {
                    classNames.add(pkg + key);
                } else {
                    classNames.addAll(buildClassNamesFromPackages(subJson, pkg + key + "."));
                }
            } catch (JSONException e) {
                // 忽略解析错误
            }
        }
        return classNames;
    }
}