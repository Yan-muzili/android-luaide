package com.luaparser.guide;

import com.luaparser.ast.ASTNode;
import com.luaparser.utils.LineTracker;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * AST导航和分析工具 - 完整对应guide.lua
 */
public class Guide {

    // 块类型集合
    public static final Set<String> BLOCK_TYPES = new HashSet<String>() {{
        add("while"); add("in"); add("loop"); add("repeat"); add("do"); add("function");
        add("if"); add("ifblock"); add("elseblock"); add("elseifblock"); add("main");
    }};

    public static final Set<String> TOP_BLOCK_TYPES = new HashSet<String>() {{
        add("while"); add("function"); add("if"); add("ifblock"); add("elseblock"); add("elseifblock"); add("main");
    }};

    public static final Set<String> BREAK_BLOCK_TYPES = new HashSet<String>() {{
        add("while"); add("in"); add("loop"); add("repeat"); add("for");
    }};

    public static final Set<String> LITERAL_TYPES = new HashSet<String>() {{
        add("nil"); add("boolean"); add("string"); add("number"); add("integer"); add("table"); add("function");
    }};

    public static final Set<String> BASIC_TYPES = new HashSet<String>() {{
        add("unknown"); add("any"); add("true"); add("false"); add("nil"); add("boolean");
        add("integer"); add("number"); add("string"); add("table"); add("function");
        add("thread"); add("userdata");
    }};

    /**
     * 判断是否是字面量
     */
    public boolean isLiteral(ASTNode obj) {
        if (obj == null) return false;
        return LITERAL_TYPES.contains(obj.getType()) || obj.isLiteral();
    }

    /**
     * 获取字面量值
     */
    public Object getLiteral(ASTNode obj) {
        if (isLiteral(obj)) {
            return obj.getLiteralValue();
        }
        return null;
    }

    /**
     * 寻找父函数
     */
    public ASTNode getParentFunction(ASTNode obj) {
        if (obj == null) return null;

        ASTNode current = obj.getParent();
        int depth = 0;

        while (current != null && depth < 1000) { // 防止无限循环
            String type = current.getType();
            if ("function".equals(type) || "main".equals(type)) {
                return current;
            }
            current = current.getParent();
            depth++;
        }

        return null;
    }

    /**
     * 寻找所在区块
     */
    public ASTNode getBlock(ASTNode obj) {
        if (obj == null) return null;

        ASTNode current = obj;
        int depth = 0;

        while (current != null && depth < 1000) {
            if (BLOCK_TYPES.contains(current.getType())) {
                return current;
            }
            current = current.getParent();
            depth++;
        }

        return null;
    }

    /**
     * 寻找所在父区块
     */
    public ASTNode getParentBlock(ASTNode obj) {
        if (obj == null) return null;

        ASTNode current = obj.getParent();
        int depth = 0;

        while (current != null && depth < 1000) {
            if (BLOCK_TYPES.contains(current.getType())) {
                return current;
            }
            current = current.getParent();
            depth++;
        }

        return null;
    }

    /**
     * 寻找所在可break的父区块
     */
    public ASTNode getBreakBlock(ASTNode obj) {
        if (obj == null) return null;

        ASTNode current = obj.getParent();
        int depth = 0;

        while (current != null && depth < 1000) {
            String type = current.getType();
            if (BREAK_BLOCK_TYPES.contains(type)) {
                return current;
            }
            if ("function".equals(type)) {
                return null; // 函数边界，不能break
            }
            current = current.getParent();
            depth++;
        }

        return null;
    }

    /**
     * 寻找根区块
     */
    public ASTNode getRoot(ASTNode obj) {
        if (obj == null) return null;

        // 检查缓存
        Object cached = obj.getAttribute("_root");
        if (cached instanceof ASTNode) {
            return (ASTNode) cached;
        }

        ASTNode current = obj;
        int depth = 0;

        while (current != null && depth < 1000) {
            if ("main".equals(current.getType())) {
                // 缓存结果
                obj.setAttribute("_root", current);
                return current;
            }

            // 检查当前节点是否有缓存的根
            Object rootCache = current.getAttribute("_root");
            if (rootCache instanceof ASTNode) {
                obj.setAttribute("_root", rootCache);
                return (ASTNode) rootCache;
            }

            ASTNode parent = current.getParent();
            if (parent == null) {
                // 没有父节点，当前节点就是根
                obj.setAttribute("_root", current);
                return current;
            }

            current = parent;
            depth++;
        }

        return obj; // 防止无限循环，返回原节点
    }

