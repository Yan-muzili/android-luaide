package com.luaparser.doc;

import com.luaparser.ast.ASTNode;
import com.luaparser.lexer.Token;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * Lua文档注释解析器 - 完整对应luadoc.lua
 */
public class LuaDocParser {
    
    public static class DocResult {
        private final List<DocNode> docs;
        private final List<DocGroup> groups;
        
        public DocResult(List<DocNode> docs, List<DocGroup> groups) {
            this.docs = docs != null ? docs : new ArrayList<>();
            this.groups = groups != null ? groups : new ArrayList<>();
        }
        
        public List<DocNode> getDocs() { return docs; }
        public List<DocGroup> getGroups() { return groups; }
    }
    
    public static class DocNode {
        private String type;
        private int start;
        private int finish;
        private String content;
        private Map<String, Object> attributes = new HashMap<>();
        
        public DocNode(String type, int start, int finish, String content) {
            this.type = type;
            this.start = start;
            this.finish = finish;
            this.content = content;
        }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public int getStart() { return start; }
        public void setStart(int start) { this.start = start; }
        
        public int getFinish() { return finish; }
        public void setFinish(int finish) { this.finish = finish; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Map<String, Object> getAttributes() { return attributes; }
        public void setAttributes(Map<String, Object> attributes) { 
            this.attributes = attributes != null ? attributes : new HashMap<>(); 
        }
        
        public Object getAttribute(String key) { return attributes.get(key); }
        public void setAttribute(String key, Object value) { attributes.put(key, value); }
        
        @Override
        public String toString() {
            return String.format("DocNode{%s, %d-%d, '%s'}", type, start, finish, content);
        }
    }
    
    public static class DocGroup {
        private final List<DocNode> docs = new ArrayList<>();
        private ASTNode bindSource;
        
        public List<DocNode> getDocs() { return docs; }
        public void addDoc(DocNode doc) { if (doc != null) docs.add(doc); }
        
        public ASTNode getBindSource() { return bindSource; }
        public void setBindSource(ASTNode bindSource) { this.bindSource = bindSource; }
        
        @Override
        public String toString() {
            return String.format("DocGroup{docs=%d, bound=%s}", docs.size(), bindSource != null);
        }
    }
    
    // 文档标签模式
    private static final Pattern DOC_PATTERN = Pattern.compile("^\\s*---?\\s*@(\\w+)(.*)$");
    private static final Pattern PARAM_PATTERN = Pattern.compile("^\\s*(\\w+)\\s+(.+)$");
    private static final Pattern TYPE_PATTERN = Pattern.compile("^\\s*([^\\s]+)(?:\\s+(.*))?$");
    private static final Pattern CLASS_PATTERN = Pattern.compile("^\\s*(\\w+)(?:\\s*:\\s*(\\w+))?(?:\\s+(.*))?$");
    private static final Pattern FIELD_PATTERN = Pattern.compile("^\\s*(\\w+)\\s+([^\\s]+)(?:\\s+(.*))?$");
    private static final Pattern GENERIC_PATTERN = Pattern.compile("^\\s*(\\w+)(?:\\s*:\\s*([^\\s]+))?(?:\\s+(.*))?$");
    private static final Pattern ALIAS_PATTERN = Pattern.compile("^\\s*(\\w+)\\s+(.+)$");
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\s*([<>=!]+)\\s*([\\d.]+)(?:\\s+(.*))?$");
    
