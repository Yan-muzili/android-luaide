package com.yan.luaeditor.vtl;

import java.util.ArrayList;
import java.util.List;

public class VtlLexer {
        private final String input;
        private int pos = 0;
        private int line = 1;
        private int col = 1;

        public VtlLexer(String s) { this.input = s; }

        private char chatAt() { return pos < input.length() ? input.charAt(pos) : '\0'; }

        private void advance() {
            if (pos < input.length()) {
                if (input.charAt(pos) == '\n') { line++; col = 1; }
                else col++;
                pos++;
            }
        }

        private void skip() {
            while (Character.isWhitespace(chatAt())) advance();
        }

        public List<Token> tokenize() {
            List<Token> tokens = new ArrayList<>();
            while (true) {
                skip();
                char ch = chatAt();
                if (ch == '\0') {
                    tokens.add(new Token(TokenType.EOF, "", line, col));
                    break;
                }

                int startLine = line, startCol = col;

                if (Character.isJavaIdentifierStart(ch)) {
                    StringBuilder sb = new StringBuilder();
                    while (pos < input.length() &&
                            (Character.isJavaIdentifierPart(chatAt()) || chatAt() == '.')) {
                        sb.append(chatAt());
                        advance();
                    }
                    TokenType type = (chatAt() == '[') ? TokenType.VIEW : TokenType.ATTRS;
                    tokens.add(new Token(type, sb.toString(), startLine, startCol));
                } else if (ch == '[') {
                    tokens.add(new Token(TokenType.LBRACKET, "[", startLine, startCol));
                    advance();
                } else if (ch == ']') {
                    tokens.add(new Token(TokenType.RBRACKET, "]", startLine, startCol));
                    advance();
                } else if (ch == '"' || ch == '\'') {
                    char quote = ch;
                    advance();
                    StringBuilder sb = new StringBuilder();
                    while (chatAt() != quote && chatAt() != '\0') {
                        if (chatAt() == '\\') {
                            advance();
                            if (chatAt() != '\0') {
                                sb.append(chatAt());
                                advance();
                            }
                        } else {
                            sb.append(chatAt());
                            advance();
                        }
                    }
                    if (chatAt() == quote) advance();
                    tokens.add(new Token(TokenType.STRING, sb.toString(), startLine, startCol));
                } else {
                    throw new RuntimeException("Unexpected character '" + ch +
                            "' at line " + line + ", col " + col);
                }
            }
            return tokens;
        }

}
