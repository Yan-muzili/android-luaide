package com.yan.luaeditor.tools.parser;

import java.util.ArrayList;
import java.util.List;

public class LuaImportParser {
    List<Token> tokens;
    public LuaImportParser(List<Token> tokens){
        this.tokens=tokens;
    }
    public List<String> parseImportAndRequire() {
        List<String> list=new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token currentToken = tokens.get(i);
            if (currentToken.type == Token.TokenType.KEYWORD &&
                    (currentToken.value.equals("import") || currentToken.value.equals("require"))) {
                i++;
                if (i < tokens.size() && tokens.get(i).type == Token.TokenType.STRING) {
                    String modulePath = tokens.get(i).value;
                    System.out.println(" module: " + modulePath);
                    list.add(modulePath);
                }
            }
        }
        return list;
    }
}
