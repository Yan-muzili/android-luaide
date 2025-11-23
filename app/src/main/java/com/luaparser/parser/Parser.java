package com.luaparser.parser;

import com.luaparser.lexer.*;
import com.luaparser.ast.*;
import com.luaparser.ParseOptions;

import java.util.*;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * 完整的Lua语法分析器 - 对应compile.lua的完整实现
 */
public class Parser {
    private final List<Token> tokens;
    private final String version;
    private final ParseOptions options;
    private int index = 0;
    private int localCount = 0;
    private boolean localLimited = false;
    private final List<ASTNode> chunkStack = new ArrayList<>();
    private final List<ParseError> errors = new ArrayList<>();

    private static final int LOCAL_LIMIT = 200;

    // Lua关键字
    private static final Set<String> KEYWORDS = Set.of(
            "and", "break", "do", "else", "elseif", "end", "false", "for",
            "function", "goto", "if", "in", "local", "nil", "not", "or",
            "repeat", "return", "then", "true", "until", "while"
    );

    // 特殊函数
    private static final Set<String> SPECIALS = Set.of(
            "_G", "rawset", "rawget", "setmetatable", "require", "dofile",
            "loadfile", "pcall", "xpcall", "pairs", "ipairs", "assert",
            "error", "type", "os.exit"
    );

