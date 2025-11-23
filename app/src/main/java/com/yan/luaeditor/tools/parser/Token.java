package com.yan.luaeditor.tools.parser;

public class Token {
    public enum TokenType {
        IDENTIFIER, NUMBER, STRING, KEYWORD, OPERATOR, PUNCTUATION, EOF, NEWLINE, COMMENT, WHITESPACE
    }

    public TokenType type;
    public String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
