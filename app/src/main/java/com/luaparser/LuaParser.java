package com.luaparser;

import com.luaparser.ast.*;
import com.luaparser.lexer.*;
import com.luaparser.parser.*;
import com.luaparser.parser.Compiler;
import com.luaparser.utils.*;
import com.luaparser.doc.*;
import com.luaparser.guide.*;
import java.util.*;

/**
 * 完整的Lua解析器主入口 - 对应init.lua
 */
public class LuaParser {
    
    public static class ParseResult {
        private final ASTNode ast;
        private final LineTracker lines;
        private final LuaDocParser.DocResult docs;
        private final List<ParseError> errors;
        
        public ParseResult(ASTNode ast, LineTracker lines, LuaDocParser.DocResult docs, List<ParseError> errors) {
            this.ast = ast;
            this.lines = lines;
            this.docs = docs;
            this.errors = errors;
        }
        
        public ASTNode getAst() { return ast; }
        public LineTracker getLines() { return lines; }
        public LuaDocParser.DocResult getDocs() { return docs; }
        public List<ParseError> getErrors() { return errors; }
    }
    
    public static class API {
        private final Compiler compiler;
        private final Guide guide;
        private final LuaDocParser luadoc;
        
        public API() {
            this.compiler = new Compiler();
            this.guide = new Guide();
            this.luadoc = new LuaDocParser();
        }
        
        public ParseState compile(String lua) {
            return compile(lua, "Lua", "Lua 5.4", new ParseOptions());
        }
        
        public ParseState compile(String lua, String mode, String version, ParseOptions options) {
            return compiler.compile(lua, mode, version, options);
        }
        
        public int[] calculateLines(String text) {
            LineTracker tracker = new LineTracker();
            return tracker.calculateLines(text);
        }
        
        public Guide getGuide() { return guide; }
        public LuaDocParser getLuaDoc() { return luadoc; }
    }
    
    /**
     * 解析Lua源代码
     */
    public static ParseResult parse(String luaSource) {
        return parse(luaSource, "Lua 5.4", new ParseOptions());
    }
    
    public static ParseResult parse(String luaSource, String version, ParseOptions options) {
        // 创建行跟踪器
        LineTracker lines = new LineTracker();
        // 然后调用calculateLines方法
        lines.calculateLines(luaSource);
        
        // 词法分析
        Tokenizer tokenizer = new Tokenizer(luaSource, options);
        List<Token> tokens = tokenizer.tokenize();
        
        // 语法分析
        Parser parser = new Parser(tokens, version, options);
        ASTNode ast = parser.parse();
        
        // 文档解析
        LuaDocParser docParser = new LuaDocParser();
        LuaDocParser.DocResult docs = docParser.parseDocumentation(ast, tokens);
        
        return new ParseResult(ast, lines, docs, parser.getErrors());
    }
    
    public static API create() {
        return new API();
    }
}