    // 一元操作符优先级
    private static final Map<String, Integer> UNARY_PRIORITY;

    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("not", 11);
        map.put("#", 11);
        map.put("~", 11);
        map.put("-", 11);
        map.put("!", 11);
        UNARY_PRIORITY = Collections.unmodifiableMap(map);
    }

    // 二元操作符优先级
    private static final Map<String, Integer> BINARY_PRIORITY;

    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("or", 1);
        map.put("and", 2);
        map.put("<=", 3);
        map.put(">=", 3);
        map.put("<", 3);
        map.put(">", 3);
        map.put("~=", 3);
        map.put("==", 3);
        map.put("|", 4);
        map.put("~", 5);
        map.put("&", 6);
        map.put("<<", 7);
        map.put(">>", 7);
        map.put("..", 8);
        map.put("+", 9);
        map.put("-", 9);
        map.put("*", 10);
        map.put("//", 10);
        map.put("/", 10);
        map.put("%", 10);
        map.put("^", 12);
        map.put("||", 1);
        map.put("&&", 2);
        map.put("!=", 3);
        BINARY_PRIORITY = Collections.unmodifiableMap(map);
    }

    // 操作符别名
    private static final Map<String, String> BINARY_ALIAS;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("&&", "and");
        map.put("||", "or");
        map.put("!=", "~=");
        BINARY_ALIAS = Collections.unmodifiableMap(map);
    }

    private static final Map<String, String> UNARY_ALIAS;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("!", "not");
        UNARY_ALIAS = Collections.unmodifiableMap(map);
    }

    public Parser(List<Token> tokens, String version, ParseOptions options) {
        this.tokens = tokens != null ? tokens : new ArrayList<>();
        this.version = version != null ? version : "Lua 5.4";
        this.options = options != null ? options : new ParseOptions();
    }

    /**
     * 执行语法分析
     */
    public ASTNode parse() {
        return parseLua();
    }

    /**
     * 解析主程序
     */
    private ASTNode parseLua() {
        ASTNode main = new ASTNode("main");
        main.setStart(0);
        main.setFinish(0);
        main.setBstart(0);

        pushChunk(main);

        // 创建_ENV局部变量
        createEnvLocal(main);

        localCount = 0;
        skipFirstComment();

        // 解析语句序列
        while (index < tokens.size() && !isEOF()) {
            parseActions(main);
            if (index < tokens.size() && !isEOF()) {
                if (!skipUnknownSymbol()) {
                    break;
                }
            }
        }

        popChunk();

        // 设置结束位置
        if (!tokens.isEmpty()) {
            Token lastToken = tokens.get(tokens.size() - 1);
            main.setFinish(lastToken.getFinish());
            main.setBfinish(main.getFinish());
        }

        return main;
    }

    /**
     * 创建_ENV局部变量
     */
    private void createEnvLocal(ASTNode main) {
        ASTNode envLocal = new ASTNode("local", -1, -1);
        envLocal.setEffect(-1);
        envLocal.setTag("_ENV");
        envLocal.setSpecial("_G");
        envLocal.setAttribute("name", getENVMode());

        main.addChild(envLocal);
    }

    /**
     * 获取环境变量模式
     */
    private String getENVMode() {
        return "Lua 5.1".equals(version) || "LuaJIT".equals(version) ? "@fenv" : "_ENV";
    }

    /**
     * 跳过第一行注释（如shebang）
     */
    private void skipFirstComment() {
        if (index < tokens.size()) {
            Token token = tokens.get(index);
            if ("#".equals(token.getContent())) {
                while (index < tokens.size()) {
                    token = tokens.get(index);
                    index++;
                    if (token.getType() == TokenType.NEWLINE) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * 解析动作序列
     */
    private void parseActions(ASTNode parent) {
        ASTNode lastReturn = null;
        ASTNode lastAction = null;

        while (index < tokens.size() && !isEOF()) {
            skipSpace(true);

            if (index >= tokens.size() || isEOF()) break;

            Token token = tokens.get(index);

            // 分号
            if (";".equals(token.getContent())) {
                index++;
                continue;
            }

            // 检查块结束标记
            if (isChunkFinishToken(token.getContent())) {
                break;
            }

            ASTNode action = parseAction();
            if (action == null) {
                if (!skipUnknownSymbol()) {
                    break;
                }
                continue;
            }

            parent.addChild(action);

            if ("return".equals(action.getType()) && lastReturn == null) {
                lastReturn = action;
            }

            lastAction = action;
        }

        // 检查return后是否还有语句
        if (lastReturn != null && lastReturn != lastAction) {
            pushError(new ParseError("ACTION_AFTER_RETURN",
                    lastReturn.getStart(), lastReturn.getFinish()));
        }
    }

    /**
     * 解析单个动作
     */
    private ASTNode parseAction() {
        if (index >= tokens.size() || isEOF()) return null;

        Token token = tokens.get(index);
        String content = token.getContent();

        // 标签
        if ("::".equals(content)) {
            return parseLabel();
        }

        // 局部变量声明
        if ("local".equals(content)) {
            return parseLocal();
        }

        // 条件语句
        if ("if".equals(content)) {
            return parseIf();
        }

        // 循环语句
        if ("for".equals(content)) {
            return parseFor();
        }

        if ("while".equals(content)) {
            return parseWhile();
        }

        if ("repeat".equals(content)) {
            return parseRepeat();
        }

        // 控制语句
        if ("do".equals(content)) {
            return parseDo();
        }

        if ("return".equals(content)) {
            return parseReturn();
        }

        if ("break".equals(content)) {
            return parseBreak();
        }

        if ("continue".equals(content) && options.getNonstandardSymbols().contains("continue")) {
            return parseBreak(); // continue作为break处理
        }

        if ("goto".equals(content) && isKeyword("goto")) {
            return parseGoto();
        }

        // 函数定义
        if ("function".equals(content)) {
            return parseFunction(false, true);
        }

        // 表达式语句
        ASTNode exp = parseExp(true);
        if (exp != null) {
            return compileExpAsAction(exp);
        }

        return null;
    }

    /**
     * 解析标签
     */
    private ASTNode parseLabel() {
        int start = tokens.get(index).getStart();
        index++; // 跳过 ::

        skipSpace();
        ASTNode name = parseName();
        skipSpace();

        if (name == null) {
            pushError(new ParseError("MISS_NAME", getCurrentPosition(), getCurrentPosition()));
            return null;
        }

        if (index < tokens.size() && "::".equals(tokens.get(index).getContent())) {
            index++;
        } else {
            pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), "::"));
        }

        ASTNode label = new ASTNode("label", start, getCurrentPosition());
        label.setAttribute("name", name.getAttribute("name"));

        // 添加到当前块的标签表
        ASTNode block = getCurrentBlock();
        if (block != null) {
            @SuppressWarnings("unchecked")
            Map<String, ASTNode> labels = (Map<String, ASTNode>) block.getAttribute("labels");
            if (labels == null) {
                labels = new HashMap<>();
                block.setAttribute("labels", labels);
            }

            String labelName = (String) name.getAttribute("name");
            ASTNode existing = labels.get(labelName);
            if (existing != null) {
                pushError(new ParseError("REDEFINED_LABEL",
                        label.getStart(), label.getFinish(), labelName));
            }
            labels.put(labelName, label);
        }

        return label;
    }

    /**
     * 解析局部变量声明
     */
    private ASTNode parseLocal() {
        int locPos = tokens.get(index).getStart();
        index++; // 跳过 local

        skipSpace();

        if (index < tokens.size() && "function".equals(tokens.get(index).getContent())) {
            // local function
            ASTNode func = parseFunction(true, true);
            if (func != null) {
                ASTNode name = (ASTNode) func.getAttribute("name");
                if (name != null) {
                    func.setAttribute("name", null);
                    name.setAttribute("value", func);
                    name.setAttribute("vstart", func.getStart());
                    name.setRange(func.getFinish());
                    name.setAttribute("locPos", locPos);
                    func.setParent(name);
                    return name;
                }
            }
            return func;
        }

        ASTNode name = parseName(true);
        if (name == null) {
            pushError(new ParseError("MISS_NAME", getCurrentPosition(), getCurrentPosition()));
            return null;
        }

        ASTNode local = createLocal(name, null);
        local.setAttribute("locPos", locPos);
        local.setEffect(Integer.MAX_VALUE);

        skipSpace();
        parseMultiVars(local, this::parseName, true);

        return local;
    }

    /**
     * 解析if语句
     */
    private ASTNode parseIf() {
        int start = tokens.get(index).getStart();
        index++; // 跳过 if

        ASTNode ifNode = new ASTNode("if", start, start);
        pushChunk(ifNode);

        skipSpace();
        ASTNode filter = parseExp();
        if (filter != null) {
            ifNode.setAttribute("filter", filter);
            filter.setParent(ifNode);
        } else {
            pushError(new ParseError("MISS_EXP", getCurrentPosition(), getCurrentPosition()));
        }

        skipSpace();
        if (index < tokens.size() && "then".equals(tokens.get(index).getContent())) {
            index++;
        } else {
            pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), "then"));
        }

        parseActions(ifNode);

        // 解析elseif和else
        while (index < tokens.size()) {
            Token token = tokens.get(index);
            String content = token.getContent();

            if ("elseif".equals(content)) {
                index++;
                ASTNode elseifBlock = new ASTNode("elseifblock", token.getStart(), token.getFinish());

                skipSpace();
                ASTNode elseifFilter = parseExp();
                if (elseifFilter != null) {
                    elseifBlock.setAttribute("filter", elseifFilter);
                    elseifFilter.setParent(elseifBlock);
                }

                skipSpace();
                if (index < tokens.size() && "then".equals(tokens.get(index).getContent())) {
                    index++;
                } else {
                    pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), "then"));
                }

                parseActions(elseifBlock);
                ifNode.addChild(elseifBlock);

            } else if ("else".equals(content)) {
                index++;
                ASTNode elseBlock = new ASTNode("elseblock", token.getStart(), token.getFinish());
                parseActions(elseBlock);
                ifNode.addChild(elseBlock);
                break;

            } else {
                break;
            }
        }

        popChunk();

        if (index < tokens.size() && "end".equals(tokens.get(index).getContent())) {
            ifNode.setFinish(tokens.get(index).getFinish());
            index++;
        } else {
            ifNode.setFinish(getCurrentPosition());
            pushError(new ParseError("MISS_END", start, getCurrentPosition()));
        }

        return ifNode;
    }

    /**
     * 解析while循环
     */
    private ASTNode parseWhile() {
        int start = tokens.get(index).getStart();
        index++; // 跳过 while

        ASTNode whileNode = new ASTNode("while", start, start);
        pushChunk(whileNode);

        skipSpace();
        ASTNode filter = parseExp();
        if (filter != null) {
            whileNode.setAttribute("filter", filter);
            filter.setParent(whileNode);
        } else {
            pushError(new ParseError("MISS_EXP", getCurrentPosition(), getCurrentPosition()));
        }

        skipSpace();
        if (index < tokens.size() && "do".equals(tokens.get(index).getContent())) {
            index++;
        } else {
            pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), "do"));
        }

        parseActions(whileNode);
        popChunk();

        if (index < tokens.size() && "end".equals(tokens.get(index).getContent())) {
            whileNode.setFinish(tokens.get(index).getFinish());
            index++;
        } else {
            whileNode.setFinish(getCurrentPosition());
            pushError(new ParseError("MISS_END", start, getCurrentPosition()));
        }

        return whileNode;
    }

    /**
     * 解析for循环
     */
    private ASTNode parseFor() {
        int start = tokens.get(index).getStart();
        index++; // 跳过 for

        skipSpace();
        ASTNode name = parseName();
        if (name == null) {
            pushError(new ParseError("MISS_NAME", getCurrentPosition(), getCurrentPosition()));
            return null;
        }

        skipSpace();
        if (index < tokens.size()) {
            String content = tokens.get(index).getContent();

            if ("=".equals(content)) {
                // 数值for循环
                return parseNumericFor(start, name);
            } else if (",".equals(content) || "in".equals(content)) {
                // 泛型for循环
                return parseGenericFor(start, name);
            }
        }

        pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), "= or in"));
        return null;
    }

    /**
     * 解析数值for循环
     */
    private ASTNode parseNumericFor(int start, ASTNode name) {
        index++; // 跳过 =

        ASTNode forNode = new ASTNode("for", start, start);
        pushChunk(forNode);

        // 创建循环变量
        ASTNode local = createLocal(name, null);
        forNode.addChild(local);

        skipSpace();
        ASTNode init = parseExp();
        if (init != null) {
            forNode.setAttribute("init", init);
            init.setParent(forNode);
        }

        skipSpace();
        if (index < tokens.size() && ",".equals(tokens.get(index).getContent())) {
            index++;
        } else {
            pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), ","));
        }

        skipSpace();
        ASTNode max = parseExp();
        if (max != null) {
            forNode.setAttribute("max", max);
            max.setParent(forNode);
        }

        // 可选的步长
        skipSpace();
        if (index < tokens.size() && ",".equals(tokens.get(index).getContent())) {
            index++;
            skipSpace();
            ASTNode step = parseExp();
            if (step != null) {
                forNode.setAttribute("step", step);
                step.setParent(forNode);
            }
        }

        skipSpace();
        if (index < tokens.size() && "do".equals(tokens.get(index).getContent())) {
            index++;
        } else {
            pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), "do"));
        }

        parseActions(forNode);
        popChunk();

        if (index < tokens.size() && "end".equals(tokens.get(index).getContent())) {
            forNode.setFinish(tokens.get(index).getFinish());
            index++;
        } else {
            forNode.setFinish(getCurrentPosition());
            pushError(new ParseError("MISS_END", start, getCurrentPosition()));
        }

        return forNode;
    }

    /**
     * 解析泛型for循环
     */
    private ASTNode parseGenericFor(int start, ASTNode name) {
        ASTNode inNode = new ASTNode("in", start, start);
        pushChunk(inNode);

        // 创建循环变量
        ASTNode local = createLocal(name, null);
        inNode.addChild(local);

        // 解析其他变量
        while (index < tokens.size() && ",".equals(tokens.get(index).getContent())) {
            index++;
            skipSpace();
            ASTNode var = parseName();
            if (var != null) {
                ASTNode varLocal = createLocal(var, null);
                inNode.addChild(varLocal);
            }
        }

        skipSpace();
        if (index < tokens.size() && "in".equals(tokens.get(index).getContent())) {
            index++;
        } else {
            pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), "in"));
        }

        skipSpace();
        List<ASTNode> exps = parseExpList();
        if (!exps.isEmpty()) {
            inNode.setAttribute("exps", exps);
            for (ASTNode exp : exps) {
                exp.setParent(inNode);
            }
        }

        skipSpace();
        if (index < tokens.size() && "do".equals(tokens.get(index).getContent())) {
            index++;
        } else {
            pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), "do"));
        }

        parseActions(inNode);
        popChunk();

        if (index < tokens.size() && "end".equals(tokens.get(index).getContent())) {
            inNode.setFinish(tokens.get(index).getFinish());
            index++;
        } else {
            inNode.setFinish(getCurrentPosition());
            pushError(new ParseError("MISS_END", start, getCurrentPosition()));
        }

        return inNode;
    }

    /**
     * 解析repeat循环
     */
    private ASTNode parseRepeat() {
        int start = tokens.get(index).getStart();
        index++; // 跳过 repeat

        ASTNode repeatNode = new ASTNode("repeat", start, start);
        pushChunk(repeatNode);

        parseActions(repeatNode);

        if (index < tokens.size() && "until".equals(tokens.get(index).getContent())) {
            index++;
        } else {
            pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), "until"));
        }

        skipSpace();
        ASTNode filter = parseExp();
        if (filter != null) {
            repeatNode.setAttribute("filter", filter);
            filter.setParent(repeatNode);
        } else {
            pushError(new ParseError("MISS_EXP", getCurrentPosition(), getCurrentPosition()));
        }

        popChunk();
        repeatNode.setFinish(getCurrentPosition());

        return repeatNode;
    }

    /**
     * 解析do块
     */
    private ASTNode parseDo() {
        int start = tokens.get(index).getStart();
        index++; // 跳过 do

        ASTNode doNode = new ASTNode("do", start, start);
        pushChunk(doNode);

        parseActions(doNode);
        popChunk();

        if (index < tokens.size() && "end".equals(tokens.get(index).getContent())) {
            doNode.setFinish(tokens.get(index).getFinish());
            index++;
        } else {
            doNode.setFinish(getCurrentPosition());
            pushError(new ParseError("MISS_END", start, getCurrentPosition()));
        }

        return doNode;
    }

    /**
     * 解析return语句
     */
    private ASTNode parseReturn() {
        int start = tokens.get(index).getStart();
        index++; // 跳过 return

        ASTNode returnNode = new ASTNode("return", start, start);

        skipSpace();
        if (index < tokens.size() && !isStatementEnd()) {
            List<ASTNode> exps = parseExpList();
            if (!exps.isEmpty()) {
                returnNode.setAttribute("exps", exps);
                for (ASTNode exp : exps) {
                    exp.setParent(returnNode);
                }
                returnNode.setFinish(exps.get(exps.size() - 1).getFinish());
            }
        }

        if (returnNode.getFinish() == start) {
            returnNode.setFinish(getCurrentPosition());
        }

        return returnNode;
    }

    /**
     * 解析break语句
     */
    private ASTNode parseBreak() {
        int start = tokens.get(index).getStart();
        int finish = tokens.get(index).getFinish();
        index++; // 跳过 break 或 continue

        return new ASTNode("break", start, finish);
    }

    /**
     * 解析goto语句
     */
    private ASTNode parseGoto() {
        int start = tokens.get(index).getStart();
        index++; // 跳过 goto

        skipSpace();
        ASTNode name = parseName();
        if (name == null) {
            pushError(new ParseError("MISS_NAME", getCurrentPosition(), getCurrentPosition()));
            return null;
        }

        ASTNode gotoNode = new ASTNode("goto", start, name.getFinish());
        gotoNode.setAttribute("name", name.getAttribute("name"));

        return gotoNode;
    }

    /**
     * 解析函数定义
     */
    private ASTNode parseFunction(boolean isLocal, boolean isAction) {
        boolean isn=false;
        int a=-1;
        while (index+a>=0){
            if (tokens.get(index+a).getType()==TokenType.SPACE){
                a--;
            }else {
                break;
            }
        }
        if (tokens.get(index+a).getContent().equals("=")||tokens.get(index+a).getContent().equals("(")||tokens.get(index+a).getContent().equals(",")){
            isn=true;
        }
        int funcLeft = tokens.get(index).getStart();
        int funcRight = tokens.get(index).getFinish();
        index+=2;
        ASTNode func = new ASTNode("function");
        func.setStart(funcLeft);
        func.setFinish(funcRight);
        func.setBstart(funcRight);

        boolean hasLeftParen = false;
        skipSpace(true);
        // 检查是否有函数名
        ASTNode funcName = null;



        boolean hasFunctionName=false;
        // 现在检查左括号
        while (index < tokens.size()&&!hasLeftParen) {
            if (tokens.get(index).getType()==TokenType.WORD) {
                hasFunctionName=true;
            }
            else if (!hasFunctionName&&!isn){
                pushError(new ParseError("MISS_NAME",
                        tokens.get(index).getStart(), tokens.get(index).getFinish()));
            }
            else if (tokens.get(index).getContent().equals("(")) {
                hasLeftParen = true;
                break;
            }
            index++;
            //System.out.println(tokens.get(i).getContent());
        }

        if (hasLeftParen) {
            int lastLocalCount = localCount;
            localCount = 0;
            pushChunk(func);

            ASTNode params = null;

            // 处理方法的self参数
            if (funcName != null && "getmethod".equals(funcName.getType())) {
                params = new ASTNode("funcargs", funcRight, funcRight);
                ASTNode self = new ASTNode("self", funcRight, funcRight);
                self.setAttribute("name", "self");
                ASTNode selfLocal = createLocal(self, null);
                params.addChild(selfLocal);
                params.setParent(func);
            }

            if (params == null) {
                params = new ASTNode("funcargs");
            }

            int parenLeft = tokens.get(index).getStart();
            index++; // 跳过 (

            params = parseParams(params);
            params.setStart(parenLeft);
            params.setParent(func);
            func.setAttribute("args", params);

            skipSpace(true);
            if (index < tokens.size() && ")".equals(tokens.get(index).getContent())) {
                int parenRight = tokens.get(index).getFinish();
                func.setFinish(parenRight);
                func.setBstart(parenRight);
                params.setFinish(parenRight);
                index++;
                skipSpace(true);
            } else {
                func.setFinish(getCurrentPosition());
                func.setBstart(func.getFinish());
                params.setFinish(func.getFinish());
                // 报告缺少右括号，使用正确的位置
                pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), ")"));
            }

            parseActions(func);
            popChunk();

            func.setBfinish(getCurrentPosition());
            if (index < tokens.size() && "end".equals(tokens.get(index).getContent())) {
                int endRight = tokens.get(index).getFinish();
                func.setFinish(endRight);
                index++;
            } else {
                func.setFinish(getCurrentPosition());
                pushError(new ParseError("MISS_END", funcLeft, funcRight));
            }

            localCount = lastLocalCount;
            return func;

        } else {
            // 没有找到左括号，这是语法错误
            // 但是要确保错误位置正确
            int errorPos = getCurrentPosition();
            pushError(new ParseError("MISS_SYMBOL", errorPos, errorPos, "("));

            // 尝试恢复解析 - 假设有空的参数列表
            int lastLocalCount = localCount;
            localCount = 0;
            pushChunk(func);

            // 创建空的参数列表
            ASTNode params = new ASTNode("funcargs", errorPos, errorPos);
            params.setParent(func);
            func.setAttribute("args", params);

            parseActions(func);
            popChunk();

            func.setBfinish(getCurrentPosition());
            if (index < tokens.size() && "end".equals(tokens.get(index).getContent())) {
                int endRight = tokens.get(index).getFinish();
                func.setFinish(endRight);
                index++;
            } else {
                func.setFinish(getCurrentPosition());
                pushError(new ParseError("MISS_END", funcLeft, funcRight));
            }

            localCount = lastLocalCount;
            return func;
        }
    }

    /**
     * 解析参数列表
     */
    private ASTNode parseParams(ASTNode params) {
        boolean lastSep = false;
        boolean hasDots = false;

        while (true) {
            skipSpace();
            if (index >= tokens.size() || ")".equals(tokens.get(index).getContent())) {
                if (lastSep) {
                    pushError(new ParseError("MISS_NAME", getCurrentPosition(), getCurrentPosition()));
                }
                break;
            }

            Token token = tokens.get(index);
            String content = token.getContent();

            if (",".equals(content)) {
                if (lastSep) {
                    pushError(new ParseError("MISS_NAME", getCurrentPosition(), getCurrentPosition()));
                }
                lastSep = true;
                index++;
                continue;
            }

            if ("...".equals(content)) {
                if (!lastSep && params.getChildren().size() > 0) {
                    pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), ","));
                }
                lastSep = false;

                if (params == null) {
                    params = new ASTNode("funcargs");
                }

                ASTNode vararg = new ASTNode("...", token.getStart(), token.getFinish());
                vararg.setAttribute("name", "...");
                vararg.setParent(params);

                ASTNode chunk = getCurrentChunk();
                if (chunk != null) {
                    chunk.setAttribute("vararg", vararg);
                }

                params.addChild(vararg);

                if (hasDots) {
                    pushError(new ParseError("ARGS_AFTER_DOTS", token.getStart(), token.getFinish()));
                }
                hasDots = true;
                index++;
                continue;
            }

            if (token.getType() == TokenType.WORD) {
                if (!lastSep && params.getChildren().size() > 0) {
                    pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), ","));
                }
                lastSep = false;

                if (params == null) {
                    params = new ASTNode("funcargs");
                }

                ASTNode paramName = new ASTNode("local", token.getStart(), token.getFinish());
                paramName.setAttribute("name", content);
                ASTNode param = createLocal(paramName, null);
                param.setParent(params);
                params.addChild(param);

                if (hasDots) {
                    pushError(new ParseError("ARGS_AFTER_DOTS", token.getStart(), token.getFinish()));
                }

                if (KEYWORDS.contains(content)) {
                    pushError(new ParseError("KEYWORD", token.getStart(), token.getFinish()));
                }

                index++;
                continue;
            }

            skipUnknownSymbol();
        }

        return params;
    }

    /**
     * 解析表达式
     */
    private ASTNode parseExp() {
        return parseExp(false);
    }

    private ASTNode parseExp(boolean asAction) {
        return parseExp(asAction, 0);
    }

    private ASTNode parseExp(boolean asAction, int level) {
        ASTNode exp;

        // 一元操作符
        String unaryOp = parseUnaryOP();
        if (unaryOp != null) {
            int uopLevel = UNARY_PRIORITY.getOrDefault(unaryOp, 11);
            skipSpace();
            ASTNode child = parseExp(asAction, uopLevel);

            // 预计算负数
            if ("-".equals(unaryOp) && child != null &&
                    ("number".equals(child.getType()) || "integer".equals(child.getType()))) {
                Object value = child.getAttribute("value");
                if (value instanceof Number) {
                    child.setStart(getCurrentPosition() - unaryOp.length());
                    if (value instanceof Integer) {
                        child.setAttribute("value", -(Integer) value);
                    } else if (value instanceof Double) {
                        child.setAttribute("value", -(Double) value);
                    }
                    exp = child;
                } else {
                    exp = createUnaryNode(unaryOp, child);
                }
            } else {
                exp = createUnaryNode(unaryOp, child);
            }
        } else {
            exp = parseExpUnit();
            if (exp == null) {
                return null;
            }
        }

        // 二元操作符
        while (true) {
            skipSpace();
            String binaryOp = parseBinaryOP(asAction, level);
            if (binaryOp == null) {
                break;
            }

            int bopLevel = BINARY_PRIORITY.getOrDefault(binaryOp, 1);
            skipSpace();

            boolean isForward = isSymbolForward(bopLevel);
            ASTNode child = parseExp(asAction, isForward ? bopLevel + 1 : bopLevel);

            if (child == null) {
                if (!skipUnknownSymbol()) {
                    pushError(new ParseError("MISS_EXP", getCurrentPosition(), getCurrentPosition()));
                }
            }

            exp = createBinaryNode(binaryOp, exp, child);
        }

        return exp;
    }

    /**
     * 创建一元操作节点
     */
    private ASTNode createUnaryNode(String op, ASTNode child) {
        int start = getCurrentPosition() - op.length();
        int finish = child != null ? child.getFinish() : getCurrentPosition();

        ASTNode unary = new ASTNode("unary", start, finish);
        unary.setAttribute("op", UNARY_ALIAS.getOrDefault(op, op));

        if (child != null) {
            unary.addChild(child);
        } else {
            pushError(new ParseError("MISS_EXP", getCurrentPosition(), getCurrentPosition()));
        }

        return unary;
    }

    /**
     * 创建二元操作节点
     */
    private ASTNode createBinaryNode(String op, ASTNode left, ASTNode right) {
        int start = left != null ? left.getStart() : getCurrentPosition();
        int finish = right != null ? right.getFinish() : getCurrentPosition();

        ASTNode binary = new ASTNode("binary", start, finish);
        binary.setAttribute("op", BINARY_ALIAS.getOrDefault(op, op));

        if (left != null) {
            binary.addChild(left);
        }
        if (right != null) {
            binary.addChild(right);
        }

        return binary;
    }

    /**
     * 解析表达式单元
     */
    private ASTNode parseExpUnit() {
        if (index >= tokens.size() || isEOF()) return null;

        Token token = tokens.get(index);
        String content = token.getContent();

        // 括号表达式
        if ("(".equals(content)) {
            return parseParen();
        }

        // 可变参数
        if ("...".equals(content)) {
            return parseVarargs();
        }

        // 表构造
        if ("{".equals(content)) {
            ASTNode table = parseTable();
            return table != null ? checkNeedParen(table) : null;
        }

        // 字符串
        if (token.isString()) {
            ASTNode string = parseString();
            return string != null ? checkNeedParen(string) : null;
        }

        // 数字
        ASTNode number = parseNumber();
        if (number != null) {
            return number;
        }

        // 字面量
        if ("nil".equals(content)) {
            return parseNil();
        }

        if ("true".equals(content) || "false".equals(content)) {
            return parseBoolean();
        }

        // 函数
        if ("function".equals(content)) {
            return parseFunction(false, false);
        }

        // 标识符
        ASTNode name = parseName();
        if (name != null) {
            ASTNode nameNode = resolveName(name);
            if (nameNode != null) {
                return parseSimple(nameNode, false);
            }
        }

        return null;
    }

    /**
     * 解析括号表达式
     */
    private ASTNode parseParen() {
        int start = tokens.get(index).getStart();
        index++; // 跳过 (

        skipSpace();
        ASTNode exp = parseExp();
        skipSpace();

        if (index < tokens.size() && ")".equals(tokens.get(index).getContent())) {
            int finish = tokens.get(index).getFinish();
            index++;

            ASTNode paren = new ASTNode("paren", start, finish);
            if (exp != null) {
                paren.addChild(exp);
            }
            return paren;
        } else {
            // 修复：报告缺少右括号的位置应该在左括号之后
            pushError(new ParseError("MISS_SYMBOL", start, getCurrentPosition(), ")"));
            return exp;
        }
    }

    /**
     * 解析可变参数
     */
    private ASTNode parseVarargs() {
        Token token = tokens.get(index);
        index++;

        ASTNode varargs = new ASTNode("...", token.getStart(), token.getFinish());
        varargs.setAttribute("name", "...");

        return varargs;
    }

    /**
     * 解析表构造
     */
    private ASTNode parseTable() {
        int start = tokens.get(index).getStart();
        index++; // 跳过 {

        ASTNode table = new ASTNode("table", start, start);

        boolean lastSep = false;

        while (true) {
            skipSpace();
            if (index >= tokens.size() || "}".equals(tokens.get(index).getContent())) {
                if (lastSep) {
                    // 允许尾随逗号
                }
                break;
            }

            Token token = tokens.get(index);
            String content = token.getContent();

            if (",".equals(content) || ";".equals(content)) {
                if (!lastSep) {
                    pushError(new ParseError("MISS_EXP", getCurrentPosition(), getCurrentPosition()));
                }
                lastSep = true;
                index++;
                continue;
            }

            lastSep = false;

            // 解析表字段
            ASTNode field = parseTableField();
            if (field != null) {
                table.addChild(field);
            } else {
                if (!skipUnknownSymbol()) {
                    break;
                }
            }
        }

        if (index < tokens.size() && "}".equals(tokens.get(index).getContent())) {
            table.setFinish(tokens.get(index).getFinish());
            index++;
        } else {
            table.setFinish(getCurrentPosition());
            // 修复：报告缺少右大括号的位置应该在左大括号之后
            pushError(new ParseError("MISS_SYMBOL", start, getCurrentPosition(), "}"));
        }

        return table;
    }

    /**
     * 解析表字段
     */
    private ASTNode parseTableField() {
        if (index >= tokens.size()) return null;

        Token token = tokens.get(index);
        String content = token.getContent();

        // [exp] = exp
        if ("[".equals(content)) {
            int start = token.getStart();
            index++;

            skipSpace();
            ASTNode key = parseExp();
            skipSpace();

            if (index < tokens.size() && "]".equals(tokens.get(index).getContent())) {
                index++;
            } else {
                // 修复：报告缺少右方括号的位置应该在左方括号之后
                pushError(new ParseError("MISS_SYMBOL", start, getCurrentPosition(), "]"));
            }

            skipSpace();
            if (index < tokens.size() && "=".equals(tokens.get(index).getContent())) {
                index++;
            } else {
                pushError(new ParseError("MISS_SYMBOL", getCurrentPosition(), getCurrentPosition(), "="));
            }

            skipSpace();
            ASTNode value = parseExp();

            ASTNode field = new ASTNode("tableindex", start,
                    value != null ? value.getFinish() : getCurrentPosition());
            if (key != null) {
                field.setAttribute("index", key);
                key.setParent(field);
            }
            if (value != null) {
                field.setAttribute("value", value);
                value.setParent(field);
            }

            return field;
        }

        // name = exp
        if (token.getType() == TokenType.WORD) {
            int nextIndex = index + 1;
            skipSpaceAt(nextIndex);

            if (nextIndex < tokens.size() && "=".equals(tokens.get(nextIndex).getContent())) {
                int start = token.getStart();
                String name = content;
                index++; // 跳过名字

                skipSpace();
                index++; // 跳过 =

                skipSpace();
                ASTNode value = parseExp();

                ASTNode field = new ASTNode("tablefield", start,
                        value != null ? value.getFinish() : getCurrentPosition());
                field.setAttribute("field", name);
                if (value != null) {
                    field.setAttribute("value", value);
                    value.setParent(field);
                }

                return field;
            }
        }

        // exp
        ASTNode exp = parseExp();
        if (exp != null) {
            ASTNode field = new ASTNode("tablelist", exp.getStart(), exp.getFinish());
            field.setAttribute("value", exp);
            exp.setParent(field);
            return field;
        }

        return null;
    }

    /**
     * 解析字符串
     */
    private ASTNode parseString() {
        Token token = tokens.get(index);
        if (!token.isString()) return null;

        index++;

        ASTNode string = new ASTNode("string", token.getStart(), token.getFinish());
        string.setAttribute("value", token.getContent());
        string.setLiteral(true);

        return string;
    }

    /**
     * 解析数字
     */
    private ASTNode parseNumber() {
        Token token = tokens.get(index);
        if (!token.isNumber()) return null;

        index++;

        String type = token.getType() == TokenType.INTEGER ? "integer" : "number";
        ASTNode number = new ASTNode(type, token.getStart(), token.getFinish());

        try {
            if (type.equals("integer")) {
                number.setAttribute("value", Long.parseLong(token.getContent()));
            } else {
                number.setAttribute("value", Double.parseDouble(token.getContent()));
            }
        } catch (NumberFormatException e) {
            number.setAttribute("value", token.getContent());
        }

        number.setLiteral(true);
        return number;
    }

    /**
     * 解析nil
     */
    private ASTNode parseNil() {
        Token token = tokens.get(index);
        if (!"nil".equals(token.getContent())) return null;

        index++;

        ASTNode nil = new ASTNode("nil", token.getStart(), token.getFinish());
        nil.setAttribute("value", null);
        nil.setLiteral(true);

        return nil;
    }

    /**
     * 解析布尔值
     */
    private ASTNode parseBoolean() {
        Token token = tokens.get(index);
        String content = token.getContent();

        if (!"true".equals(content) && !"false".equals(content)) return null;

        index++;

        ASTNode bool = new ASTNode("boolean", token.getStart(), token.getFinish());
        bool.setAttribute("value", "true".equals(content));
        bool.setLiteral(true);

        return bool;
    }

    /**
     * 解析标识符
     */
    private ASTNode parseName() {
        return parseName(false);
    }

    private ASTNode parseName(boolean asAction) {
        if (index >= tokens.size()) return null;

        Token token = tokens.get(index);
        if (token.getType() != TokenType.WORD) return null;

        String content = token.getContent();
        if (KEYWORDS.contains(content)) return null;

        index++;

        ASTNode name = new ASTNode("name", token.getStart(), token.getFinish());
        name.setAttribute("name", content);

        return name;
    }

    /**
     * 解析简单表达式（字段访问、方法调用等）
     */
    private ASTNode parseSimple(ASTNode node, boolean funcName) {
        if (node == null) return null;

        ASTNode current = node;

        while (true) {
            skipSpace();
            if (index >= tokens.size()) break;

            Token token = tokens.get(index);
            String content = token.getContent();

            // 字段访问 obj.field
            if (".".equals(content)) {
                index++;
                skipSpace();

                ASTNode field = parseName();
                if (field == null) {
                    pushError(new ParseError("MISS_NAME", getCurrentPosition(), getCurrentPosition()));
                    break;
                }

                ASTNode getfield = new ASTNode("getfield", current.getStart(), field.getFinish());
                getfield.setAttribute("node", current);
                getfield.setAttribute("field", field);
                current.setParent(getfield);
                field.setParent(getfield);

                current = getfield;
                continue;
            }

            // 方法调用 obj:method
            if (":".equals(content)) {
                index++;
                skipSpace();

                ASTNode method = parseName();
                if (method == null) {
                    pushError(new ParseError("MISS_NAME", getCurrentPosition(), getCurrentPosition()));
                    break;
                }

                ASTNode getmethod = new ASTNode("getmethod", current.getStart(), method.getFinish());
                getmethod.setAttribute("node", current);
                getmethod.setAttribute("method", method);
                current.setParent(getmethod);
                method.setParent(getmethod);

                current = getmethod;
                continue;
            }

            // 索引访问 obj[index]
            if ("[".equals(content)) {
                int bracketStart = token.getStart();
                index++;
                skipSpace();

                ASTNode indexExp = parseExp();
                skipSpace();

                if (index < tokens.size() && "]".equals(tokens.get(index).getContent())) {
                    int finish = tokens.get(index).getFinish();
                    index++;

                    ASTNode getindex = new ASTNode("getindex", current.getStart(), finish);
                    getindex.setAttribute("node", current);
                    getindex.setAttribute("index", indexExp);
                    current.setParent(getindex);
                    if (indexExp != null) {
                        indexExp.setParent(getindex);
                    }

                    current = getindex;
                    continue;
                } else {
                    // 修复：报告缺少右方括号的位置应该在左方括号之后
                    pushError(new ParseError("MISS_SYMBOL", bracketStart, getCurrentPosition(), "]"));
                    break;
                }
            }

            // 函数调用 func(args) 或 func{table} 或 func"string"
            if ("(".equals(content) || "{".equals(content) ||
                    (index < tokens.size() && tokens.get(index).isString())) {

                ASTNode args = null;
                int argsStart = getCurrentPosition();
                int argsFinish = getCurrentPosition();

                if ("(".equals(content)) {
                    // 普通函数调用
                    int parenStart = token.getStart();
                    index++;
                    skipSpace();

                    List<ASTNode> argList = new ArrayList<>();
                    if (index < tokens.size() && !")".equals(tokens.get(index).getContent())) {
                        argList = parseExpList();
                    }

                    skipSpace();
                    if (index < tokens.size() && ")".equals(tokens.get(index).getContent())) {
                        argsFinish = tokens.get(index).getFinish();
                        index++;
                    } else {
                        // 修复：报告缺少右括号的位置应该在左括号之后
                        pushError(new ParseError("MISS_SYMBOL", parenStart, getCurrentPosition(), ")"));
                    }

                    args = new ASTNode("callargs", argsStart, argsFinish);
                    for (ASTNode arg : argList) {
                        args.addChild(arg);
                    }

                } else if ("{".equals(content)) {
                    // 表参数调用
                    args = parseTable();

                } else if (tokens.get(index).isString()) {
                    // 字符串参数调用
                    args = parseString();
                }

                ASTNode call = new ASTNode("call", current.getStart(),
                        args != null ? args.getFinish() : getCurrentPosition());
                call.setAttribute("node", current);
                call.setAttribute("args", args);
                current.setParent(call);
                if (args != null) {
                    args.setParent(call);
                }

                current = call;
                continue;
            }

            break;
        }

        return current;
    }

    /**
     * 解析表达式列表
     */
    private List<ASTNode> parseExpList() {
        List<ASTNode> exps = new ArrayList<>();

        ASTNode exp = parseExp();
        if (exp != null) {
            exps.add(exp);

            while (index < tokens.size()) {
                skipSpace();
                if (index < tokens.size() && ",".equals(tokens.get(index).getContent())) {
                    index++;
                    skipSpace();

                    ASTNode nextExp = parseExp();
                    if (nextExp != null) {
                        exps.add(nextExp);
                    } else {
                        pushError(new ParseError("MISS_EXP", getCurrentPosition(), getCurrentPosition()));
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        return exps;
    }

    /**
     * 解析多变量声明
     */
    private void parseMultiVars(ASTNode first, Supplier<ASTNode> parser, boolean isLocal) {
        List<ASTNode> vars = new ArrayList<>();
        vars.add(first);

        // 解析其他变量
        while (index < tokens.size() && ",".equals(tokens.get(index).getContent())) {
            index++;
            skipSpace();

            ASTNode var = parser.get();
            if (var != null) {
                if (isLocal) {
                    ASTNode local = createLocal(var, null);
                    vars.add(local);
                } else {
                    vars.add(var);
                }
            } else {
                pushError(new ParseError("MISS_NAME", getCurrentPosition(), getCurrentPosition()));
                break;
            }
        }

        // 解析赋值
        skipSpace();
        if (index < tokens.size() && "=".equals(tokens.get(index).getContent())) {
            index++;
            skipSpace();

            List<ASTNode> values = parseExpList();

            // 将值分配给变量
            for (int i = 0; i < vars.size(); i++) {
                ASTNode var = vars.get(i);
                if (i < values.size()) {
                    ASTNode value = values.get(i);
                    var.setAttribute("value", value);
                    if (value != null) {
                        value.setParent(var);
                        var.setRange(value.getFinish());
                    }
                }
            }
        }
    }

    /**
     * 将表达式编译为动作
     */
    private ASTNode compileExpAsAction(ASTNode exp) {
        if (exp == null) return null;

        String type = exp.getType();

        // 赋值操作
        if ("call".equals(type)) {
            return exp; // 函数调用可以作为语句
        }

        // 检查是否是赋值表达式
        if (isAssignmentTarget(exp)) {
            // 查找赋值操作符
            skipSpace();
            if (index < tokens.size()) {
                String op = tokens.get(index).getContent();
                if ("=".equals(op) || isCompoundAssignment(op)) {
                    return parseAssignment(exp, op);
                }
            }
        }

        // 其他表达式作为语句（可能是错误）
        pushError(new ParseError("UNEXPECT_SYMBOL", exp.getStart(), exp.getFinish()));
        return exp;
    }

    /**
     * 解析赋值语句
     */
    private ASTNode parseAssignment(ASTNode target, String op) {
        index++; // 跳过赋值操作符
        skipSpace();

        List<ASTNode> values = parseExpList();

        // 创建赋值节点
        String assignType = getAssignmentType(target);
        ASTNode assign = new ASTNode(assignType, target.getStart(),
                values.isEmpty() ? getCurrentPosition() :
                        values.get(values.size() - 1).getFinish());

        // 设置目标和值
        assign.setAttribute("node", target);
        target.setParent(assign);

        if (!values.isEmpty()) {
            if (values.size() == 1) {
                assign.setAttribute("value", values.get(0));
                values.get(0).setParent(assign);
            } else {
                assign.setAttribute("values", values);
                for (ASTNode value : values) {
                    value.setParent(assign);
                }
            }
        }

        return assign;
    }

    /**
     * 获取赋值类型
     */
    private String getAssignmentType(ASTNode target) {
        String type = target.getType();
        switch (type) {
            case "getglobal":
                return "setglobal";
            case "getlocal":
                return "setlocal";
            case "getfield":
                return "setfield";
            case "getindex":
                return "setindex";
            case "getmethod":
                return "setmethod";
            default:
                return "assign";
        }
    }

    /**
     * 检查是否是赋值目标
     */
    private boolean isAssignmentTarget(ASTNode exp) {
        if (exp == null) return false;
        String type = exp.getType();
        return "getglobal".equals(type) || "getlocal".equals(type) ||
                "getfield".equals(type) || "getindex".equals(type) ||
                "getmethod".equals(type);
    }

    /**
     * 检查是否是复合赋值操作符
     */
    private boolean isCompoundAssignment(String op) {
        return "+=".equals(op) || "-=".equals(op) || "*=".equals(op) ||
                "/=".equals(op) || "%=".equals(op) || "^=".equals(op) ||
                "//=".equals(op) || "&=".equals(op) || "|=".equals(op) ||
                "<<=".equals(op) || ">>=".equals(op);
    }

    /**
     * 解析一元操作符
     */
    private String parseUnaryOP() {
        if (index >= tokens.size()) return null;

        Token token = tokens.get(index);
        String content = token.getContent();

        if (UNARY_PRIORITY.containsKey(content)) {
            // 检查非标准操作符
            if ("!".equals(content) && !options.getNonstandardSymbols().contains("!")) {
                return null;
            }

            index++;
            return content;
        }

        return null;
    }

    /**
     * 解析二元操作符
     */
    private String parseBinaryOP(boolean asAction, int level) {
        if (index >= tokens.size()) return null;

        Token token = tokens.get(index);
        String content = token.getContent();

        // 检查是否是二元操作符
        if (!BINARY_PRIORITY.containsKey(content)) {
            return null;
        }

        int priority = BINARY_PRIORITY.get(content);
        if (priority < level) {
            return null;
        }

        // 检查非标准操作符
        if (("&&".equals(content) || "||".equals(content) || "!=".equals(content)) &&
                !options.getNonstandardSymbols().contains(content)) {
            return null;
        }

        // 在动作上下文中，某些操作符可能有特殊含义
        if (asAction && "=".equals(content)) {
            return null; // 赋值操作符在动作中单独处理
        }

        index++;
        return content;
    }

    /**
     * 检查操作符结合性
     */
    private boolean isSymbolForward(int level) {
        // 右结合操作符
        return level != 8 && level != 12; // .. 和 ^ 是右结合的
    }

    /**
     * 检查是否需要括号
     */
    private ASTNode checkNeedParen(ASTNode node) {
        // 在某些上下文中，字面量可能需要括号
        // 这里简化处理
        return node;
    }

    /**
     * 名称解析
     */
    private ASTNode resolveName(ASTNode name) {
        if (name == null) return null;

        String nameStr = (String) name.getAttribute("name");
        if (nameStr == null) return name;

        // 检查是否是特殊名称
        if (SPECIALS.contains(nameStr)) {
            name.setSpecial(nameStr);
        }

        // 查找局部变量
        ASTNode local = findLocal(nameStr);
        if (local != null) {
            ASTNode getlocal = new ASTNode("getlocal", name.getStart(), name.getFinish());
            getlocal.setAttribute("name", nameStr);
            getlocal.setNode(local);
            return getlocal;
        }

        // 全局变量
        ASTNode getglobal = new ASTNode("getglobal", name.getStart(), name.getFinish());
        getglobal.setAttribute("name", nameStr);
        return getglobal;
    }

    /**
     * 查找局部变量
     */
    private ASTNode findLocal(String name) {
        // 在当前作用域栈中查找局部变量
        for (int i = chunkStack.size() - 1; i >= 0; i--) {
            ASTNode chunk = chunkStack.get(i);
            @SuppressWarnings("unchecked")
            List<ASTNode> locals = (List<ASTNode>) chunk.getAttribute("locals");
            if (locals != null) {
                for (ASTNode local : locals) {
                    String localName = (String) local.getAttribute("name");
                    if (name.equals(localName)) {
                        return local;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 创建局部变量
     */
    private ASTNode createLocal(ASTNode name, Object attrs) {
        if (name == null) return null;

        String nameStr = (String) name.getAttribute("name");
        if (nameStr == null) return name;

        // 检查局部变量限制
        localCount++;
        if (localCount > LOCAL_LIMIT && !localLimited) {
            localLimited = true;
            pushError(new ParseError("LOCAL_LIMIT", name.getStart(), name.getFinish()));
        }

        ASTNode local = new ASTNode("local", name.getStart(), name.getFinish());
        local.setAttribute("name", nameStr);
        local.setEffect(name.getFinish());

        // 添加到当前块的局部变量列表
        ASTNode chunk = getCurrentChunk();
        if (chunk != null) {
            @SuppressWarnings("unchecked")
            List<ASTNode> locals = (List<ASTNode>) chunk.getAttribute("locals");
            if (locals == null) {
                locals = new ArrayList<>();
                chunk.setAttribute("locals", locals);
            }
            locals.add(local);
        }

        return local;
    }

    // 工具方法

    /**
     * 跳过空白字符
     */
    private void skipSpace() {
        skipSpace(false);
    }

    private void skipSpace(boolean isAction) {
        while (index < tokens.size()) {
            Token token = tokens.get(index);
            if (token.isSpace() || token.isNewline() || token.isComment()) {
                index++;
            } else {
                break;
            }
        }
    }

    /**
     * 在指定位置跳过空白字符（不改变当前位置）
     */
    private void skipSpaceAt(int pos) {
        while (pos < tokens.size()) {
            Token token = tokens.get(pos);
            if (token.isSpace() || token.isNewline() || token.isComment()) {
                pos++;
            } else {
                break;
            }
        }
    }

    /**
     * 跳过未知符号
     */
    private boolean skipUnknownSymbol() {
        if (index < tokens.size()) {
            Token token = tokens.get(index);
            pushError(new ParseError("UNKNOWN_SYMBOL", token.getStart(), token.getFinish(), token.getContent()));
            index++;
            return true;
        }
        return false;
    }

    /**
     * 获取当前位置
     */
    private int getCurrentPosition() {
        if (index < tokens.size()) {
            return tokens.get(index).getStart();
        }
        return tokens.isEmpty() ? 0 : tokens.get(tokens.size() - 1).getFinish();
    }

    /**
     * 获取前一个token的结束位置
     */
    private int getPreviousPosition() {
        if (index > 0 && index <= tokens.size()) {
            return tokens.get(index - 1).getFinish();
        }
        return getCurrentPosition();
    }

    /**
     * 检查是否到达文件末尾
     */
    private boolean isEOF() {
        return index >= tokens.size() ||
                (index < tokens.size() && tokens.get(index).isEOF());
    }

    /**
     * 检查是否是语句结束
     */
    private boolean isStatementEnd() {
        if (index >= tokens.size()) return true;

        Token token = tokens.get(index);
        String content = token.getContent();

        return ";".equals(content) || token.isNewline() ||
                isChunkFinishToken(content) || token.isEOF();
    }

    /**
     * 检查是否是块结束标记
     */
    private boolean isChunkFinishToken(String token) {
        return "end".equals(token) || "else".equals(token) || "elseif".equals(token) ||
                "until".equals(token) || "}".equals(token) || ")".equals(token) ||
                "]".equals(token);
    }

    /**
     * 检查是否是关键字
     */
    private boolean isKeyword(String word) {
        return KEYWORDS.contains(word);
    }

    /**
     * 压入块栈
     */
    private void pushChunk(ASTNode chunk) {
        chunkStack.add(chunk);
    }

    /**
     * 弹出块栈
     */
    private void popChunk() {
        if (!chunkStack.isEmpty()) {
            chunkStack.remove(chunkStack.size() - 1);
        }
    }

    /**
     * 获取当前块
     */
    private ASTNode getCurrentChunk() {
        return chunkStack.isEmpty() ? null : chunkStack.get(chunkStack.size() - 1);
    }

    /**
     * 获取当前块（用于作用域）
     */
    private ASTNode getCurrentBlock() {
        for (int i = chunkStack.size() - 1; i >= 0; i--) {
            ASTNode chunk = chunkStack.get(i);
            String type = chunk.getType();
            if ("function".equals(type) || "main".equals(type) ||
                    "do".equals(type) || "if".equals(type) ||
                    "while".equals(type) || "for".equals(type) ||
                    "repeat".equals(type)) {
                return chunk;
            }
        }
        return null;
    }

    /**
     * 添加错误
     */
    private void pushError(ParseError error) {
        errors.add(error);
    }

    /**
     * 获取错误列表
     */
    public List<ParseError> getErrors() {
        return new ArrayList<>(errors);
    }
}
