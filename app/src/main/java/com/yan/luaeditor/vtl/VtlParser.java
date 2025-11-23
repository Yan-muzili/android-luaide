package com.yan.luaeditor.vtl;

import android.content.Context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Stack;

public class VtlParser {
    private final List<Token> tokens;
    private int idx = 0;

    /* 记录尚未关闭的 [ 的栈：元素为 {tag, line, col} */
    private static class BracketFrame {
        String tag; int line; int col;
        BracketFrame(String tag,int l,int c){this.tag=tag;this.line=l;this.col=c;}
    }
    private final Deque<BracketFrame> bracketStack = new ArrayDeque<>();

    public VtlParser(List<Token> tokens){ this.tokens = tokens; }

    private Token current(){ return idx<tokens.size()?tokens.get(idx):eof(); }
    private Token eof(){ return new Token(TokenType.EOF,"",-1,-1); }

    private void expect(TokenType type){
        if(current().type!=type)
            throw new RuntimeException("Expected "+type+" but got "+current());
    }

    /* ========= 公开入口 ========= */
    public VNode parse(){
        VNode root = parseView();
        if(!bracketStack.isEmpty()){
            BracketFrame f = bracketStack.pop();
            throw new RuntimeException("Unclosed '[' for view '"+f.tag+"' at "+f.line+":"+f.col);
        }
        return root;
    }

    public VNode parseView(){
        expect(TokenType.VIEW);
        String tag = current().string;
        int tagLine = current().line;
        int tagCol  = current().col;

        VNode node = new VNode(tag);
        idx++;
        expect(TokenType.LBRACKET);
        idx++;

        bracketStack.push(new BracketFrame(tag, tagLine, tagCol));

        while(true){
            Token tk = current();
            if(tk.type==TokenType.EOF){
                BracketFrame f = bracketStack.pop();
                throw new RuntimeException("Unexpected EOF: unclosed '[' for view '"+f.tag+"' at "+f.line+":"+f.col);
            }
            if(tk.type==TokenType.RBRACKET){
                idx++;
                bracketStack.pop();
                return node;
            }

            if(tk.type==TokenType.RBRACKET && bracketStack.isEmpty()){
                throw new RuntimeException("Unexpected ']' at "+tk.line+":"+tk.col);
            }

            if(tk.type==TokenType.VIEW){
                node.children.add(parseView());
            }else if(tk.type==TokenType.ATTRS){
                String key = tk.string;
                idx++;
                expect(TokenType.STRING);
                String val = current().string;
                node.attrs.put(key,val);
                idx++;
            }else{
                throw new RuntimeException("Unexpected token "+tk);
            }
        }
    }
}
