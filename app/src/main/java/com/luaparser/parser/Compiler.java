package com.luaparser.parser;

import com.luaparser.lexer.*;
import com.luaparser.ast.*;
import com.luaparser.ParseOptions;
import com.luaparser.utils.LineTracker;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 编译器主类 - 完整对应compile.lua
 */
public class Compiler {
    
    /**
     * 编译Lua源代码
     */
    public ParseState compile(String lua, String mode, String version, ParseOptions options) {
        // 创建解析状态
        ParseState state = new ParseState();
        state.setVersion(version);
        state.setLua(lua);
        state.setOptions(options);
        
        try {
            // 词法分析
            Tokenizer tokenizer = new Tokenizer(lua, options);
            List<Token> tokens = tokenizer.tokenize();
            
            // 语法分析
            Parser parser = new Parser(tokens, version, options);
            ASTNode ast = parser.parse();
            
            state.setAst(ast);
            state.setErrors(parser.getErrors());
            
            // 计算行信息
            LineTracker lineTracker = new LineTracker();
            state.setLines(lineTracker.calculateLines(lua));
            
            // 提取注释
            List<Object> comments = tokens.stream()
                .filter(Token::isComment)
                .collect(Collectors.toList()); // 使用collect而不是toList()
            state.setComments(comments);
            
        } catch (Exception e) {
            // 处理编译异常
            ParseError error = new ParseError("COMPILE_ERROR", 0, 0, e.getMessage(), e);
            state.setErrors(List.of(error));
        }
        
        return state;
    }
    
    /**
     * 快速编译（仅词法分析）
     */
    public ParseState tokenize(String lua, ParseOptions options) {
        ParseState state = new ParseState();
        state.setLua(lua);
        state.setOptions(options);
        
        try {
            Tokenizer tokenizer = new Tokenizer(lua, options);
            List<Token> tokens = tokenizer.tokenize();
            
            // 创建简单的AST根节点
            ASTNode ast = new ASTNode("main");
            state.setAst(ast);
            
            // 提取注释
            List<Object> comments = tokens.stream()
                .filter(Token::isComment)
                .collect(Collectors.toList()); // 使用collect而不是toList()
            state.setComments(comments);
            
            // 计算行信息
            LineTracker lineTracker = new LineTracker();
            state.setLines(lineTracker.calculateLines(lua));
            
        } catch (Exception e) {
            ParseError error = new ParseError("TOKENIZE_ERROR", 0, 0, e.getMessage(), e);
            state.setErrors(List.of(error));
        }
        
        return state;
    }
}
