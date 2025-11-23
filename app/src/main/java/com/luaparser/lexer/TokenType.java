package com.luaparser.lexer;

/**
 * Token类型枚举 - 完整对应tokens.lua中的所有类型
 */
public enum TokenType {
    // 基础类型
    SPACE("Space"),
    NEWLINE("Newline"),
    NUMBER("Number"),
    INTEGER("Integer"),
    WORD("Word"),
    SYMBOL("Symbol"),
    UNKNOWN("Unknown"),
    EOF("EOF"),
    
    // 字符串类型
    STRING_SHORT("String"),
    STRING_LONG("String"),
    
    // 注释类型
    COMMENT_SHORT("Comment"),
    COMMENT_LONG("Comment"),
    COMMENT_CSHORT("Comment"),
    
    // 特殊符号
    SYMBOL_EQ("=="),
    SYMBOL_NE("~="),
    SYMBOL_LE("<="),
    SYMBOL_GE(">="),
    SYMBOL_SHL("<<"),
    SYMBOL_SHR(">>"),
    SYMBOL_IDIV("//"),
    SYMBOL_CONCAT(".."),
    SYMBOL_DOTS("..."),
    SYMBOL_LABEL("::"),
    
    // 非标准符号
    SYMBOL_NE_ALT("!="),
    SYMBOL_AND_ALT("&&"),
    SYMBOL_OR_ALT("||"),
    SYMBOL_ASSIGN_ADD("+="),
    SYMBOL_ASSIGN_SUB("-="),
    SYMBOL_ASSIGN_MUL("*="),
    SYMBOL_ASSIGN_DIV("/="),
    SYMBOL_ASSIGN_MOD("%="),
    SYMBOL_ASSIGN_POW("^="),
    SYMBOL_ASSIGN_IDIV("//="),
    SYMBOL_ASSIGN_AND("&="),
    SYMBOL_ASSIGN_OR("|="),
    SYMBOL_ASSIGN_SHL("<<="),
    SYMBOL_ASSIGN_SHR(">>=");
    
    private final String display;
    
    TokenType(String display) {
        this.display = display;
    }
    
    public String getDisplay() {
        return display;
    }
}
