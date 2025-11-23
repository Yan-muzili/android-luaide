package com.yan.luaeditor.vtl;

import java.util.Map;


public class VNodeUtils {

    private static String dump(VNode node, int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        sb.append(node.tag);
        sb.append("[\n");
        for (Map.Entry<String, String> entry : node.attrs.entrySet()) {
            for (int i = 0; i < indent + 1; i++) {
                sb.append("  ");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        for (VNode child : node.children) {
            sb.append(dump(child, indent + 1));
        }
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        sb.append("]\n");

        return sb.toString();
    }

    public static String dump(VNode root) {
        return dump(root, 0);
    }
}