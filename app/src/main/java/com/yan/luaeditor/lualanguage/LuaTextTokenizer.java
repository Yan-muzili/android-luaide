/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 */
package com.yan.luaeditor.lualanguage;

import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.util.TrieTree;

/**
 * @author Rose
 */
public class LuaTextTokenizer {

    private static TrieTree<Tokens> keywords;

    static {
        doStaticInit();
    }

    public static TrieTree<Tokens> getTree() {
        return keywords;
    }

    private CharSequence source;
    protected int bufferLen;
    private int line;
    private int column;
    private int index;
    protected int offset;
    protected int length;
    private Tokens currToken;
    private boolean lcCal;

    public LuaTextTokenizer(CharSequence src) {
        if (src == null) {
            throw new IllegalArgumentException("src can not be null");
        }
        this.source = src;
        init();
    }

    private void init() {
        line = 0;
        column = 0;
        length = 0;
        index = 0;
        currToken = Tokens.WHITESPACE;
        lcCal = false;
        this.bufferLen = source.length();
    }

    public void setCalculateLineColumn(boolean cal) {
        this.lcCal = cal;
    }

    public void pushBack(int length) {
        if (length > getTokenLength()) {
            throw new IllegalArgumentException("pushBack length too large");
        }
        this.length -= length;
    }

    private boolean isIdentifierPart(char ch) {
        return MyCharacter.isJavaIdentifierPart(ch);
    }

    private boolean isIdentifierStart(char ch) {
        return MyCharacter.isJavaIdentifierStart(ch);
    }

    public CharSequence getTokenText() {
        return source.subSequence(offset, offset + length);
    }

