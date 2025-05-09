package com.yan.luaeditor.tools.parser;

import com.yan.luaeditor.format.LuaTokenTypes;
import com.yan.luaeditor.tools.parser.Token;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class LuaLexer {
    private final com.yan.luaeditor.format.LuaLexer lexer;

    public LuaLexer(String input) {
        this.lexer = new com.yan.luaeditor.format.LuaLexer(new java.io.StringReader(input));
    }

    public List<Token> tokenize() throws IOException {
        List<Token> tokens = new ArrayList<>();
        LuaTokenTypes tokenType;
        while ((tokenType = lexer.advance()) != null) {
            String tokenValue = lexer.yytext();
            Token.TokenType type = convertTokenType(tokenType);
            if (type != Token.TokenType.WHITESPACE) {
                tokens.add(new Token(type, tokenValue));
            }
        }
        tokens.add(new Token(Token.TokenType.EOF, ""));
        return tokens;
    }

    private Token.TokenType convertTokenType(LuaTokenTypes tokenType) {
        switch (tokenType) {
            case NAME:
                return Token.TokenType.IDENTIFIER;
            case NUMBER:
                return Token.TokenType.NUMBER;
            case STRING:
            case LONG_STRING:
                return Token.TokenType.STRING;
            case PLUS:
            case MINUS:
            case MULT:
            case DIV:
            case MOD:
            case EQ:
            case EXP:
            case BIT_AND:
            case BIT_OR:
            case BIT_TILDE:
            case BIT_LTLT:
            case BIT_RTRT:
            case CONCAT:
                return Token.TokenType.OPERATOR;
            case ASSIGN:
            case NE:
            case LT:
            case GT:
            case LE:
            case GE:
            case COLON:
            case SEMI:
            case COMMA:
            case DOT:
            case DOUBLE_COLON:
            case DOUBLE_DIV:
            case ELLIPSIS:
            case LBRACK:
            case RBRACK:
            case LCURLY:
            case RCURLY:
            case LPAREN:
            case RPAREN:
                return Token.TokenType.PUNCTUATION;
            case IF:
            case ELSE:
            case WHILE:
            case FOR:
            case FUNCTION:
            case RETURN:
            case LOCAL:
            case DO:
            case END:
            case THEN:
            case UNTIL:
            case REPEAT:
            case ELSEIF:
            case SWITCH:
            case CASE:
            case DEFAULT:
            case REGION:
            case ENDREGION:
            case LABEL:
            case AT:
            case DEFER:
            case WHEN:
            case LAMBDA:
            case CONTINUE:
            case BREAK:
            case GOTO:
            case TRUE:
            case FALSE:
            case NIL:
                return Token.TokenType.KEYWORD;
            case WHITE_SPACE:
                return Token.TokenType.WHITESPACE; // 新增忽略空格的类型
            default:
                return Token.TokenType.EOF;
        }
    }
}