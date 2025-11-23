package com.luaparser.lexer;

import java.util.Objects;

/**
 * Token类 - 完整对应tokens.lua的Token结构
 */
public class Token {
    private final TokenType type;
    private final String content;
    private final int start;
    private final int finish;
    private final int line;
    private final int column;
    
    public Token(TokenType type, String content, int start, int finish, int line, int column) {
        this.type = type;
        this.content = content;
        this.start = start;
        this.finish = finish;
        this.line = line;
        this.column = column;
    }
    
    // Getters
    public TokenType getType() { return type; }
    public String getContent() { return content; }
    public int getStart() { return start; }
    public int getFinish() { return finish; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    
    // 类型检查方法
    public boolean isSpace() { return type == TokenType.SPACE; }
    public boolean isNewline() { return type == TokenType.NEWLINE; }
    public boolean isNumber() { return type == TokenType.NUMBER || type == TokenType.INTEGER; }
    public boolean isWord() { return type == TokenType.WORD; }
    public boolean isString() { return type == TokenType.STRING_SHORT || type == TokenType.STRING_LONG; }
    public boolean isComment() { 
        return type == TokenType.COMMENT_SHORT || 
               type == TokenType.COMMENT_LONG || 
               type == TokenType.COMMENT_CSHORT; 
    }
    public boolean isSymbol() { return type.name().startsWith("SYMBOL") || type == TokenType.SYMBOL; }
    public boolean isEOF() { return type == TokenType.EOF; }
    
    @Override
    public String toString() {
        return String.format("Token{%s, '%s', %d-%d, %d:%d}", 
                           type, content, start, finish, line, column);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Token)) return false;
        Token other = (Token) obj;
        return type == other.type && 
               Objects.equals(content, other.content) &&
               start == other.start && 
               finish == other.finish;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, content, start, finish);
    }
}
