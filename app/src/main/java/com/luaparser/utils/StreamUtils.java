package com.luaparser.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stream工具类 - 提供跨平台兼容的Stream操作
 */
public class StreamUtils {
    
    /**
     * 将Stream转换为List，兼容Android平台
     */
    public static <T> List<T> toList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }
    
    /**
     * 安全的并行Stream转List
     */
    public static <T> List<T> toListParallel(Stream<T> stream) {
        return stream.parallel().collect(Collectors.toList());
    }
}
