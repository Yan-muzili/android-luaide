package com.luaparser.ast;

import java.util.List;

/**
 * Represents a Lua function
 */
public class FunctionNode extends ASTNode {
    private List<String> parameters;
    private ASTNode body;
    private boolean isLocal;
    private String name;
    
    public FunctionNode(int start, int finish) {
        super("function", start, finish);
    }
    
    public List<String> getParameters() { return parameters; }
    public void setParameters(List<String> parameters) { this.parameters = parameters; }
    
    public ASTNode getBody() { return body; }
    public void setBody(ASTNode body) { 
        this.body = body;
        if (body != null) {
            body.setParent(this);
        }
    }
    
    public boolean isLocal() { return isLocal; }
    public void setLocal(boolean local) { isLocal = local; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
