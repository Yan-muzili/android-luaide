package com.luaparser.parser;

import com.luaparser.ast.ASTNode;
import com.luaparser.ParseOptions;
import java.util.List;
import java.util.ArrayList;

/**
 * 解析状态类 - 完整对应compile.lua中的State
 */
public class ParseState {
    private String version;
    private String lua;
    private ASTNode ast;
    private List<ParseError> errors = new ArrayList<>();
    private List<Object> comments = new ArrayList<>();
    private int[] lines;
    private ParseOptions options;
    private String uri;
    
    // Getters and Setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getLua() { return lua; }
    public void setLua(String lua) { this.lua = lua; }
    
    public ASTNode getAst() { return ast; }
    public void setAst(ASTNode ast) { this.ast = ast; }
    
    public List<ParseError> getErrors() { return errors; }
    public void setErrors(List<ParseError> errors) { 
        this.errors = errors != null ? errors : new ArrayList<>(); 
    }
    
    public List<Object> getComments() { return comments; }
    public void setComments(List<Object> comments) { 
        this.comments = comments != null ? comments : new ArrayList<>(); 
    }
    
    public int[] getLines() { return lines; }
    public void setLines(int[] lines) { this.lines = lines; }
    
    public ParseOptions getOptions() { return options; }
    public void setOptions(ParseOptions options) { this.options = options; }
    
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    
    // 便利方法
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    public void addError(ParseError error) {
        if (error != null) {
            errors.add(error);
        }
    }
    
    public void addComment(Object comment) {
        if (comment != null) {
            comments.add(comment);
        }
    }
    
    @Override
    public String toString() {
        return String.format("ParseState{version='%s', uri='%s', errors=%d, comments=%d}", 
                           version, uri, errors.size(), comments.size());
    }
}
