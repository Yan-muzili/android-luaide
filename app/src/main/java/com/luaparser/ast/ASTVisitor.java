package com.luaparser.ast;

/**
 * Visitor pattern for traversing AST
 * Based on guide.lua traversal functions
 */
public interface ASTVisitor {
    /**
     * 访问节点的默认方法
     */
    void visit(ASTNode node);
    
    /**
     * 访问特定类型节点的方法（可选实现）
     */
    default void visitMain(ASTNode node) { visit(node); }
    default void visitFunction(ASTNode node) { visit(node); }
    default void visitLocal(ASTNode node) { visit(node); }
    default void visitCall(ASTNode node) { visit(node); }
    default void visitBinary(ASTNode node) { visit(node); }
    default void visitUnary(ASTNode node) { visit(node); }
    default void visitLiteral(ASTNode node) { visit(node); }
    default void visitString(ASTNode node) { visit(node); }
    default void visitNumber(ASTNode node) { visit(node); }
    default void visitBoolean(ASTNode node) { visit(node); }
    default void visitNil(ASTNode node) { visit(node); }
    default void visitTable(ASTNode node) { visit(node); }
    default void visitIf(ASTNode node) { visit(node); }
    default void visitWhile(ASTNode node) { visit(node); }
    default void visitFor(ASTNode node) { visit(node); }
    default void visitRepeat(ASTNode node) { visit(node); }
    default void visitDo(ASTNode node) { visit(node); }
    default void visitReturn(ASTNode node) { visit(node); }
    default void visitBreak(ASTNode node) { visit(node); }
    default void visitGoto(ASTNode node) { visit(node); }
    default void visitLabel(ASTNode node) { visit(node); }
    default void visitGetGlobal(ASTNode node) { visit(node); }
    default void visitSetGlobal(ASTNode node) { visit(node); }
    default void visitGetLocal(ASTNode node) { visit(node); }
    default void visitSetLocal(ASTNode node) { visit(node); }
    default void visitGetField(ASTNode node) { visit(node); }
    default void visitSetField(ASTNode node) { visit(node); }
    default void visitGetIndex(ASTNode node) { visit(node); }
    default void visitSetIndex(ASTNode node) { visit(node); }
    default void visitGetMethod(ASTNode node) { visit(node); }
    default void visitSetMethod(ASTNode node) { visit(node); }
    default void visitVarargs(ASTNode node) { visit(node); }
    default void visitParen(ASTNode node) { visit(node); }
}
