/**
 * 这个是参考dingyi的androlua with sora写的工具，用于获取最终返回值类型和所有类及其方法
 */

package com.yan.luaeditor.tools;


import com.yan.luaeditor.CompletionName;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import io.github.rosemoe.sora.lang.completion.CompletionItemKind;

public class ClassMethodScanner {


    public HashMap<String, HashMap<String, CompletionName>> scanClassesAndMethods(List<String> allClassNames) {
        HashMap<String, HashMap<String, CompletionName>> classInfoMap = new HashMap<>();
        for (String className : allClassNames) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
                HashMap<String, CompletionName> classInfo = new HashMap<>();

                Method[] declaredMethods = clazz.getMethods();
                for (Method method : declaredMethods) {
                    classInfo.put(method.getName(), new CompletionName(method.getReturnType().getName(), CompletionItemKind.Method," :method"));
                }

                Field[] declaredFields = clazz.getFields();
                for (Field field : declaredFields) {
                    classInfo.put(field.getName(), new CompletionName(field.getType().getName(), CompletionItemKind.Field," :field"));
                }

                for (Field field : declaredFields) {
                    String fieldName = field.getName();
                    String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                    String getterMethodName = "get" + capitalizedFieldName;
                    try {
                        Method getterMethod = clazz.getMethod(getterMethodName);
                        classInfo.put(fieldName, new CompletionName(getterMethod.getReturnType().getName(), CompletionItemKind.Property," :property"));
                    } catch (NoSuchMethodException e) {
                        if (field.getType() == boolean.class) {
                            String isGetterMethodName = "is" + capitalizedFieldName;
                            try {
                                Method isGetterMethod = clazz.getMethod(isGetterMethodName);
                                classInfo.put(fieldName, new CompletionName(isGetterMethod.getReturnType().getName(), CompletionItemKind.Property," :property"));
                            } catch (NoSuchMethodException ignored) {
                            }
                        }
                    }

                    String setterMethodName = "set" + capitalizedFieldName;
                    try {
                        Method setterMethod = clazz.getMethod(setterMethodName, field.getType());
                        classInfo.put(fieldName, new CompletionName(setterMethod.getReturnType().getName(), CompletionItemKind.Property," :property"));
                    } catch (NoSuchMethodException ignored) {
                    }
                }

                classInfoMap.put(className.replaceAll("\\$","."), classInfo);
                //System.out.println(className+":"+className.replaceAll("\\$","."));
            }catch (ClassNotFoundException | NoClassDefFoundError|NoSuchMethodError e) {
                System.err.println("Failed to load class: " + className);

            }
        }
        return classInfoMap;
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
    public static String getReturnType(HashMap<String, List<String>> classMap, HashMap<String, HashMap<String, CompletionName>> classInfoMap, String input,Map<String, String> mMap) {
        String[] parts = input.split("\\.");
        int start=0;
        String currentClassName = null;
        String currentMethodName = null;
        for (int i = 1; i <= parts.length; i++) {
            StringBuilder keyBuilder = new StringBuilder(parts[0]);
            for (int j = 1; j < i; j++) {
                keyBuilder.append(".").append(parts[j]);
            }
            String key = keyBuilder.toString();

            if (classMap.get(key) != null) {
                currentClassName = classMap.get(key).get(0);
                if (key.startsWith("R.")) {
                    currentClassName = "com.yan.luaide." + key;
                }
                start = i;
            } else if (mMap.get(key) != null) {
                try {
                    currentClassName = getReturnType(classMap, classInfoMap, mMap.get(key), mMap);
                } catch (Exception e) {
                    //System.out.println(e.getMessage());
                    if (classInfoMap.get(mMap.get(key)) != null) {
                        currentClassName = mMap.get(key);
                    }
                }
                start = i;
            }
        }
        //System.out.println(currentClassName);
        for (int i = start; i < parts.length; i++) {
            if (currentClassName != null) {
                HashMap<String, CompletionName> classInfo = classInfoMap.get(currentClassName);
                if (classInfo != null && classInfo.containsKey(parts[i])) {
                    currentMethodName = parts[i];
                    if (parts[i].startsWith("set")&&classInfo.get(currentMethodName).getName().equals("void")){

                    }else {
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
}