    /**
     * 获取URI
     */
    public String getUri(ASTNode obj) {
        if (obj == null) return "";

        Object uri = obj.getAttribute("uri");
        if (uri instanceof String) {
            return (String) uri;
        }

        ASTNode root = getRoot(obj);
        if (root != null) {
            Object rootUri = root.getAttribute("uri");
            return rootUri instanceof String ? (String) rootUri : "";
        }

        return "";
    }

    /**
     * 判断source是否包含position
     */
    public boolean isContain(ASTNode source, int position) {
        if (source == null) return false;

        int[] range = getStartFinish(source);
        if (range == null) return false;

        return range[0] <= position && position <= range[1];
    }

    /**
     * 判断position在source的影响范围内
     */
    public boolean isInRange(ASTNode source, int position) {
        if (source == null) return false;

        int[] range = getRange(source);
        if (range == null) return false;

        return range[0] <= position && position <= range[1];
    }

    /**
     * 获取开始和结束位置
     */
    public int[] getStartFinish(ASTNode source) {
        if (source == null) return null;

        int start = source.getStart();
        int finish = source.getFinish();

        // 检查块结束位置
        if (source.getBfinish() > 0 && source.getBfinish() > finish) {
            finish = source.getBfinish();
        }

        // 如果没有明确的位置，从子节点推断
        if (start == 0 && finish == 0) {
            List<ASTNode> children = source.getChildren();
            if (children.isEmpty()) {
                return null;
            }

            ASTNode first = children.get(0);
            ASTNode last = children.get(children.size() - 1);

            if (first != null && last != null) {
                start = first.getStart();
                finish = last.getFinish();

                // 检查最后一个节点的块结束位置
                if (last.getBfinish() > 0 && last.getBfinish() > finish) {
                    finish = last.getBfinish();
                }
            }
        }

        return start == 0 && finish == 0 ? null : new int[]{start, finish};
    }

    /**
     * 获取范围
     */
    public int[] getRange(ASTNode source) {
        if (source == null) return null;

        // 获取虚拟开始位置
        Object vstart = source.getAttribute("vstart");
        int start = vstart instanceof Integer ? (Integer) vstart : source.getStart();

        // 获取范围结束位置
        int finish = source.getRange() > 0 ? source.getRange() : source.getFinish();

        // 检查块结束位置
        if (source.getBfinish() > 0 && source.getBfinish() > finish) {
            finish = source.getBfinish();
        }

        // 如果没有明确的位置，从子节点推断
        if (start == 0 && finish == 0) {
            List<ASTNode> children = source.getChildren();
            if (children.isEmpty()) {
                return null;
            }

            ASTNode first = children.get(0);
            ASTNode last = children.get(children.size() - 1);

            if (first != null && last != null) {
                Object firstVstart = first.getAttribute("vstart");
                start = firstVstart instanceof Integer ? (Integer) firstVstart : first.getStart();

                int lastRange = last.getRange();
                finish = lastRange > 0 ? lastRange : last.getFinish();

                // 检查最后一个节点的块结束位置
                if (last.getBfinish() > 0 && last.getBfinish() > finish) {
                    finish = last.getBfinish();
                }
            }
        }

        return start == 0 && finish == 0 ? null : new int[]{start, finish};
    }

