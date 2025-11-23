package com.yan.luaeditor.vtl.view;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * 视图属性处理器接口，所有特定控件的处理器都需实现此接口
 */
public interface ViewAttributeProcessor {
    /**
     * 处理视图属性
     * @param view 要处理的视图
     * @param attributeName 属性名称
     * @param attributeValue 属性值
     * @return 是否成功处理该属性
     */
    boolean processAttribute(View view, String attributeName, String attributeValue);

    /**
     * 获取视图上下文
     */
    default Context getContext(View view) {
        return view != null ? view.getContext() : null;
    }


    /**
     * 日志警告
     */
    default void logWarning(String message) {
        Log.w("ViewAttributeProcessor", message);
    }
}