    /**
     * 解析文档注释
     */
    public DocResult parseDocumentation(ASTNode ast, List<Token> tokens) {
        List<DocNode> docs = new ArrayList<>();
        List<DocGroup> groups = new ArrayList<>();
        
        if (tokens == null || tokens.isEmpty()) {
            return new DocResult(docs, groups);
        }
        
        // 提取注释token
        List<Token> comments = tokens.stream()
            .filter(Token::isComment)
            .collect(Collectors.toList());
        
        DocGroup currentGroup = null;
        
        for (Token comment : comments) {
            String text = comment.getContent();
            
            // 检查是否是文档注释
            if (isDocComment(text)) {
                DocNode docNode = parseDocComment(comment);
                if (docNode != null) {
                    docs.add(docNode);
                    
                    // 创建新组或添加到当前组
                    if (shouldStartNewGroup(docNode, currentGroup)) {
                        currentGroup = new DocGroup();
                        groups.add(currentGroup);
                    }
                    
                    if (currentGroup != null) {
                        currentGroup.addDoc(docNode);
                    }
                }
            } else {
                // 普通注释，结束当前组
                if (currentGroup != null && !currentGroup.getDocs().isEmpty()) {
                    currentGroup = null;
                }
            }
        }
        
        // 绑定文档到AST节点
        bindDocsToAST(groups, ast);
        
        return new DocResult(docs, groups);
    }
    
    /**
     * 检查是否是文档注释
     */
    private boolean isDocComment(String text) {
        return text.contains("@") || text.startsWith("---");
    }
    
    /**
     * 解析单个文档注释
     */
    private DocNode parseDocComment(Token comment) {
        String text = comment.getContent().trim();
        
        Matcher matcher = DOC_PATTERN.matcher(text);
        if (!matcher.matches()) {
            // 尝试解析简单的文档注释
            if (text.startsWith("---")) {
                String content = text.substring(3).trim();
                return new DocNode("doc.comment", comment.getStart(), comment.getFinish(), content);
            }
            return null;
        }
        
        String tag = matcher.group(1);
        String content = matcher.group(2).trim();
        
        DocNode docNode = new DocNode("doc." + tag, comment.getStart(), comment.getFinish(), content);
        
        // 解析特定标签
        switch (tag) {
            case "param":
                parseParamDoc(docNode, content);
                break;
            case "return":
                parseReturnDoc(docNode, content);
                break;
            case "type":
                parseTypeDoc(docNode, content);
                break;
            case "class":
                parseClassDoc(docNode, content);
                break;
            case "field":
                parseFieldDoc(docNode, content);
                break;
            case "generic":
                parseGenericDoc(docNode, content);
                break;
            case "alias":
                parseAliasDoc(docNode, content);
                break;
            case "enum":
                parseEnumDoc(docNode, content);
                break;
            case "overload":
                parseOverloadDoc(docNode, content);
                break;
            case "deprecated":
                parseDeprecatedDoc(docNode, content);
                break;
            case "see":
                parseSeeDoc(docNode, content);
                break;
            case "version":
                parseVersionDoc(docNode, content);
                break;
            case "since":
                parseSinceDoc(docNode, content);
                break;
            case "async":
                parseAsyncDoc(docNode, content);
                break;
            case "nodiscard":
                parseNodeiscardDoc(docNode, content);
                break;
            case "meta":
                parseMetaDoc(docNode, content);
                break;
            case "module":
                parseModuleDoc(docNode, content);
                break;
            case "author":
                parseAuthorDoc(docNode, content);
                break;
            case "copyright":
                parseCopyrightDoc(docNode, content);
                break;
            case "license":
                parseLicenseDoc(docNode, content);
                break;
            default:
                // 未知标签，保持原始内容
                docNode.setAttribute("rawContent", content);
                break;
        }
        
        return docNode;
    }
    
    /**
     * 解析@param标签
     */
    private void parseParamDoc(DocNode docNode, String content) {
        Matcher matcher = PARAM_PATTERN.matcher(content);
        if (matcher.matches()) {
            docNode.setAttribute("paramName", matcher.group(1));
            String typeAndDesc = matcher.group(2);
            
            // 尝试分离类型和描述
            String[] parts = typeAndDesc.split("\\s+", 2);
            if (parts.length >= 1) {
                docNode.setAttribute("paramType", parts[0]);
            }
            if (parts.length >= 2) {
                docNode.setAttribute("paramDesc", parts[1]);
            }
        } else {
            docNode.setAttribute("rawContent", content);
        }
    }
    
