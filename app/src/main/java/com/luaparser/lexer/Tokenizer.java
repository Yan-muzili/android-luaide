package com.luaparser.lexer;

import com.luaparser.ParseOptions;
import java.util.*;
import java.util.regex.*;

/**
 * 完整的Lua词法分析器 - 对应tokens.lua的完整实现
 */
public class Tokenizer {
    private final String lua;
    private final ParseOptions options;
    private int position = 0;
    private int line = 1;
    private int column = 1;
    
    // Lua关键字
    private static final Set<String> KEYWORDS = Set.of(
        "and", "break", "do", "else", "elseif", "end", "false", "for",
        "function", "goto", "if", "in", "local", "nil", "not", "or", 
        "repeat", "return", "then", "true", "until", "while"
    );
    
    // 符号映射表
    private static final Map<String, TokenType> SYMBOL_MAP = new HashMap<>();
    static {
        SYMBOL_MAP.put("==", TokenType.SYMBOL_EQ);
        SYMBOL_MAP.put("~=", TokenType.SYMBOL_NE);
        SYMBOL_MAP.put("<=", TokenType.SYMBOL_LE);
        SYMBOL_MAP.put(">=", TokenType.SYMBOL_GE);
        SYMBOL_MAP.put("<<", TokenType.SYMBOL_SHL);
        SYMBOL_MAP.put(">>", TokenType.SYMBOL_SHR);
        SYMBOL_MAP.put("//", TokenType.SYMBOL_IDIV);
        SYMBOL_MAP.put("..", TokenType.SYMBOL_CONCAT);
        SYMBOL_MAP.put("...", TokenType.SYMBOL_DOTS);
        SYMBOL_MAP.put("::", TokenType.SYMBOL_LABEL);
        
        // 非标准符号
        SYMBOL_MAP.put("!=", TokenType.SYMBOL_NE_ALT);
        SYMBOL_MAP.put("&&", TokenType.SYMBOL_AND_ALT);
        SYMBOL_MAP.put("||", TokenType.SYMBOL_OR_ALT);
        SYMBOL_MAP.put("+=", TokenType.SYMBOL_ASSIGN_ADD);
        SYMBOL_MAP.put("-=", TokenType.SYMBOL_ASSIGN_SUB);
        SYMBOL_MAP.put("*=", TokenType.SYMBOL_ASSIGN_MUL);
        SYMBOL_MAP.put("/=", TokenType.SYMBOL_ASSIGN_DIV);
        SYMBOL_MAP.put("%=", TokenType.SYMBOL_ASSIGN_MOD);
        SYMBOL_MAP.put("^=", TokenType.SYMBOL_ASSIGN_POW);
        SYMBOL_MAP.put("//=", TokenType.SYMBOL_ASSIGN_IDIV);
        SYMBOL_MAP.put("&=", TokenType.SYMBOL_ASSIGN_AND);
        SYMBOL_MAP.put("|=", TokenType.SYMBOL_ASSIGN_OR);
        SYMBOL_MAP.put("<<=", TokenType.SYMBOL_ASSIGN_SHL);
        SYMBOL_MAP.put(">>=", TokenType.SYMBOL_ASSIGN_SHR);
    }
    
    public Tokenizer(String lua, ParseOptions options) {
        this.lua = lua != null ? lua : "";
        this.options = options != null ? options : new ParseOptions();
    }
    
