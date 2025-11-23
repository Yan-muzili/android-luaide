package com.luaparser.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Android兼容性工具类
 * 处理Android平台上Java API的兼容性问题
 */
public class AndroidCompatibility {
    
    /**
     * 兼容的toList方法
     * 在Android上使用collect(Collectors.toList())替代toList()
     */
    public static <T> List<T> toList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }
    
    /**
     * 检查当前是否运行在Android平台
     */
    public static boolean isAndroid() {
        try {
            Class.forName("android.os.Build");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 获取Android API级别（如果在Android上运行）
     */
    public static int getAndroidApiLevel() {
        if (!isAndroid()) {
            return -1;
        }
        
        try {
            Class<?> buildVersion = Class.forName("android.os.Build$VERSION");
            return buildVersion.getField("SDK_INT").getInt(null);
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * 检查是否支持Stream.toList()方法
     */
    public static boolean supportsStreamToList() {
        try {
            // 尝试调用toList方法
            Stream.of().toList();
            return true;
        } catch (NoSuchMethodError e) {
            return false;
        }
    }
}