    /**
     * 解析@return标签
     */
    private void parseReturnDoc(DocNode docNode, String content) {
        Matcher matcher = TYPE_PATTERN.matcher(content);
        if (matcher.matches()) {
            docNode.setAttribute("returnType", matcher.group(1));
            if (matcher.group(2) != null) {
                docNode.setAttribute("returnDesc", matcher.group(2));
            }
        } else {
            docNode.setAttribute("rawContent", content);
        }
    }
    
    /**
     * 解析@type标签
     */
    private void parseTypeDoc(DocNode docNode, String content) {
        docNode.setAttribute("typeSpec", content);
    }
    
    /**
     * 解析@class标签
     */
    private void parseClassDoc(DocNode docNode, String content) {
        Matcher matcher = CLASS_PATTERN.matcher(content);
        if (matcher.matches()) {
            docNode.setAttribute("className", matcher.group(1));
            if (matcher.group(2) != null) {
                docNode.setAttribute("superClass", matcher.group(2));
            }
            if (matcher.group(3) != null) {
                docNode.setAttribute("classDesc", matcher.group(3));
            }
        } else {
            docNode.setAttribute("rawContent", content);
        }
    }
    
    /**
     * 解析@field标签
     */
    private void parseFieldDoc(DocNode docNode, String content) {
        Matcher matcher = FIELD_PATTERN.matcher(content);
        if (matcher.matches()) {
            docNode.setAttribute("fieldName", matcher.group(1));
            docNode.setAttribute("fieldType", matcher.group(2));
            if (matcher.group(3) != null) {
                docNode.setAttribute("fieldDesc", matcher.group(3));
            }
        } else {
            docNode.setAttribute("rawContent", content);
        }
    }
    
    /**
     * 解析@generic标签
     */
    private void parseGenericDoc(DocNode docNode, String content) {
        Matcher matcher = GENERIC_PATTERN.matcher(content);
        if (matcher.matches()) {
            docNode.setAttribute("genericName", matcher.group(1));
            if (matcher.group(2) != null) {
                docNode.setAttribute("genericConstraint", matcher.group(2));
            }
            if (matcher.group(3) != null) {
                docNode.setAttribute("genericDesc", matcher.group(3));
            }
        } else {
            docNode.setAttribute("rawContent", content);
        }
    }
    
    /**
     * 解析@alias标签
     */
    private void parseAliasDoc(DocNode docNode, String content) {
        Matcher matcher = ALIAS_PATTERN.matcher(content);
        if (matcher.matches()) {
            docNode.setAttribute("aliasName", matcher.group(1));
            docNode.setAttribute("aliasType", matcher.group(2));
        } else {
            docNode.setAttribute("rawContent", content);
        }
    }
    
    /**
     * 解析@enum标签
     */
    private void parseEnumDoc(DocNode docNode, String content) {
        docNode.setAttribute("enumName", content.trim());
    }
    
    /**
     * 解析@overload标签
     */
    private void parseOverloadDoc(DocNode docNode, String content) {
        docNode.setAttribute("overloadSpec", content);
    }
    
    /**
     * 解析@deprecated标签
     */
    private void parseDeprecatedDoc(DocNode docNode, String content) {
        docNode.setAttribute("deprecatedReason", content);
    }
    
    /**
     * 解析@see标签
     */
    private void parseSeeDoc(DocNode docNode, String content) {
        docNode.setAttribute("seeRef", content);
    }
    
    /**
     * 解析@version标签
     */
    private void parseVersionDoc(DocNode docNode, String content) {
        Matcher matcher = VERSION_PATTERN.matcher(content);
        if (matcher.matches()) {
            docNode.setAttribute("versionOp", matcher.group(1));
            docNode.setAttribute("versionNum", matcher.group(2));
            if (matcher.group(3) != null) {
                docNode.setAttribute("versionDesc", matcher.group(3));
            }
        } else {
            docNode.setAttribute("versionSpec", content);
        }
    }
    
