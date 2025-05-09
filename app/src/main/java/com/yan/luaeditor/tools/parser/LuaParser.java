package com.yan.luaeditor.tools.parser;

import com.yan.luaide.LuaUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuaParser {
    private final List<Token> tokens;

    public LuaParser(List<Token> tokens) {
        this.tokens = tokens;
    }
    public Map<String, String> parseVariables() {
        Map<String, String> variableMap = new HashMap<>();
        try {
            for (int i = 0; i < tokens.size (); i++) {
                Token currentToken = tokens.get (i);
                if (currentToken.type == Token.TokenType.IDENTIFIER) {
                    StringBuilder variableNameBuilder = new StringBuilder (currentToken.value);
                    int j = i + 1;
// 处理带点号的索引情况
                    while (j < tokens.size () && tokens.get (j).type == Token.TokenType.PUNCTUATION && tokens.get (j).value.equals (".")) {
                        if (j + 1 < tokens.size () && tokens.get (j + 1).type == Token.TokenType.IDENTIFIER) {
                            variableNameBuilder.append (".").append (tokens.get (j + 1).value);
                            j += 2;
                        } else {
                            break;
                        }
                    }
                    String variableName = variableNameBuilder.toString ();
                    if (j < tokens.size () && tokens.get (j).type == Token.TokenType.PUNCTUATION && tokens.get (j).value.equals ("=")) {
                        StringBuilder valueBuilder = new StringBuilder ();
                        int parenCount = 0;
                        int braceCount = 0;
                        int bracketCount = 0;
                        int funcCount = 0;
                        j++;
                        while (j < tokens.size () && tokens.get (j).type != Token.TokenType.EOF) {
                            Token valueToken = tokens.get (j);
                            if (valueToken.type == Token.TokenType.PUNCTUATION) {
                                if (valueToken.value.equals (",")) {
                                    if (parenCount == 0 && braceCount == 0 && bracketCount == 0 && funcCount == 0) {
                                        break;
                                    }
                                }
                            } else if (valueToken.type == Token.TokenType.KEYWORD) {
                                if (valueToken.value.equals ("function")) {
                                    funcCount++;
                                } else if (valueToken.value.equals ("end")) {
                                    if (funcCount > 0) {
                                        funcCount--;
                                    }
                                }
                            }
                            if (parenCount == 0 && braceCount == 0 && bracketCount == 0) {
                                if (valueToken.type != Token.TokenType.PUNCTUATION || (valueToken.type == Token.TokenType.PUNCTUATION && valueToken.value != "(" && valueToken.value != ")")) {
                                    valueBuilder.append (valueToken.value);
                                }
                            }
                            j++;
                        }
                        String value = valueBuilder.toString ();
                        variableMap.put (variableName, new LuaParser(tokens).filterParentheses(value));
                        i = j - 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return variableMap;
    }
    public List<String> parseImports() {
        List<String> importList = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token currentToken = tokens.get(i);
            if (currentToken.type == Token.TokenType.IDENTIFIER && (currentToken.value.equals("import") || currentToken.value.equals("require"))) {
                if (i + 1 < tokens.size() && tokens.get(i + 1).type == Token.TokenType.STRING) {
                    String importStr = tokens.get(i + 1).value;
                    if (importStr.startsWith("\"") && importStr.endsWith("\"")) {
                        importStr = importStr.substring(1, importStr.length() - 1);
                    }
                    importList.add(importStr);
                    i++;
                }
            }
        }
        return importList;
    }

    public String filterParentheses(String input) {
        StringBuilder result = new StringBuilder();
        int depth = 0;
        for (char c : input.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                depth++;
            } else if (c == ')' || c == ']' || c == '}') {
                if (depth > 0) {
                    depth--;
                }
            } else if (depth == 0) {
                result.append(c);
            }
        }
        return result.toString();
    }


}