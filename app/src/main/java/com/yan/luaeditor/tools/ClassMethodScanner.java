/**
 * 这个是参考dingyi的androlua with sora写的工具，用于获取最终返回值类型和所有类及其方法
 */

package com.yan.luaeditor.tools;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yan.luaeditor.CompletionName;
import com.yan.luaide.LuaUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import io.github.rosemoe.sora.lang.completion.CompletionItemKind;

public class ClassMethodScanner {

    @RequiresApi(api = Build.VERSION_CODES.P)
    public HashMap<String, HashMap<String, CompletionName>> scanClassesAndMethods(List<String> allClassNames, String dexPath) {
        HashMap<String, HashMap<String, CompletionName>> classInfoMap = new HashMap<>();
        ClassLoader classLoader = getClassLoader(dexPath);

        for (String className : allClassNames) {
            Class<?> clazz = null;
            try {
                clazz = classLoader.loadClass(className);
                HashMap<String, CompletionName> classInfo = new HashMap<>();

                Method[] declaredMethods = clazz.getMethods();
                for (Method method : declaredMethods) {
                    classInfo.put(method.getName(), new CompletionName(method.getReturnType().getName(), CompletionItemKind.Method,":method",getParameterTypesAsString(method)));
                }

                Field[] declaredFields = clazz.getFields();
                for (Field field : declaredFields) {
                    classInfo.put(field.getName(), new CompletionName(field.getType().getName(), CompletionItemKind.Field, " :field",""));
                }

                for (Field field : declaredFields) {
                    String fieldName = field.getName();
                    String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                    String getterMethodName = "get" + capitalizedFieldName;
                    try {
                        Method getterMethod = clazz.getMethod(getterMethodName);
                        classInfo.put(fieldName, new CompletionName(getterMethod.getReturnType().getName(), CompletionItemKind.Property, " :property",getParameterTypesAsString(getterMethod)));
                    } catch (NoSuchMethodException e) {
                        if (field.getType() == boolean.class) {
                            String isGetterMethodName = "is" + capitalizedFieldName;
                            try {
                                Method isGetterMethod = clazz.getMethod(isGetterMethodName);
                                classInfo.put(fieldName, new CompletionName(isGetterMethod.getReturnType().getName(), CompletionItemKind.Property, " :property",getParameterTypesAsString(isGetterMethod)));
                            } catch (NoSuchMethodException ignored) {
                            }
                        }
                    }

                    String setterMethodName = "set" + capitalizedFieldName;
                    try {
                        Method setterMethod = clazz.getMethod(setterMethodName, field.getType());
                        classInfo.put(fieldName, new CompletionName(setterMethod.getReturnType().getName(), CompletionItemKind.Property, " :property",getParameterTypesAsString(setterMethod)));
                    } catch (NoSuchMethodException ignored) {
                    }
                }

                classInfoMap.put(className, classInfo);
            } catch (ClassNotFoundException | NoClassDefFoundError | NoSuchMethodError | IllegalAccessError e) {
                System.err.println("Failed to load class: " + className);
                e.printStackTrace();
            }
        }
        //LuaUtil.save2("/sdcard/Luaide/lll.log",classInfoMap.toString());
        return classInfoMap;
    }

    private ClassLoader getClassLoader(String dexPath) {
        if (dexPath == null || dexPath.isEmpty()) {
            // 使用默认的类加载器
            return getClass().getClassLoader();
        } else {
            // 使用 DexClassLoader 加载外部 DEX 文件
            File optimizedDirectory = new File("/data/data/com.yan.luaide/dex");
            optimizedDirectory.mkdirs();
            return new DexClassLoader(dexPath, optimizedDirectory.getAbsolutePath(), null, getClass().getClassLoader());
        }
    }

    /**
     * 获取方法的参数类型字符串，包含泛型信息，用逗号分隔
     * 示例输出: "List<String>, Map<Integer, String>, String"
     * @param method 要分析的方法对象
     * @return 参数类型字符串
     */
    public static String getParameterTypesAsString(Method method) {
        List<String> parameterTypeStrings = new ArrayList<>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        for (Type genericType : genericParameterTypes) {
            parameterTypeStrings.add(typeToString(genericType));
        }

        return String.join(", ", parameterTypeStrings);
    }