    /**
     * 解析@since标签
     */
    private void parseSinceDoc(DocNode docNode, String content) {
        docNode.setAttribute("sinceVersion", content);
    }
    
    /**
     * 解析@async标签
     */
    private void parseAsyncDoc(DocNode docNode, String content) {
        docNode.setAttribute("asyncInfo", content);
    }
    
    /**
     * 解析@nodiscard标签
     */
    private void parseNodeiscardDoc(DocNode docNode, String content) {
        docNode.setAttribute("nodiscardInfo", content);
    }
    
    /**
     * 解析@meta标签
     */
    private void parseMetaDoc(DocNode docNode, String content) {
        docNode.setAttribute("metaInfo", content);
    }
    
    /**
     * 解析@module标签
     */
    private void parseModuleDoc(DocNode docNode, String content) {
        docNode.setAttribute("moduleName", content);
    }
    
    /**
     * 解析@author标签
     */
    private void parseAuthorDoc(DocNode docNode, String content) {
        docNode.setAttribute("authorName", content);
    }
    
    /**
     * 解析@copyright标签
     */
    private void parseCopyrightDoc(DocNode docNode, String content) {
        docNode.setAttribute("copyrightInfo", content);
    }
    
    /**
     * 解析@license标签
     */
    private void parseLicenseDoc(DocNode docNode, String content) {
        docNode.setAttribute("licenseInfo", content);
    }
    
    /**
     * 判断是否应该开始新组
     */
    private boolean shouldStartNewGroup(DocNode docNode, DocGroup currentGroup) {
        if (currentGroup == null) {
            return true;
        }
        
        // 某些标签总是开始新组
        String type = docNode.getType();
        return "doc.class".equals(type) || 
               "doc.alias".equals(type) || 
               "doc.enum".equals(type) ||
               "doc.module".equals(type);
    }
    
    /**
     * 绑定文档到AST节点
     */
    private void bindDocsToAST(List<DocGroup> groups, ASTNode ast) {
        if (groups.isEmpty() || ast == null) return;
        
        // 简化版本的文档绑定
        for (DocGroup group : groups) {
            ASTNode bindTarget = findBindTarget(group, ast);
            if (bindTarget != null) {
                group.setBindSource(bindTarget);
                
                // 设置节点的文档绑定
                @SuppressWarnings("unchecked")
                List<DocNode> bindDocs = (List<DocNode>) bindTarget.getAttribute("bindDocs");
                if (bindDocs == null) {
                    bindDocs = new ArrayList<>();
                    bindTarget.setAttribute("bindDocs", bindDocs);
                }
                bindDocs.addAll(group.getDocs());
            }
        }
    }
    
    /**
     * 查找绑定目标
     */
    private ASTNode findBindTarget(DocGroup group, ASTNode ast) {
        // 简化实现：查找第一个合适的节点
        List<ASTNode> candidates = new ArrayList<>();
        findBindCandidates(ast, candidates);
        
        if (candidates.isEmpty()) return null;
        
        // 根据文档类型选择最合适的候选者
        for (DocNode doc : group.getDocs()) {
            String type = doc.getType();
            if ("doc.class".equals(type) || "doc.module".equals(type)) {
                // 类或模块文档绑定到主节点
                return ast;
            } else if ("doc.param".equals(type) || "doc.return".equals(type)) {
                // 参数或返回值文档绑定到函数
                for (ASTNode candidate : candidates) {
                    if ("function".equals(candidate.getType())) {
                        return candidate;
                    }
                }
            }
        }
        
        return candidates.get(0);
    }
    
    /**
     * 查找绑定候选者
     */
    private void findBindCandidates(ASTNode node, List<ASTNode> candidates) {
        if (node == null) return;
        
        String type = node.getType();
        if ("function".equals(type) || "local".equals(type) || 
            "setglobal".equals(type) || "setlocal".equals(type) ||
            "class".equals(type) || "interface".equals(type)) {
            candidates.add(node);
        }
        
        for (ASTNode child : node.getChildren()) {
            findBindCandidates(child, candidates);
        }
    }
}
