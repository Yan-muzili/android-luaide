package com.luaparser.ast;

import java.util.*;

/**
 * AST节点基类 - 完整对应guide.lua中的parser.object
 */
public class ASTNode {
    // 基本属性
    protected String type;
    protected int start;
    protected int finish;
    protected int range;
    protected int effect;
    protected int bstart;  // block start
    protected int bfinish; // block finish
    
    // 树结构
    protected ASTNode parent;
    protected List<ASTNode> children = new ArrayList<>();
    
    // 扩展属性
    protected Map<String, Object> attributes = new HashMap<>();
    
    // 绑定信息
    protected List<ASTNode> bindDocs;
    protected List<ASTNode> bindGroup;
    protected ASTNode bindSource;
    
    // 引用信息
    protected List<ASTNode> ref;
    protected ASTNode node;
    
    // 特殊标记
    protected String special;
    protected String tag;
    protected boolean literal;
    
    // 构造函数
    public ASTNode() {
        this("unknown");
    }
    
    public ASTNode(String type) {
        this.type = type;
    }
    
    public ASTNode(String type, int start, int finish) {
        this.type = type;
        this.start = start;
        this.finish = finish;
    }
    
    // 基本属性访问器
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public int getStart() { return start; }
    public void setStart(int start) { this.start = start; }
    
