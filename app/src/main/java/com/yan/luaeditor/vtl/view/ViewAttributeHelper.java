package com.yan.luaeditor.vtl.view;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 视图属性处理助手类，整合所有处理器
 */
public class ViewAttributeHelper {
    // 所有属性处理器
    private static final List<ViewAttributeProcessor> processors = new ArrayList<>();

    static {
        // 初始化所有处理器
        processors.add(new BaseViewAttributeProcessor());
        processors.add(new LayoutAttributeProcessor());
        processors.add(new AndroidXAttributeProcessor());
        processors.add(new MaterialAttributeProcessor());
    }

    /**
     * 为视图应用属性
     * @param view 目标视图
     * @param attributeName 属性名称
     * @param attributeValue 属性值
     */
    public static void applyAttribute(View view, String attributeName, String attributeValue) {
        if (view == null || attributeName == null || attributeValue == null) {
            return;
        }

        // 尝试用所有处理器处理属性
        for (ViewAttributeProcessor processor : processors) {
            if (processor.processAttribute(view, attributeName, attributeValue)) {
                return; // 找到合适的处理器并处理后返回
            }
        }

        // 如果没有处理器能处理该属性，输出警告
        Log.w("ViewAttributeHelper", "No processor found for attribute: " + attributeName +
                " on view: " + view.getClass().getSimpleName());
    }

    /**
     * 为视图应用多个属性
     * @param view 目标视图
     * @param attributes 属性键值对
     */
    public static void applyAttributes(View view, Map<String, String> attributes) {
        if (view == null || attributes == null || attributes.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            applyAttribute(view, entry.getKey(), entry.getValue());
        }
    }

    /**
     * 注册自定义处理器
     * @param processor 自定义处理器
     */
    public static void registerProcessor(ViewAttributeProcessor processor) {
        if (processor != null && !processors.contains(processor)) {
            processors.add(0, processor); // 添加到前面，优先使用自定义处理器
        }
    }
}
