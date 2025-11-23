package com.yan.luaeditor.tools.parser;

import com.yan.luaide.LuaUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class LuaParser {
    private final List<Token> tokens;

    public LuaParser(List<Token> tokens) {
        this.tokens = tokens;
    }
    public Map<String, String> parseVariables() {
        return new HashMap<>();
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