    /**
     * 遍历所有包含position的source
     */
    public void eachSourceContain(ASTNode ast, int position, Consumer<ASTNode> callback) {
        if (ast == null || callback == null) return;

        List<ASTNode> stack = new ArrayList<>();
        Set<ASTNode> visited = new HashSet<>();

        stack.add(ast);

        while (!stack.isEmpty()) {
            ASTNode obj = stack.remove(stack.size() - 1);

            if (visited.contains(obj)) continue;
            visited.add(obj);

            if (isInRange(obj, position)) {
                if (isContain(obj, position)) {
                    callback.accept(obj);
                }

                // 添加子节点到栈中
                addChildrenToStack(stack, obj);
            }
        }
    }

    /**
     * 遍历所有source
     */
    public void eachSource(ASTNode ast, Consumer<ASTNode> callback) {
        if (ast == null || callback == null) return;

        @SuppressWarnings("unchecked")
        List<ASTNode> cache = (List<ASTNode>) ast.getAttribute("_eachCache");

        if (cache == null) {
            cache = new ArrayList<>();
            Set<ASTNode> visited = new HashSet<>();

            cache.add(ast);
            int index = 0;

            while (index < cache.size()) {
                ASTNode obj = cache.get(index++);

                if (!visited.contains(obj)) {
                    visited.add(obj);
                    addChildrenToList(cache, obj);
                }
            }

            ast.setAttribute("_eachCache", cache);
        }

        for (ASTNode node : cache) {
            callback.accept(node);
        }
    }

    /**
     * 遍历指定类型的source
     */
    public void eachSourceType(ASTNode ast, String type, Consumer<ASTNode> callback) {
        if (ast == null || type == null || callback == null) return;

        @SuppressWarnings("unchecked")
        Map<String, List<ASTNode>> cache = (Map<String, List<ASTNode>>) ast.getAttribute("_typeCache");

        if (cache == null) {
            cache = new HashMap<>();
            final Map<String, List<ASTNode>> finalCache = cache;
            ast.setAttribute("_typeCache", cache);

            eachSource(ast, new Consumer<ASTNode>() {
                @Override
                public void accept(ASTNode source) {
                    String tp = source.getType();
                    if (tp != null) {
                        List<ASTNode> list = finalCache.get(tp);
                        if (list == null) {
                            list = new ArrayList<>();
                            finalCache.put(tp, list);
                        }
                        list.add(source);
                    }
                }
            });
        }

        List<ASTNode> myCache = cache.get(type);
        if (myCache != null) {
            for (ASTNode node : myCache) {
                callback.accept(node);
            }
        }
    }

    /**
     * 查找指定类型的所有节点
     */
    public List<ASTNode> findSourcesByType(ASTNode ast, String type) {
        final List<ASTNode> result = new ArrayList<>();
        eachSourceType(ast, type, new Consumer<ASTNode>() {
            @Override
            public void accept(ASTNode node) {
                result.add(node);
            }
        });
        return result;
    }

    /**
     * 查找满足条件的所有节点
     */
    public List<ASTNode> findSources(ASTNode ast, Predicate<ASTNode> predicate) {
        final List<ASTNode> result = new ArrayList<>();
        if (predicate != null) {
            eachSource(ast, new Consumer<ASTNode>() {
                @Override
                public void accept(ASTNode node) {
                    if (predicate.test(node)) {
                        result.add(node);
                    }
                }
            });
        }
        return result;
    }

    /**
     * 添加子节点到栈中
     */
    private void addChildrenToStack(List<ASTNode> stack, ASTNode obj) {
        if (obj == null) return;

        // 根据节点类型添加特定的子节点
        String type = obj.getType();

        // 添加直接子节点
        List<ASTNode> children = obj.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) { // 逆序添加保持遍历顺序
            ASTNode child = children.get(i);
            if (child != null) {
                stack.add(child);
            }
        }

