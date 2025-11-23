package com.yan.luaeditor.tools.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParserLuaLayout {
    HashMap<String,String> parsermap=new HashMap<>();

    /**
     *
     * @param t
     * @return
     */
    public HashMap<String,String> parser(HashMap<String,Object> t){
        for (String rootkey:t.keySet()){

            if (t.get(rootkey) instanceof String){
                //System.out.println(rootkey);
                if(rootkey.equals("id")) {
                    //System.out.println(rootkey);
                    if (((String) t.get(rootkey)).startsWith("\"") && ((String) t.get(rootkey)).endsWith("\"")) {
                        t.put(rootkey,((String) t.get(rootkey)).substring(1, ((String) t.get(rootkey)).length() - 1));
                    } else if (((String) t.get(rootkey)).startsWith("'") && ((String) t.get(rootkey)).endsWith("'")) {
                        t.put(rootkey,((String) t.get(rootkey)).substring(1, ((String) t.get(rootkey)).length() - 1));
                    }
                    parsermap.put(((String) t.get(rootkey)), (String) t.get("1"));
                }
            }
            if (t.get(rootkey) instanceof HashMap<?,?>){
                parser((HashMap<String, Object>) t.get(rootkey));
            }
        }
        return parsermap;
    }

    /**
     *
     * @param tokens
     * @return
     */
    public List<String> parseLoadLayout(List<Token> tokens) {
        List<String> alyList = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token currentToken = tokens.get(i);
            if (currentToken.type == Token.TokenType.IDENTIFIER && (currentToken.value.equals("loadlayout"))) {
                //System.out.println(tokens.get(i + 2).value);
                if (i + 2 < tokens.size() && tokens.get(i + 2).type == Token.TokenType.STRING) {
                    String Str = tokens.get(i + 2).value;
                    if (Str.startsWith("\"") && Str.endsWith("\"")) {
                        Str = Str.substring(1, Str.length() - 1);
                    }
                    alyList.add(Str);
                    i++;
                } else if (i + 2 < tokens.size() && tokens.get(i + 2).type == Token.TokenType.IDENTIFIER) {
                    String Str = tokens.get(i + 2).value;
                    alyList.add(Str);
                    i++;
                }
            }
        }
        return alyList;
    }
    public void clearMap(){
        parsermap=new HashMap<>();
    }
}