    /**
     * 执行词法分析，返回Token列表
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        
        while (position < lua.length()) {
            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
            }
        }
        
        // 添加EOF标记
        tokens.add(new Token(TokenType.EOF, "", position, position, line, column));
        return tokens;
    }
    
    /**
     * 获取下一个Token
     */
    private Token nextToken() {
        if (position >= lua.length()) {
            return null;
        }
        
        int start = position;
        int startLine = line;
        int startColumn = column;
        
        char ch = lua.charAt(position);
        
        // 空白字符
        if (ch == ' ' || ch == '\t' || ch == '\f' ) {
            return parseSpace(start, startLine, startColumn);
        }
        
        // 换行符
        if (ch == '\r' || ch == '\n') {
            return parseNewline(start, startLine, startColumn);
        }
        
        // 注释
        if (ch == '-' && position + 1 < lua.length() && lua.charAt(position + 1) == '-') {
            return parseComment(start, startLine, startColumn);
        }
        
        // C风格注释 (非标准)
        if (ch == '/' && position + 1 < lua.length()) {
            char next = lua.charAt(position + 1);
            if (next == '/' && options.getNonstandardSymbols().contains("//")) {
                return parseCComment(start, startLine, startColumn);
            }
            if (next == '*') {
                return parseLongComment(start, startLine, startColumn);
            }
        }
        
        // 长字符串/长注释
        if (ch == '[') {
            Token longString = parseLongString(start, startLine, startColumn);
            if (longString != null) {
                return longString;
            }
        }
        
        // 短字符串
        if (ch == '"' || ch == '\'' || ch == '`') {
            return parseShortString(start, startLine, startColumn, ch);
        }
        
        // 数字
        if (Character.isDigit(ch) || (ch == '.' && position + 1 < lua.length() && Character.isDigit(lua.charAt(position + 1)))) {
            return parseNumber(start, startLine, startColumn);
        }
        
        // 标识符/关键字
        if (Character.isLetter(ch) || ch == '_' || (options.isUnicodeName() && ch > 127)) {
            return parseWord(start, startLine, startColumn);
        }
        
        // 符号
        return parseSymbol(start, startLine, startColumn);
    }
    
    /**
     * 解析空白字符
     */
    private Token parseSpace(int start, int startLine, int startColumn) {
        StringBuilder content = new StringBuilder();
        
        while (position < lua.length()) {
            char ch = lua.charAt(position);
            if (ch == ' ' || ch == '\t' || ch == '\f' ) {
                content.append(ch);
                advance();
            } else {
                break;
            }
        }
        
        return new Token(TokenType.SPACE, content.toString(), start, position - 1, startLine, startColumn);
    }
    
    /**
     * 解析换行符
     */
    private Token parseNewline(int start, int startLine, int startColumn) {
        StringBuilder content = new StringBuilder();
        
        char ch = lua.charAt(position);
        if (ch == '\r') {
            content.append(ch);
            advance();
            if (position < lua.length() && lua.charAt(position) == '\n') {
                content.append('\n');
                advance();
            }
        } else if (ch == '\n') {
            content.append(ch);
            advance();
        }
        
        line++;
        column = 1;
        
        return new Token(TokenType.NEWLINE, content.toString(), start, position - 1, startLine, startColumn);
    }
    
    /**
     * 解析注释
     */
    private Token parseComment(int start, int startLine, int startColumn) {
        advance(); // 跳过第一个 -
        advance(); // 跳过第二个 -
        
        // 检查是否是长注释
        if (position < lua.length() && lua.charAt(position) == '[') {
            Token longComment = parseLongString(start, startLine, startColumn);
            if (longComment != null) {
                return new Token(TokenType.COMMENT_LONG, longComment.getContent(), 
                               start, longComment.getFinish(), startLine, startColumn);
            }
        }
        
        // 短注释
        StringBuilder content = new StringBuilder();
        while (position < lua.length()) {
            char ch = lua.charAt(position);
            if (ch == '\r' || ch == '\n') {
                break;
            }
            content.append(ch);
            advance();
        }
        
        return new Token(TokenType.COMMENT_SHORT, content.toString(), 
                        start, position - 1, startLine, startColumn);
    }
    
    /**
     * 解析C风格单行注释
     */
    private Token parseCComment(int start, int startLine, int startColumn) {
        advance(); // 跳过第一个 /
        advance(); // 跳过第二个 /
        
        StringBuilder content = new StringBuilder();
        while (position < lua.length()) {
            char ch = lua.charAt(position);
            if (ch == '\r' || ch == '\n') {
                break;
            }
            content.append(ch);
            advance();
        }
        
        return new Token(TokenType.COMMENT_CSHORT, content.toString(), 
                        start, position - 1, startLine, startColumn);
    }
    
    /**
     * 解析C风格块注释
     */
    private Token parseLongComment(int start, int startLine, int startColumn) {
        advance(); // 跳过 /
        advance(); // 跳过 *
        
        StringBuilder content = new StringBuilder();
        while (position + 1 < lua.length()) {
            if (lua.charAt(position) == '*' && lua.charAt(position + 1) == '/') {
                advance(); // 跳过 *
                advance(); // 跳过 /
                break;
            }
            
            char ch = lua.charAt(position);
            if (ch == '\n') {
                line++;
                column = 1;
            }
            content.append(ch);
            advance();
        }
        
        return new Token(TokenType.COMMENT_LONG, content.toString(), 
                        start, position - 1, startLine, startColumn);
    }
    
