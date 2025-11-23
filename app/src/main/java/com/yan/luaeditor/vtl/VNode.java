package com.yan.luaeditor.vtl;

import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 树节点
public final class VNode {
    public final String tag;
    public final Map<String, String> attrs; // 属性表
    public final List<VNode> children;      // 有序子节点

    public VNode(String tag) {
        this.tag = tag;
        this.attrs = new ArrayMap<>();
        this.children = new ArrayList<>();
    }
}