        // 添加特殊属性节点
        addSpecialNodes(stack, obj);
    }

    /**
     * 添加子节点到列表中
     */
    private void addChildrenToList(List<ASTNode> list, ASTNode obj) {
        if (obj == null) return;

        // 添加直接子节点
        List<ASTNode> children = obj.getChildren();
        for (ASTNode child : children) {
            if (child != null) {
                list.add(child);
            }
        }

        // 添加特殊属性节点
        addSpecialNodes(list, obj);
    }

    /**
     * 添加特殊属性节点
     */
    private void addSpecialNodes(List<ASTNode> list, ASTNode obj) {
        // 添加绑定的节点
        ASTNode node = obj.getNode();
        if (node != null) {
            list.add(node);
        }

        // 添加引用节点
        List<ASTNode> refs = obj.getRef();
        if (refs != null) {
            list.addAll(refs);
        }

        // 根据节点类型添加特定属性节点
        String type = obj.getType();
        switch (type) {
            case "getfield":
            case "setfield":
                addAttributeNode(list, obj, "field");
                addAttributeNode(list, obj, "node");
                break;
            case "getindex":
            case "setindex":
                addAttributeNode(list, obj, "index");
                addAttributeNode(list, obj, "node");
                break;
            case "getmethod":
            case "setmethod":
                addAttributeNode(list, obj, "method");
                addAttributeNode(list, obj, "node");
                break;
            case "call":
                addAttributeNode(list, obj, "node");
                addAttributeNode(list, obj, "args");
                break;
            case "binary":
                addAttributeNode(list, obj, "left");
                addAttributeNode(list, obj, "right");
                break;
            case "unary":
                addAttributeNode(list, obj, "exp");
                break;
            case "function":
                addAttributeNode(list, obj, "args");
                addAttributeNode(list, obj, "returns");
                break;
            case "if":
                addAttributeNode(list, obj, "filter");
                break;
            case "while":
            case "repeat":
                addAttributeNode(list, obj, "filter");
                break;
            case "for":
                addAttributeNode(list, obj, "init");
                addAttributeNode(list, obj, "max");
                addAttributeNode(list, obj, "step");
                break;
            case "in":
                addAttributeNode(list, obj, "exps");
                break;
            case "return":
                addAttributeNode(list, obj, "exps");
                break;
            case "local":
                addAttributeNode(list, obj, "value");
                break;
            case "setlocal":
            case "setglobal":
                addAttributeNode(list, obj, "value");
                break;
        }
    }

    /**
     * 添加属性节点到列表
     */
    private void addAttributeNode(List<ASTNode> list, ASTNode obj, String attrName) {
        Object attr = obj.getAttribute(attrName);
        if (attr instanceof ASTNode) {
            list.add((ASTNode) attr);
        } else if (attr instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> attrList = (List<Object>) attr;
            for (Object item : attrList) {
                if (item instanceof ASTNode) {
                    list.add((ASTNode) item);
                }
            }
        }
    }

    /**
     * 将position拆分成行号与列号
     */
    public LineTracker.Position rowColOf(int position) {
        return LineTracker.rowColOf(position);
    }

    /**
     * 将行列合并为position
     */
    public int positionOf(int row, int col) {
        return LineTracker.positionOf(row, col);
    }

    /**
     * 判断是否是赋值操作
     */
    public boolean isAssign(ASTNode source) {
        if (source == null) return false;

        String tp = source.getType();
        return "setglobal".equals(tp) || "local".equals(tp) || "self".equals(tp) ||
                "setlocal".equals(tp) || "setfield".equals(tp) || "setmethod".equals(tp) ||
                "setindex".equals(tp) || "tablefield".equals(tp) || "tableindex".equals(tp) ||
                "label".equals(tp);
    }

    /**
     * 判断是否是获取操作
     */
    public boolean isGet(ASTNode source) {
        if (source == null) return false;

        String tp = source.getType();
        return "getglobal".equals(tp) || "getlocal".equals(tp) || "getfield".equals(tp) ||
                "getmethod".equals(tp) || "getindex".equals(tp);
    }

    /**
     * 获取键名
     */
    public String getKeyName(ASTNode obj) {
        if (obj == null) return null;

        String tp = obj.getType();
        switch (tp) {
            case "getglobal":
            case "setglobal":
            case "local":
            case "self":
            case "getlocal":
            case "setlocal":
                return obj.getStringAttribute("name");

            case "getfield":
            case "setfield":
            case "tablefield":
                ASTNode field = (ASTNode) obj.getAttribute("field");
                return field != null ? field.getStringAttribute("name") : null;

            case "getmethod":
            case "setmethod":
                ASTNode method = (ASTNode) obj.getAttribute("method");
                return method != null ? method.getStringAttribute("name") : null;

            case "getindex":
            case "setindex":
            case "tableindex":
                return getKeyNameOfLiteral((ASTNode) obj.getAttribute("index"));

            case "field":
            case "method":
                return obj.getStringAttribute("name");

            default:
                return getKeyNameOfLiteral(obj);
        }
    }

    /**
     * 获取字面量的键名
     */
    public String getKeyNameOfLiteral(ASTNode obj) {
        if (obj == null) return null;

        String tp = obj.getType();
        if ("field".equals(tp) || "method".equals(tp) ||
                "string".equals(tp) || "number".equals(tp) ||
                "integer".equals(tp) || "boolean".equals(tp)) {

            Object value = obj.getAttribute("value");
            return value != null ? value.toString() : null;
        }

        return null;
    }

    /**
     * 判断是否是全局变量
     */
    public boolean isGlobal(ASTNode source) {
        if (source == null) return false;

        Object cached = source.getAttribute("_isGlobal");
        if (cached instanceof Boolean) {
            return (Boolean) cached;
        }

        boolean result = false;

        String tag = source.getTag();
        if ("_ENV".equals(tag)) {
            result = false;
        } else {
            String special = source.getSpecial();
            if ("_G".equals(special)) {
                result = true;
            } else {
                String tp = source.getType();
                if ("setglobal".equals(tp) || "getglobal".equals(tp)) {
                    ASTNode node = source.getNode();
                    if (node != null && "_ENV".equals(node.getTag())) {
                        result = true;
                    }
                }
            }
        }

        source.setAttribute("_isGlobal", result);
        return result;
    }

    /**
     * 判断是否是基础类型
     */
    public boolean isBasicType(String str) {
        return str != null && BASIC_TYPES.contains(str);
    }

    /**
     * 判断是否是块类型
     */
    public boolean isBlockType(ASTNode source) {
        return source != null && BLOCK_TYPES.contains(source.getType());
    }

    /**
     * 获取节点的完整路径
     */
    public String getNodePath(ASTNode node) {
        if (node == null) return "";

        List<String> parts = new ArrayList<>();
        ASTNode current = node;

        while (current != null) {
            String name = getKeyName(current);
            if (name != null) {
                parts.add(0, name);
            }
            current = current.getParent();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) sb.append(".");
            sb.append(parts.get(i));
        }
        return sb.toString();
    }

    /**
     * 查找最近的包含指定位置的节点
     */
    public ASTNode findNodeAt(ASTNode ast, int position) {
        if (ast == null) return null;

        final ASTNode[] result = {null};
        final int[] minRange = {Integer.MAX_VALUE};

        eachSourceContain(ast, position, new Consumer<ASTNode>() {
            @Override
            public void accept(ASTNode node) {
                int[] range = getStartFinish(node);
                if (range != null) {
                    int rangeSize = range[1] - range[0];
                    if (rangeSize < minRange[0]) {
                        minRange[0] = rangeSize;
                        result[0] = node;
                    }
                }
            }
        });

        return result[0];
    }

    /**
     * 获取节点的作用域
     */
    public ASTNode getScope(ASTNode node) {
        if (node == null) return null;

        ASTNode current = node;
        while (current != null) {
            String type = current.getType();
            if ("function".equals(type) || "main".equals(type) ||
                    "do".equals(type) || "if".equals(type) ||
                    "while".equals(type) || "for".equals(type) ||
                    "repeat".equals(type)) {
                return current;
            }
            current = current.getParent();
        }

        return null;
    }

    /**
     * 检查两个节点是否在同一作用域
     */
    public boolean inSameScope(ASTNode node1, ASTNode node2) {
        if (node1 == null || node2 == null) return false;

        ASTNode scope1 = getScope(node1);
        ASTNode scope2 = getScope(node2);

        return scope1 != null && scope1 == scope2;
    }
}