    /**
     * 解析长字符串
     */
    private Token parseLongString(int start, int startLine, int startColumn) {
        if (position >= lua.length() || lua.charAt(position) != '[') {
            return null;
        }
        
        // 计算等号数量
        int equalCount = 0;
        int pos = position + 1;
        while (pos < lua.length() && lua.charAt(pos) == '=') {
            equalCount++;
            pos++;
        }
        
        if (pos >= lua.length() || lua.charAt(pos) != '[') {
            return null;
        }
        
        // 构建结束标记
        StringBuilder endMark = new StringBuilder("]");
        for (int i = 0; i < equalCount; i++) {
            endMark.append("=");
        }
        endMark.append("]");
        
        // 跳过开始标记
        position = pos + 1;
        column += 2 + equalCount;
        
        // 跳过第一个换行符（如果存在）
        if (position < lua.length() && lua.charAt(position) == '\n') {
            advance();
            line++;
            column = 1;
        } else if (position + 1 < lua.length() && 
                   lua.charAt(position) == '\r' && lua.charAt(position + 1) == '\n') {
            advance();
            advance();
            line++;
            column = 1;
        }
        
        StringBuilder content = new StringBuilder();
        String endMarkStr = endMark.toString();
        
        while (position < lua.length()) {
            if (position + endMarkStr.length() <= lua.length() && 
                lua.substring(position, position + endMarkStr.length()).equals(endMarkStr)) {
                // 找到结束标记
                position += endMarkStr.length();
                column += endMarkStr.length();
                break;
            }
            
            char ch = lua.charAt(position);
            if (ch == '\n') {
                line++;
                column = 1;
            }
            content.append(ch);
            advance();
        }
        
        return new Token(TokenType.STRING_LONG, content.toString(), 
                        start, position - 1, startLine, startColumn);
    }
    
    /**
     * 解析短字符串
     */
    private Token parseShortString(int start, int startLine, int startColumn, char quote) {
        advance(); // 跳过开始引号
        
        StringBuilder content = new StringBuilder();
        while (position < lua.length()) {
            char ch = lua.charAt(position);
            
            if (ch == quote) {
                advance(); // 跳过结束引号
                break;
            }
            
            if (ch == '\r' || ch == '\n') {
                // 字符串不能跨行
                break;
            }
            
            if (ch == '\\' && position + 1 < lua.length()) {
                advance(); // 跳过反斜杠
                char escaped = lua.charAt(position);
                content.append(getEscapedChar(escaped));
                advance();
            } else {
                content.append(ch);
                advance();
            }
        }
        
        return new Token(TokenType.STRING_SHORT, content.toString(), 
                        start, position - 1, startLine, startColumn);
    }
    
