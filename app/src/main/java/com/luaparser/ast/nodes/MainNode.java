package com.luaparser.ast.nodes;

import com.luaparser.ast.ASTNode;
import java.util.*;

/**
 * 主程序节点
 */
public class MainNode extends ASTNode {
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
    
    public MainNode() {
        super("main");
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
    public void addBreak(ASTNode breakNode) {
        if (breaks == null) breaks = new ArrayList<>();
        breaks.add(breakNode);
    }
    
    public List<ASTNode> getGotos() { return gotos; }
    public void setGotos(List<ASTNode> gotos) { this.gotos = gotos; }
    public void addGoto(ASTNode gotoNode) {
        if (gotos == null) gotos = new ArrayList<>();
        gotos.add(gotoNode);
    }
    
    public Map<String, ASTNode> getLabels() { return labels; }
    public void setLabels(Map<String, ASTNode> labels) { this.labels = labels; }
    public void addLabel(String name, ASTNode label) {
        if (labels == null) labels = new HashMap<>();
        labels.put(name, label);
    }
    
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
}