    public int getFinish() { return finish; }
    public void setFinish(int finish) { this.finish = finish; }
    
    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }
    
    public int getEffect() { return effect; }
    public void setEffect(int effect) { this.effect = effect; }
    
    public int getBstart() { return bstart; }
    public void setBstart(int bstart) { this.bstart = bstart; }
    
    public int getBfinish() { return bfinish; }
    public void setBfinish(int bfinish) { this.bfinish = bfinish; }
    
    // 树结构访问器
    public ASTNode getParent() { return parent; }
    public void setParent(ASTNode parent) { this.parent = parent; }
    
    public List<ASTNode> getChildren() { return children; }
    public void setChildren(List<ASTNode> children) { 
        this.children = children != null ? children : new ArrayList<>();
        // 设置父节点
        for (ASTNode child : this.children) {
            if (child != null) {
                child.setParent(this);
            }
        }
    }
    
    public void addChild(ASTNode child) { 
        if (child != null) {
            children.add(child);
            child.setParent(this);
        }
    }
    
    public void removeChild(ASTNode child) {
        if (child != null) {
            children.remove(child);
            child.setParent(null);
        }
    }
    
    public ASTNode getChild(int index) {
        return index >= 0 && index < children.size() ? children.get(index) : null;
    }
    
    public int getChildCount() {
        return children.size();
    }
    
    // 绑定信息访问器
    public List<ASTNode> getBindDocs() { return bindDocs; }
    public void setBindDocs(List<ASTNode> bindDocs) { this.bindDocs = bindDocs; }
    public void addBindDoc(ASTNode doc) {
        if (bindDocs == null) bindDocs = new ArrayList<>();
        bindDocs.add(doc);
    }
    
    public List<ASTNode> getBindGroup() { return bindGroup; }
    public void setBindGroup(List<ASTNode> bindGroup) { this.bindGroup = bindGroup; }
    
    public ASTNode getBindSource() { return bindSource; }
    public void setBindSource(ASTNode bindSource) { this.bindSource = bindSource; }
    
    // 引用信息访问器
    public List<ASTNode> getRef() { return ref; }
    public void setRef(List<ASTNode> ref) { this.ref = ref; }
    public void addRef(ASTNode ref) {
        if (this.ref == null) this.ref = new ArrayList<>();
        this.ref.add(ref);
    }
    
    public ASTNode getNode() { return node; }
    public void setNode(ASTNode node) { this.node = node; }
    
    // 特殊标记访问器
    public String getSpecial() { return special; }
    public void setSpecial(String special) { this.special = special; }
    
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    
    public boolean isLiteral() { return literal; }
    public void setLiteral(boolean literal) { this.literal = literal; }
    
    // 属性访问器
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    public void removeAttribute(String key) {
        attributes.remove(key);
    }
    
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }
    
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
    
    // 便利方法
    public String getStringAttribute(String key) {
        Object value = getAttribute(key);
        return value instanceof String ? (String) value : null;
    }
    
    public Integer getIntAttribute(String key) {
        Object value = getAttribute(key);
        return value instanceof Integer ? (Integer) value : null;
    }
    
    public Boolean getBooleanAttribute(String key) {
        Object value = getAttribute(key);
        return value instanceof Boolean ? (Boolean) value : null;
    }
    
    /**
     * 获取字面量值
     */
    public Object getLiteralValue() {
        if (!literal) return null;
        return getAttribute("value");
    }
    
    /**
     * 设置字面量值
     */
    public void setLiteralValue(Object value) {
        this.literal = true;
        setAttribute("value", value);
    }
    
    /**
     * 检查是否包含指定位置
     */
    public boolean contains(int position) {
        return start <= position && position <= finish;
    }
    
    /**
     * 检查是否在范围内
     */
    public boolean inRange(int position) {
        int rangeEnd = range > 0 ? range : finish;
        return start <= position && position <= rangeEnd;
    }
    
    /**
     * 访问者模式支持
     */
    public void accept(ASTVisitor visitor) {
        if (visitor != null) {
            visitor.visit(this);
            for (ASTNode child : children) {
                if (child != null) {
                    child.accept(visitor);
                }
            }
        }
    }
    
    /**
     * 深度优先遍历
     */
    public void traverse(java.util.function.Consumer<ASTNode> visitor) {
        if (visitor != null) {
            visitor.accept(this);
            for (ASTNode child : children) {
                if (child != null) {
                    child.traverse(visitor);
                }
            }
        }
    }
    
    /**
     * 查找指定类型的子节点
     */
    public List<ASTNode> findChildrenByType(String type) {
        List<ASTNode> result = new ArrayList<>();
        if (type != null) {
            for (ASTNode child : children) {
                if (type.equals(child.getType())) {
                    result.add(child);
                }
            }
        }
        return result;
    }
    
    /**
     * 查找第一个指定类型的子节点
     */
    public ASTNode findFirstChildByType(String type) {
        if (type != null) {
            for (ASTNode child : children) {
                if (type.equals(child.getType())) {
                    return child;
                }
            }
        }
        return null;
    }
    
    /**
     * 递归查找指定类型的所有后代节点
     */
    public List<ASTNode> findDescendantsByType(String type) {
        List<ASTNode> result = new ArrayList<>();
        if (type != null) {
            traverse(node -> {
                if (type.equals(node.getType()) && node != this) {
                    result.add(node);
                }
            });
        }
        return result;
    }
    
    /**
     * 获取根节点
     */
    public ASTNode getRoot() {
        ASTNode root = this;
        while (root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }
    
    /**
     * 获取深度
     */
    public int getDepth() {
        int depth = 0;
        ASTNode current = this.parent;
        while (current != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }
    
    /**
     * 检查是否是祖先节点
     */
    public boolean isAncestorOf(ASTNode node) {
        if (node == null) return false;
        ASTNode current = node.parent;
        while (current != null) {
            if (current == this) return true;
            current = current.parent;
        }
        return false;
    }
    
    /**
     * 检查是否是后代节点
     */
    public boolean isDescendantOf(ASTNode node) {
        return node != null && node.isAncestorOf(this);
    }
    
    /**
     * 克隆节点（浅拷贝）
     */
    public ASTNode clone() {
        ASTNode cloned = new ASTNode(type, start, finish);
        cloned.range = range;
        cloned.effect = effect;
        cloned.bstart = bstart;
        cloned.bfinish = bfinish;
        cloned.special = special;
        cloned.tag = tag;
        cloned.literal = literal;
        cloned.attributes = new HashMap<>(attributes);
        return cloned;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        if (start != 0 || finish != 0) {
            sb.append("[").append(start).append(":").append(finish).append("]");
        }
        if (tag != null) {
            sb.append("(").append(tag).append(")");
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ASTNode)) return false;
        ASTNode other = (ASTNode) obj;
        return Objects.equals(type, other.type) &&
               start == other.start &&
               finish == other.finish &&
               Objects.equals(tag, other.tag);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, start, finish, tag);
    }
}