    /**
     * 解析数字
     */
    private Token parseNumber(int start, int startLine, int startColumn) {
        StringBuilder content = new StringBuilder();
        boolean isInteger = true;
        
        // 处理十六进制数
        if (position + 1 < lua.length() && lua.charAt(position) == '0' && 
            (lua.charAt(position + 1) == 'x' || lua.charAt(position + 1) == 'X')) {
            
            content.append(lua.charAt(position)); advance();
            content.append(lua.charAt(position)); advance();
            
            while (position < lua.length() && isHexDigit(lua.charAt(position))) {
                content.append(lua.charAt(position));
                advance();
            }
            
            // 十六进制小数部分
            if (position < lua.length() && lua.charAt(position) == '.') {
                isInteger = false;
                content.append(lua.charAt(position));
                advance();
                while (position < lua.length() && isHexDigit(lua.charAt(position))) {
                    content.append(lua.charAt(position));
                    advance();
                }
            }
            
            // 十六进制指数部分
            if (position < lua.length() && 
                (lua.charAt(position) == 'p' || lua.charAt(position) == 'P')) {
                isInteger = false;
                content.append(lua.charAt(position));
                advance();
                if (position < lua.length() && 
                    (lua.charAt(position) == '+' || lua.charAt(position) == '-')) {
                    content.append(lua.charAt(position));
                    advance();
                }
                while (position < lua.length() && Character.isDigit(lua.charAt(position))) {
                    content.append(lua.charAt(position));
                    advance();
                }
            }
        } else {
            // 十进制数
            while (position < lua.length() && Character.isDigit(lua.charAt(position))) {
                content.append(lua.charAt(position));
                advance();
            }
            
            // 小数部分
            if (position < lua.length() && lua.charAt(position) == '.') {
                isInteger = false;
                content.append(lua.charAt(position));
                advance();
                while (position < lua.length() && Character.isDigit(lua.charAt(position))) {
                    content.append(lua.charAt(position));
                    advance();
                }
            }
            
            // 指数部分
            if (position < lua.length() && 
                (lua.charAt(position) == 'e' || lua.charAt(position) == 'E')) {
                isInteger = false;
                content.append(lua.charAt(position));
                advance();
                if (position < lua.length() && 
                    (lua.charAt(position) == '+' || lua.charAt(position) == '-')) {
                    content.append(lua.charAt(position));
                    advance();
                }
                while (position < lua.length() && Character.isDigit(lua.charAt(position))) {
                    content.append(lua.charAt(position));
                    advance();
                }
            }
        }
        
        TokenType type = isInteger ? TokenType.INTEGER : TokenType.NUMBER;
        return new Token(type, content.toString(), start, position - 1, startLine, startColumn);
    }
    
    /**
     * 解析标识符/关键字
     */
    private Token parseWord(int start, int startLine, int startColumn) {
        StringBuilder content = new StringBuilder();
        
        while (position < lua.length()) {
            char ch = lua.charAt(position);
            if (Character.isLetterOrDigit(ch) || ch == '_' || 
                (options.isUnicodeName() && ch > 127)) {
                content.append(ch);
                advance();
            } else {
                break;
            }
        }
        
        return new Token(TokenType.WORD, content.toString(), 
                        start, position - 1, startLine, startColumn);
    }
    
    /**
     * 解析符号
     */
    private Token parseSymbol(int start, int startLine, int startColumn) {
        // 尝试匹配多字符符号（从长到短）
        for (int len = 3; len >= 1; len--) {
            if (position + len <= lua.length()) {
                String symbol = lua.substring(position, position + len);
                TokenType type = SYMBOL_MAP.get(symbol);
                
                if (type != null) {
                    // 检查非标准符号是否被允许
                    if (isNonstandardSymbol(symbol) && !options.getNonstandardSymbols().contains(symbol)) {
                        continue;
                    }
                    
                    for (int i = 0; i < len; i++) {
                        advance();
                    }
                    return new Token(type, symbol, start, position - 1, startLine, startColumn);
                }
            }
        }
        
        // 单字符符号
        char ch = lua.charAt(position);
        advance();
        return new Token(TokenType.SYMBOL, String.valueOf(ch), 
                        start, position - 1, startLine, startColumn);
    }
    
    /**
     * 检查是否是非标准符号
     */
    private boolean isNonstandardSymbol(String symbol) {
        return Set.of("!=", "&&", "||", "+=", "-=", "*=", "/=", "%=", "^=", 
                     "//=", "&=", "|=", "<<=", ">>=").contains(symbol);
    }
    
    /**
     * 前进一个字符
     */
    private void advance() {
        if (position < lua.length()) {
            position++;
            column++;
        }
    }
    
    /**
     * 检查是否是十六进制数字
     */
    private boolean isHexDigit(char ch) {
        return Character.isDigit(ch) || 
               (ch >= 'a' && ch <= 'f') || 
               (ch >= 'A' && ch <= 'F');
    }
    
    /**
     * 获取转义字符
     */
    private char getEscapedChar(char ch) {
        return switch (ch) {
            case 'a' -> '\u0007'; // bell
            case 'b' -> '\b';     // backspace
            case 'f' -> '\f';     // form feed
            case 'n' -> '\n';     // newline
            case 'r' -> '\r';     // carriage return
            case 't' -> '\t';     // tab
            case 'v' -> '\u000B'; // vertical tab
            case '\\' -> '\\';
            case '\'' -> '\'';
            case '"' -> '"';
            case '0' -> '\0';     // null
            default -> ch;
        };
    }
}