    public int getTokenLength() {
        return length;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getIndex() {
        return index;
    }

    public Tokens getToken() {
        return currToken;
    }

    private char charAt(int i) {
        return source.charAt(i);
    }

    private char charAt() {
        return source.charAt(offset + length);
    }

    public Tokens nextToken() {
        return currToken = nextTokenInternal();
    }

    private Tokens nextTokenInternal() {
        if (lcCal) {
            boolean r = false;
            for (int i = offset; i < offset + length; i++) {
                char ch = charAt(i);
                if (ch == '\r') {
                    r = true;
                    line++;
                    column = 0;
                } else if (ch == '\n') {
                    if (r) {
                        r = false;
                        continue;
                    }
                    line++;
                    column = 0;
                } else {
                    r = false;
                    column++;
                }
            }
        }
        index = index + length;
        offset = offset + length;
        if (offset >= bufferLen) {
            return Tokens.EOF;
        }
        char ch = source.charAt(offset);
        length = 1;
        if (ch == '\n') {
            return Tokens.NEWLINE;
        } else if (ch == '\r') {
            scanNewline();
            return Tokens.NEWLINE;
        } else if (isWhitespace(ch)) {
            char chLocal;
            while (offset + length < bufferLen && isWhitespace(chLocal = charAt(offset + length))) {
                if (chLocal == '\r' || chLocal == '\n') {
                    break;
                }
                length++;
            }
            return Tokens.WHITESPACE;
        } else {
            if (isIdentifierStart(ch)) {
                return scanIdentifier(ch);
            }
            if (isPrimeDigit(ch)) {
                return scanNumber();
            }
            /* Scan usual symbols first */
            if (ch == ';') {
                return Tokens.SEMICOLON;
            } else if (ch == '(') {
                return Tokens.LPAREN;
            } else if (ch == ')') {
                return Tokens.RPAREN;
            } else if (ch == ':') {
                return Tokens.COLON;
            } else if (ch == '<') {
                return scanLT();
            } else if (ch == '>') {
                return scanGT();
            }
            /* Scan secondly symbols */
            switch (ch) {
                case '=':
                    return scanOperatorTwo(Tokens.EQ);
                case '.':
                    return Tokens.DOT;
                case '@':
                    return Tokens.AT;
                case '{':
                    return Tokens.LBRACE;
                case '}':
                    return Tokens.RBRACE;
                case '/':
                    return scanDIV();
                case '*':
                    return scanOperatorTwo(Tokens.MUL);
                case '-':
                    return scanDIV();
                case '+':
                    return scanOperatorTwo(Tokens.AND);
                case '[':
                    return Tokens.LBRACK;
                case ']':
                    return Tokens.RBRACK;
                case ',':
                    return Tokens.COMMA;
                case '!':
                    return Tokens.NOT;
                case '~':
                    return Tokens.XOR;
                case '?':
                    return Tokens.QUESTION;
                case '&':
                    return scanOperatorTwo(Tokens.AND);
                case '|':
                    return scanOperatorTwo(Tokens.OR);
                case '^':
                    return scanOperatorTwo(Tokens.POW);
                case '%':
                    return scanOperatorTwo(Tokens.MOD);
                case '\'':
                    scanCharLiteral();
                    return Tokens.CHARACTER_LITERAL;
                case '\"':
                    scanStringLiteral();
                    return Tokens.STRING;
                default:
                    return Tokens.UNKNOWN;
            }
        }
    }

    protected final void throwIfNeeded() {
        if (offset + length >= bufferLen) {
            throw new RuntimeException("Token too long");
        }
    }

    protected void scanNewline() {
        if (offset + length < bufferLen && charAt(offset + length) == '\n') {
            length++;
        }
    }

    protected Tokens scanIdentifier(char ch) {
        TrieTree.Node<Tokens> n = keywords.root.map.get(ch);
        while (offset + length < bufferLen && isIdentifierPart(ch = charAt(offset + length))) {
            length++;
            n = n == null ? null : n.map.get(ch);
        }
        return n == null ? Tokens.IDENTIFIER : (n.token == null ? Tokens.IDENTIFIER : n.token);
    }

    protected void scanTrans() {
        throwIfNeeded();
        char ch = charAt();
        if (ch == '\\'
                || ch == 't'
                || ch == 'f'
                || ch == 'n'
                || ch == 'r'
                || ch == '0'
                || ch == '\"'
                || ch == '\''
                || ch == 'b') {
            length++;
        } else if (ch == 'u') {
            length++;
            for (int i = 0; i < 4; i++) {
                throwIfNeeded();
                if (!isDigit(charAt(offset + length))) {
                    return;
                }
                length++;
            }
        }
    }

    protected void scanStringLiteral() {
        if (offset + 1 >= bufferLen) {
            return;
        }
        char ch;

        while (offset + length < bufferLen && (ch = charAt(offset + length)) != '\"') {
            if (ch == '\\') {
                length++;
                scanTrans();
            } else {
                if (ch == '\n') {
                    return;
                }
                length++;
            }
        }

        if (offset + length < bufferLen) {
            length++;
        }
    }

    protected void scanCharLiteral() {
        if (offset + 1 >= bufferLen) {
            return;
        }
        char ch;
        while (offset + length < bufferLen && (ch = charAt(offset + length)) != '\'') {
            if (ch == '\\') {
                length++;
                scanTrans();
            } else {
                if (ch == '\n') {
                    return;
                }
                length++;
            }
        }
        if (offset + length != bufferLen) {
            length++;
        }
    }

    protected Tokens scanNumber() {
        if (offset + length == bufferLen) {
            return Tokens.NUMBER;
        }
        boolean flag = false;
        char ch = charAt(offset);
        if (ch == '0') {
            if (charAt() == 'x') {
                length++;
            }
            flag = true;
        }
        while (offset + length < bufferLen && isDigit(charAt())) {
            length++;
        }
        if (offset + length == bufferLen) {
            return Tokens.NUMBER;
        }
        ch = charAt();
        if (ch == '.') {
            if (flag) {
                return Tokens.NUMBER;
            }
            if (offset + length + 1 == bufferLen) {
                return Tokens.NUMBER;
            }
            length++;
            throwIfNeeded();
            while (offset + length < bufferLen && isDigit(charAt())) {
                length++;
            }
            if (offset + length == bufferLen) {
                return Tokens.NUMBER;
            }
            ch = charAt();
            if (ch == 'e' || ch == 'E') {
                length++;
                throwIfNeeded();
                if (charAt() == '-' || charAt() == '+') {
                    length++;
                    throwIfNeeded();
                }
                while (offset + length < bufferLen && isPrimeDigit(charAt())) {
                    length++;
                }
                if (offset + length == bufferLen) {
                    return Tokens.NUMBER;
                }
                ch = charAt();
            }
            if (ch == 'f' || ch == 'F' || ch == 'D' || ch == 'd') {
                length++;
            }
            return Tokens.NUMBER;
        } else if (ch == 'l' || ch == 'L') {
            length++;
            return Tokens.NUMBER;
        } else if (ch == 'F' || ch == 'f' || ch == 'D' || ch == 'd') {
            length++;
            return Tokens.NUMBER;
        } else {
            return Tokens.NUMBER;
        }
    }

    /* The following methods have been simplified for syntax high light */

    protected Tokens scanDIV() {
        if (offset + 1 >= bufferLen) {
            return Tokens.DIV;
        }
        char ch = charAt(offset);
        char nextChar = charAt(offset + 1);

        if (ch == '-' && nextChar == '-') { // Possible start of a comment
            // Check if this is the start of a multi-line comment
            if (offset + 2 < bufferLen && charAt(offset + 2) == '[' && charAt(offset + 3) == '[') {
                // Multi-line comment start
                length += 2; // Skip the '--[[' part
                boolean finished = false;
                while (offset + length < bufferLen) {
                    char currChar = charAt(offset + length);
                    //System.out.println(
                            //"Current char at offset " + offset + " length " + length + ": " + currChar);
                    if (currChar == ']' && charAt(offset + length + 1) == ']') {
                        length += 2; // Include the ']]' in the length
                        finished = true;
                        System.out.println("yes");
                        break;
                    }
                    length++;
                }
                return finished ? Tokens.LONG_COMMENT_COMPLETE : Tokens.LONG_COMMENT_INCOMPLETE;
            } else {
                // Single line comment
                while (offset + length < bufferLen && charAt(offset + length) != '\n') {
                    length++;
                }
                return Tokens.LINE_COMMENT;
            }
        }
        return Tokens.DIV; // Default return if none of the above conditions are met
    }

    public boolean isAssignment() {
        Tokens current = getToken();
        Tokens next = nextToken();
        if (current == Tokens.IDENTIFIER && next == Tokens.ASSGN) {
            return true;
        }
        pushBack(1); // 回退到上一个标记
        return false;
    }

    public String getVariableName() {
        if (getToken() == Tokens.IDENTIFIER) {
            return getTokenText().toString();
        }
        return null;
    }

    public String getVariableValue() {
        Tokens token = nextToken();
        if (token == Tokens.NUMBER || token == Tokens.STRING) {
            return getTokenText().toString();
        }
        pushBack(1); // 回退到上一个标记
        return null;
    }

    @SuppressWarnings("SameReturnValue")
    protected Tokens scanLT() {
        return Tokens.LT;
    }

    @SuppressWarnings("SameReturnValue")
    protected Tokens scanGT() {
        return Tokens.GT;
    }

    protected Tokens scanOperatorTwo(Tokens ifWrong) {
        return ifWrong;
    }

    public void reset(CharSequence src) {
        if (src == null) {
            throw new IllegalArgumentException();
        }
        this.source = src;
        line = 0;
        column = 0;
        length = 0;
        index = 0;
        offset = 0;
        currToken = Tokens.WHITESPACE;
        bufferLen = src.length();
    }

    protected static String[] sKeywords;

    protected static void doStaticInit() {
        sKeywords =
                new String[]{
                        "and",
                        "break",
                        "do",
                        "else",
                        "elseif",
                        "end",
                        "false",
                        "for",
                        "function",
                        "goto",
                        "if",
                        "in",
                        "local",
                        "nil",
                        "not",
                        "or",
                        "repeat",
                        "return",
                        "then",
                        "true",
                        "until",
                        "while",
                        "import",
                        "require",
                        "switch",
                        "case",
                        "try",
                        "catch"
                };

        Tokens[] sTokens = {
                Tokens.AND,
                Tokens.BREAK,
                Tokens.DO,
                Tokens.ELSE,
                Tokens.ELSEIF,
                Tokens.END,
                Tokens.FALSE,
                Tokens.FOR,
                Tokens.FUNCTION,
                Tokens.GOTO,
                Tokens.IF,
                Tokens.IN,
                Tokens.LOCAL,
                Tokens.NIL,
                Tokens.NOT,
                Tokens.OR,
                Tokens.REPEAT,
                Tokens.RETURN,
                Tokens.THEN,
                Tokens.TRUE,
                Tokens.UNTIL,
                Tokens.WHILE,
                Tokens.IMPORT,
                Tokens.REQUIRE,
                Tokens.SWITCH,
                Tokens.CASE,
                Tokens.TRY,
                Tokens.CATCH
        };
        keywords = new TrieTree<>();
        for (int i = 0; i < sKeywords.length; i++) {
            keywords.put(sKeywords[i], sTokens[i]);
        }
    }

    protected static boolean isDigit(char c) {
        return ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'));
    }

    protected static boolean isPrimeDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    protected static boolean isWhitespace(char c) {
        return (c == '\t' || c == ' ' || c == '\f' || c == '\n' || c == '\r');
    }
}
