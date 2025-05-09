package com.yan.luaeditor.format;

import com.yan.luaeditor.format.LuaLexer;
import com.yan.luaeditor.format.LuaTokenTypes;

import java.io.IOException;

public class AutoIndent {
    public static int createAutoIndent(CharSequence text) {
        LuaLexer lexer = new LuaLexer(text);
        int idt = 0;
        try {
            while (true) {
                LuaTokenTypes type = lexer.advance();
                if (type == null) {
                    break;
                }
                if (lexer.yytext().equals("switch")||lexer.yytext().equals("try")||lexer.yytext().equals("catch"))
                    idt += 1;
                else
                    idt += indent(type);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return idt;
    }

    private static int indent(LuaTokenTypes t) {
        switch (t) {
            case FOR:
            case WHILE:
            case FUNCTION:
            case IF:
            case REPEAT:
            case LCURLY:
            case SWITCH:
                return 1;
            case UNTIL:
            case END:
            case RCURLY:
                return -1;
            default:
                return 0;
        }
    }

    public static CharSequence format(CharSequence text, int width) {
        CharSequence firstPass = firstPassFormat(text);
        return secondPassFormat(firstPass, width);
    }

    private static CharSequence firstPassFormat(CharSequence text) {
        StringBuilder builder = new StringBuilder();
        LuaLexer lexer = new LuaLexer(text);
        try {
            LuaTokenTypes prevType = null;
            while (true) {
                LuaTokenTypes type = lexer.advance();
                if (type == null)
                    break;

                if (type == LuaTokenTypes.WHITE_SPACE) {
                    continue;
                }

                if (prevType != null) {
                    if (isKeyword(prevType) && (isIdentifier(type) || isOperator(type))) {
                        builder.append(' ');
                    } else if (isIdentifier(prevType) && isKeyword(type)) {
                        builder.append(' ');
                    } else if (prevType == LuaTokenTypes.COMMA) {
                        builder.append(' ');
                    } else if (lexer.yytext().equals("and")) {
                        builder.append(' ');
                    } else if (lexer.yytext().equals("or")) {
                        builder.append(' ');
                    } else if (isKeyword(prevType)) {
                        builder.append(' ');
                    } else if (lexer.yytext().equals("do")) {
                        builder.append(' ');
                    } else if (lexer.yytext().equals("then")) {
                        builder.append(' ');
                    } else if (lexer.yytext().equals("in")) {
                        builder.append(' ');
                    }
                }
                if (lexer.yytext().equals("pairs") && builder.length() > 0 && builder.charAt(builder.length() - 1) != ' ') {
                    builder.append(' ');
                }else if (lexer.yytext().equals("ipairs") && builder.length() > 0 && builder.charAt(builder.length() - 1) != ' ') {
                    builder.append(' ');
                }
                builder.append(lexer.yytext());

                if (lexer.yytext().equals("import") || lexer.yytext().equals("require") || lexer.yytext().equals("do") || lexer.yytext().equals("and") || lexer.yytext().equals("or") || lexer.yytext().equals("then")||lexer.yytext().equals("in")) {
                    builder.append(" ");
                }
                prevType = type;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder;
    }

    private static CharSequence secondPassFormat(CharSequence text, int width) {
        StringBuilder builder = new StringBuilder();
        boolean isNewLine = true;
        LuaLexer lexer = new LuaLexer(text);
        try {
            int idt = 0;

            while (true) {
                LuaTokenTypes type = lexer.advance();
                if (type == null)
                    break;

                if (type == LuaTokenTypes.NEW_LINE) {
                    if (builder.length() > 0 && builder.charAt(builder.length() - 1) == ' ')
                        builder.deleteCharAt(builder.length() - 1);
                    isNewLine = true;
                    builder.append('\n');
                    idt = Math.max(0, idt);
                } else if (isNewLine) {
                    switch (type) {
                        case WHITE_SPACE:
                            break;
                        case CASE:
                        case DEFAULT:
                            //idt--;
                            builder.append(createIndent(idt * width-width/2 ));
                            builder.append(lexer.yytext());
                            //idt++;
                            isNewLine = false;
                            break;
                        case ELSE:
                        case ELSEIF:
                            builder.append(createIndent((idt-1) * width));
                            builder.append(lexer.yytext());
                            isNewLine=false;
                            break;
                        case DOUBLE_COLON:
                        case AT:
                            builder.append(lexer.yytext());
                            isNewLine = false;
                            break;
                        case END:
                        case UNTIL:
                        case RCURLY:
                            idt--;
                            builder.append(createIndent(idt * width));
                            builder.append(lexer.yytext());
                            isNewLine = false;
                            break;
                        default:
                            if (lexer.yytext().equals("try")){
                                builder.append(createIndent((idt*width)));
                                builder.append(lexer.yytext());
                                idt++;
                            } else if (lexer.yytext().equals("catch")) {
                                builder.append(createIndent((idt-1)*width));
                                builder.append(lexer.yytext());
                            }else {
                                builder.append(createIndent(idt * width));
                                builder.append(lexer.yytext());
                            }
                            idt += indent(type);

                            isNewLine = false;
                    }
                } else if (type == LuaTokenTypes.WHITE_SPACE) {
                    builder.append(' ');
                } else {
                    if (isOperator(type)) {
                        if (builder.length() > 0 && builder.charAt(builder.length() - 1) != ' ') {
                            builder.append(' ');
                        }
                        builder.append(lexer.yytext());
                        builder.append(' ');
                    } else {
                        builder.append(lexer.yytext());
                    }
                    idt += indent(type);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder;
    }

    private static boolean isKeyword(LuaTokenTypes type) {
        switch (type) {
            case IF:
            case ELSE:
            case ELSEIF:
            case FOR:
            case WHILE:
            case FUNCTION:
            case REPEAT:
            case UNTIL:
            case END:
            case RETURN:
            case LOCAL:
            case CASE:
            case SWITCH:
            case NOT:
            case GOTO:
                return true;
            default:
                return false;
        }
    }

    private static boolean isIdentifier(LuaTokenTypes type) {
        return type == LuaTokenTypes.NAME;
    }

    private static boolean isOperator(LuaTokenTypes type) {
        switch (type) {
            case PLUS:
            case MINUS:
            case MULT:
            case DIV:
            case MOD:
            case EXP:
            case ASSIGN:
            case EQ:
            case NE:
            case LT:
            case GT:
            case LE:
            case GE:
            case CONCAT:
            case DOUBLE_DIV:
            case BIT_AND:
            case BIT_OR:
            case BIT_TILDE:
            case BIT_LTLT:
            case BIT_RTRT:
                return true;
            default:
                return false;
        }
    }


    private static char[] createIndent(int n) {
        if (n < 0)
            return new char[0];
        char[] idts = new char[n];
        for (int i = 0; i < n; i++)
            idts[i] = ' ';
        return idts;
    }
}