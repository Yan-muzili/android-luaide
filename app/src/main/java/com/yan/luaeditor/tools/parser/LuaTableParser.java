package com.yan.luaeditor.tools.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuaTableParser {

    /**
     * @param input
     * @return
     * @throws IOException
     */
    public static HashMap<String, Object> luaTable2HashMap(String input) throws IOException {
        LuaLexer lexer = new LuaLexer(input);
        List<Token> tokens = lexer.tokenize();
        int index = 0;
        if (index < tokens.size() && tokens.get(index).type == Token.TokenType.PUNCTUATION && tokens.get(index).value.equals("{")) {
            index++;
        }

        return parseTable(tokens, index);
    }

    /**
     *
     * @param tokens
     * @param startIndex
     * @return
     */
    private static HashMap<String, Object> parseTable(List<Token> tokens, int startIndex) {
        HashMap<String, Object> map = new HashMap<>();
        int index = startIndex;
        int arrayIndex = 1;

        while (index < tokens.size()) {
            Token currentToken = tokens.get(index);

            if (currentToken.type == Token.TokenType.PUNCTUATION && currentToken.value.equals("}")) {
                break;
            }
            if (currentToken.value.equals(",")||currentToken.value.equals(";")||currentToken.type== Token.TokenType.NEWLINE||currentToken.type== Token.TokenType.WHITESPACE||currentToken.type==Token.TokenType.COMMENT||currentToken.type== Token.TokenType.EOF) {
                index++;
                continue;
            }



            String key;
            if (index + 1 < tokens.size() && tokens.get(index + 1).type == Token.TokenType.PUNCTUATION && tokens.get(index + 1).value.equals("=")) {
                key = currentToken.value;
                index += 2; // 跳过键和 '='
            } else {
                key = String.valueOf(arrayIndex++);
            }

            if (index < tokens.size()) {
                Token valueToken = tokens.get(index);
                if (valueToken.type == Token.TokenType.PUNCTUATION && valueToken.value.equals("{")) {
                    index++;
                    Map<String, Object> nestedMap = parseTable(tokens, index);
                    map.put(key, nestedMap);
                    while (index < tokens.size() &&!(tokens.get(index).type == Token.TokenType.PUNCTUATION && tokens.get(index).value.equals("}"))) {
                        index++;
                    }
                    index++;
                } else {
                    String value = valueToken.value;
                    map.put(key, value);
                    index++;
                }
            }

            if (index < tokens.size() && tokens.get(index).type == Token.TokenType.PUNCTUATION && (tokens.get(index).value.equals(",")||tokens.get(index).value.equals(";"))) {
                index++;
            }
        }

        return map;
    }


}