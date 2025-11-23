package com.yan.luaeditor.vtl;

public class Token {
    public final TokenType type;
    public final String string;
    public final int line;
    public final int col;
    Token(TokenType type, String string, int line, int col) {
        this.type = type;
        this.string = string;
        this.line = line;
        this.col  = col;
    }
    @Override public String toString() {
        return type + "(" + string + ") @" + line + ":" + col;
    }
}