    /**
     * 将Type对象转换为包含泛型信息的字符串
     */
    private static String typeToString(Type type) {
        if (type instanceof Class<?>) {
            return ((Class<?>) type).getName();
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            StringBuilder sb = new StringBuilder();
            sb.append(((Class<?>) pType.getRawType()).getName());

            Type[] typeArgs = pType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                sb.append("<");
                for (int i = 0; i < typeArgs.length; i++) {
                    sb.append(typeToString(typeArgs[i]));
                    if (i < typeArgs.length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(">");
            }

            return sb.toString();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            return typeToString(arrayType.getGenericComponentType()) + "[]";
        } else if (type instanceof TypeVariable<?>) {
            return ((TypeVariable<?>) type).getName();
        } else if (type instanceof WildcardType) {
            return "?"; // 简化处理通配符
        } else {
            return type.toString();
        }
    }

    /**
     *
     *
     * 用于获取最终返回值，这个有性能问题，classinfomap太大，处理起来费劲
     * classinfomap：存储所有类的hashmap
     * inputstring：需要获取最终返回值的语句
     *
     *
     * */
    public static String getReturnType(HashMap<String, List<String>> classMap, HashMap<String, HashMap<String, CompletionName>> classInfoMap, String input, Map<String, String> mMap,HashMap<String,List<String>> importmap) {
        String[] parts = input.split("\\.");
        int start = 0;
        String currentClassName = null;
        String currentMethodName = null;
        boolean isnull=true;
        /*if (importmap!=null){
            for (int i = 1; i <= parts.length; i++) {
                StringBuilder keyBuilder = new StringBuilder(parts[0]);
                for (int j = 1; j < i; j++) {
                    keyBuilder.append(".").append(parts[j]);
                }
                String key = keyBuilder.toString();
                //System.out.println(key);
                if (importmap.get(key)!=null){
                    currentClassName=importmap.get(key).get(0);
                    isnull=false;
                }else {
                    isnull=true;
                }
            }
        }*/
        if (isnull) {
            for (int i = 1; i <= parts.length; i++) {
                StringBuilder keyBuilder = new StringBuilder(parts[0]);
                for (int j = 1; j < i; j++) {
                    keyBuilder.append(".").append(parts[j]);
                }
                String key = keyBuilder.toString();
                //System.out.println(key);
                if (classMap.get(key) != null) {
                    currentClassName = classMap.get(key).get(0);
                    if (key.startsWith("R.")) {
                        currentClassName = "com.yan.luaide." + key;
                    }
                    start = i;
                } else if (mMap.get(key) != null) {
                    try {
                        currentClassName = getReturnType(classMap, classInfoMap, mMap.get(key), mMap, importmap);
                    } catch (Exception e) {
                        //System.out.println(e.getMessage());
                        if (classInfoMap.get(mMap.get(key)) != null) {
                            currentClassName = mMap.get(key);
                        }
                    }
                    start = i;
                } else if (classInfoMap.get(key) != null) {
                    currentClassName = key;
                    start = i;
                }
            }
        }
        //System.out.println(currentClassName);
        for (int i = start; i < parts.length; i++) {
            if (currentClassName != null) {
                HashMap<String, CompletionName> classInfo = classInfoMap.get(currentClassName);
                if (classInfo != null && classInfo.containsKey(parts[i])) {
                    currentMethodName = parts[i];
                    if (parts[i].startsWith("set") && classInfo.get(currentMethodName).getName().equals("void")) {

                    } else {
                        currentClassName = classInfo.get(currentMethodName).getName();
                    }
                } else {
                    return "nullclass";
                }
            } else {
                return "nullclass";
            }
        }

        if (currentMethodName == null) {
            if (currentClassName != null) {
                //System.out.println(currentClassName);
                return currentClassName;
            } else {
                return "nullclass";
            }
        } else {
            //System.out.println(currentClassName);
            return currentClassName;
        }
    }

    public static String getReturnType(String input, HashMap<String, Object> baseMap, HashMap<String, String> importmap, HashMap<String, String> varMap) {
        String[] parts = input.split("\\.");
        int start = 0;
        String currentClassName = "nullclass";
        //String currentMethodName = null;
        HashMap<String,Object> currentMap = null;
        if (importmap!=null&&importmap.containsKey(parts[0])) {
            currentClassName=importmap.get(parts[0]);
        } else if (varMap!=null&&varMap.containsKey(parts[0])) {
            currentClassName=varMap.get(parts[0]);
        }
        if (!currentClassName.equals("nullclass")){
            if (baseMap.get(currentClassName)!=null&&baseMap.get(currentClassName) instanceof HashMap){
                currentMap= (HashMap<String, Object>) baseMap.get(currentClassName);
            }
            for (int i = 1; i < parts.length; i++) {
                if (currentMap.get(parts[i])!=null&&currentMap.get(parts[i]) instanceof HashMap){
                    currentMap= (HashMap<String, Object>) currentMap.get(parts[i]);
                    currentClassName+="$"+parts[i];
                    start=i;
                }
            }
        }
        if (currentClassName!=null){
            for (int i = start+1; i < parts.length; i++) {
                if (currentMap.containsKey(parts[i])){
                    if (!currentClassName.equals("void")) {
                        if (currentMap.get(parts[i]) instanceof CompletionName) {
                            currentClassName = ((CompletionName) getMap(baseMap,currentClassName.split("\\$")).get(parts[i])).getName();
                            currentMap=getMap(baseMap,currentClassName.split("\\$"));
                        }
                    }
                }else {
                    currentClassName="nullclass";
                    break;
                }
            }
        }
        return currentClassName;
    }

    /**
     *
     * @param dexPath
     * @return
     */
    public static List<String> getClassNames(String dexPath) {
        List<String> classNames = new ArrayList<>();
        try {
            DexFile dexFile = new DexFile(dexPath);
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                classNames.add(entries.nextElement());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classNames;
    }
    public static HashMap getMap(HashMap mmap,String[] key){
        HashMap<String,Object> map=mmap;
        for (int i=0;i<key.length;i++){
            if (map.containsKey(key[i])) {
                map = (HashMap<String, Object>) map.get(key[i]);
            }else {
                map.put(key[i],new HashMap<String,Object>());
                map=(HashMap<String, Object>) map.get(key[i]);
            }
        }
        return map;
    }
}