package com.luaparser.ast.nodes;

import com.luaparser.ast.ASTNode;
import java.util.*;

/**
 * 函数节点
 */
public class FunctionNode extends ASTNode {
    private ASTNode name;
    private ASTNode args;
    private List<int[]> keyword; // 关键字位置信息
    private List<ASTNode> locals;
    private List<ASTNode> returns;
    private List<ASTNode> breaks;
    private List<ASTNode> gotos;
    private Map<String, ASTNode> labels;
    private ASTNode vararg;
    private boolean hasReturn;
    private boolean hasBreak;
    private boolean hasGoTo;
    private boolean hasExit;
    private boolean async;
    private int asyncPos;
    
    public FunctionNode() {
        super("function");
        this.keyword = new ArrayList<>();
    }
    
    public ASTNode getName() { return name; }
    public void setName(ASTNode name) { 
        this.name = name;
        if (name != null) name.setParent(this);
    }
    
    public ASTNode getArgs() { return args; }
    public void setArgs(ASTNode args) { 
        this.args = args;
        if (args != null) args.setParent(this);
    }
    
    public List<int[]> getKeyword() { return keyword; }
    public void addKeyword(int start, int finish) {
        keyword.add(new int[]{start, finish});
    }
    
    public List<ASTNode> getLocals() { return locals; }
    public void setLocals(List<ASTNode> locals) { this.locals = locals; }
    public void addLocal(ASTNode local) {
        if (locals == null) locals = new ArrayList<>();
        locals.add(local);
    }
    
    public List<ASTNode> getReturns() { return returns; }
    public void setReturns(List<ASTNode> returns) { this.returns = returns; }
    public void addReturn(ASTNode returnNode) {
        if (returns == null) returns = new ArrayList<>();
        returns.add(returnNode);
    }
    
    public List<ASTNode> getBreaks() { return breaks; }
    public void setBreaks(List<ASTNode> breaks) { this.breaks = breaks; }
    
    public List<ASTNode> getGotos() { return gotos; }
    public void setGotos(List<ASTNode> gotos) { this.gotos = gotos; }
    
    public Map<String, ASTNode> getLabels() { return labels; }
    public void setLabels(Map<String, ASTNode> labels) { this.labels = labels; }
    
    public ASTNode getVararg() { return vararg; }
    public void setVararg(ASTNode vararg) { this.vararg = vararg; }
    
    public boolean hasReturn() { return hasReturn; }
    public void setHasReturn(boolean hasReturn) { this.hasReturn = hasReturn; }
    
    public boolean hasBreak() { return hasBreak; }
    public void setHasBreak(boolean hasBreak) { this.hasBreak = hasBreak; }
    
    public boolean hasGoTo() { return hasGoTo; }
    public void setHasGoTo(boolean hasGoTo) { this.hasGoTo = hasGoTo; }
    
    public boolean hasExit() { return hasExit; }
    public void setHasExit(boolean hasExit) { this.hasExit = hasExit; }
    
    public boolean isAsync() { return async; }
    public void setAsync(boolean async) { this.async = async; }
    
    public int getAsyncPos() { return asyncPos; }
    public void setAsyncPos(int asyncPos) { this.asyncPos = asyncPos; }
}
