package com.luaparser;

import com.luaparser.parser.ParseError;
import com.luaparser.parser.ParseState;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 错误修复建议提供器
 */
public class ErrorSuggestionProvider {

    // 常见的Lua关键字
    private static final Set<String> LUA_KEYWORDS = Set.of(
            "and", "break", "do", "else", "elseif", "end", "false", "for",
            "function", "goto", "if", "in", "local", "nil", "not", "or",
            "repeat", "return", "then", "true", "until", "while"
    );

    // 常见的Lua函数
    private static final Set<String> COMMON_FUNCTIONS = Set.of(
            "print", "type", "pairs", "ipairs", "next", "tostring", "tonumber",
            "assert", "error", "pcall", "xpcall", "require", "dofile", "loadfile",
            "setmetatable", "getmetatable", "rawget", "rawset", "rawequal", "rawlen"
    );

    // 常见的字符串方法
    private static final Set<String> STRING_METHODS = Set.of(
            "find", "match", "gmatch", "gsub", "sub", "upper", "lower", "len",
            "rep", "reverse", "format", "char", "byte"
    );

    // 常见的表方法
    private static final Set<String> TABLE_METHODS = Set.of(
            "insert", "remove", "concat", "sort", "unpack", "pack"
    );

    /**
     * 为解析状态中的所有错误生成修复建议
     */
    public static void generateSuggestions(ParseState state) {
        if (state == null || !state.hasErrors()) {
            return;
        }

        String luaSource = state.getLua();

        for (ParseError error : state.getErrors()) {
            generateSuggestionsForError(error, luaSource, state);
        }
    }

    /**
     * 为单个错误生成修复建议
     */
    public static void generateSuggestionsForError(ParseError error, String luaSource, ParseState state) {
        if (error == null) return;

        String errorType = error.getType();
        String message = error.getMessage();

        switch (errorType) {
            case "MISS_SYMBOL":
                generateMissSymbolSuggestions(error, message, luaSource);
                break;

            case "MISS_END":
                generateMissEndSuggestions(error, luaSource);
                break;

            case "MISS_NAME":
                generateMissNameSuggestions(error, luaSource);
                break;

            case "MISS_EXP":
                generateMissExpSuggestions(error, luaSource);
                break;

            case "UNKNOWN_SYMBOL":
                generateUnknownSymbolSuggestions(error, message, luaSource);
                break;

            case "UNEXPECT_SYMBOL":
                generateUnexpectedSymbolSuggestions(error, message, luaSource);
                break;

            case "KEYWORD":
                generateKeywordSuggestions(error, message, luaSource);
                break;

            case "REDEFINED_LABEL":
                generateRedefinedLabelSuggestions(error, message);
                break;

            case "ACTION_AFTER_RETURN":
                generateActionAfterReturnSuggestions(error);
                break;

            case "LOCAL_LIMIT":
                generateLocalLimitSuggestions(error);
                break;

            case "ARGS_AFTER_DOTS":
                generateArgsAfterDotsSuggestions(error);
                break;

            case "COMPILE_ERROR":
            case "TOKENIZE_ERROR":
                generateCompileErrorSuggestions(error, message);
                break;

            default:
                generateGenericSuggestions(error, luaSource);
                break;
        }
    }

    /**
     * 生成缺少符号的修复建议
     */
    private static void generateMissSymbolSuggestions(ParseError error, String expectedSymbol, String luaSource) {
        if (expectedSymbol == null) return;

        switch (expectedSymbol) {
            case "(":
                // 检查是否是函数定义上下文
                String context = getErrorContext(luaSource, error.getStart(), 30);
                if (context.contains("function")) {
                    error.addSuggestion("在函数名后添加参数列表的左括号 '('")
                            .addSuggestion("函数定义语法: function name() ... end")
                            .addSuggestion("如果函数没有参数，仍需要空括号: function name() end");
                } else {
                    error.addSuggestion("添加缺少的左括号 '('")
                            .addSuggestion("检查是否有未配对的右括号 ')'")
                            .addSuggestion("确认函数调用或表达式的括号是否正确配对");
                }
                break;

            case ")":
                error.addSuggestion("添加缺少的右括号 ')'")
                        .addSuggestion("检查是否有未配对的左括号 '('")
                        .addSuggestion("确认函数调用或表达式的括号是否正确配对");
                break;

            case "}":
                error.addSuggestion("添加缺少的右大括号 '}'")
                        .addSuggestion("检查表构造语法是否正确")
                        .addSuggestion("确认所有的左大括号 '{' 都有对应的右大括号");
                break;

            case "]":
                error.addSuggestion("添加缺少的右方括号 ']'")
                        .addSuggestion("检查数组索引或表字段访问语法")
                        .addSuggestion("确认所有的左方括号 '[' 都有对应的右方括号");
                break;

            case "end":
                error.addSuggestion("添加缺少的 'end' 关键字")
                        .addSuggestion("检查 function、if、while、for 等语句块是否正确结束")
                        .addSuggestion("确认代码块的嵌套层次是否正确");
                break;

            case "then":
                error.addSuggestion("在 if 或 elseif 条件后添加 'then' 关键字")
                        .addSuggestion("语法: if condition then ... end");
                break;

            case "do":
                error.addSuggestion("在 while 条件后添加 'do' 关键字")
                        .addSuggestion("语法: while condition do ... end");
                break;

            case "=":
                error.addSuggestion("添加赋值操作符 '='")
                        .addSuggestion("检查变量赋值语法是否正确")
                        .addSuggestion("语法: local variable = value");
                break;

            case ",":
                error.addSuggestion("添加逗号 ',' 分隔参数或表元素")
                        .addSuggestion("检查函数参数列表或表构造语法");
                break;

            case "::":
                error.addSuggestion("添加标签结束符 '::'")
                        .addSuggestion("标签语法: ::labelname::");
                break;

            default:
                error.addSuggestion("添加缺少的符号: '" + expectedSymbol + "'")
                        .addSuggestion("检查语法是否符合Lua规范");
                break;
        }
    }

    /**
     * 生成缺少end的修复建议
     */
    private static void generateMissEndSuggestions(ParseError error, String luaSource) {
        error.addSuggestion("添加 'end' 关键字来结束代码块")
                .addSuggestion("检查 function、if、while、for、repeat 等语句是否正确结束")
                .addSuggestion("确认代码块的嵌套层次，每个开始关键字都需要对应的 'end'")
                .addSuggestion("使用代码编辑器的括号匹配功能检查配对");
    }

    /**
     * 生成缺少名称的修复建议
     */
    private static void generateMissNameSuggestions(ParseError error, String luaSource) {
        error.addSuggestion("提供一个有效的标识符名称")
                .addSuggestion("标识符必须以字母或下划线开头，后跟字母、数字或下划线")
                .addSuggestion("避免使用Lua保留关键字作为变量名")
                .addSuggestion("示例: myVariable, _private, user_name");
    }

    /**
     * 生成缺少表达式的修复建议
     */
    private static void generateMissExpSuggestions(ParseError error, String luaSource) {
        error.addSuggestion("提供一个有效的表达式")
                .addSuggestion("表达式可以是变量、字面量、函数调用或运算")
                .addSuggestion("检查是否有未完成的运算符")
                .addSuggestion("示例: 42, \"hello\", myFunction(), x + y");
    }

    /**
     * 生成未知符号的修复建议
     */
    private static void generateUnknownSymbolSuggestions(ParseError error, String unknownSymbol, String luaSource) {
        if (unknownSymbol == null) return;

        error.addSuggestion("移除或替换未知符号: '" + unknownSymbol + "'");

        // 检查是否是常见的拼写错误
        String suggestion = suggestSimilarSymbol(unknownSymbol);
        if (suggestion != null) {
            error.addSuggestion("可能是拼写错误，建议使用: '" + suggestion + "'");
        }

        // 检查是否是其他语言的语法
        switch (unknownSymbol) {
            case "++":
            case "--":
                error.addSuggestion("Lua不支持 ++ 或 -- 操作符，使用 x = x + 1 或 x = x - 1");
                break;
            case "&&":
                error.addSuggestion("使用 'and' 代替 '&&'");
                break;
            case "||":
                error.addSuggestion("使用 'or' 代替 '||'");
                break;
            case "!=":
                error.addSuggestion("使用 '~=' 代替 '!='");
                break;
            case "//":
                error.addSuggestion("单行注释使用 '--'，多行注释使用 '--[[ ]]'");
                break;
        }
    }

    /**
     * 生成意外符号的修复建议
     */
    private static void generateUnexpectedSymbolSuggestions(ParseError error, String unexpectedSymbol, String luaSource) {
        error.addSuggestion("移除意外的符号: '" + unexpectedSymbol + "'")
                .addSuggestion("检查语法结构是否正确")
                .addSuggestion("确认符号在当前上下文中是否合法");
    }

    /**
     * 生成关键字错误的修复建议
     */
    private static void generateKeywordSuggestions(ParseError error, String keyword, String luaSource) {
        error.addSuggestion("不能使用Lua保留关键字 '" + keyword + "' 作为变量名")
                .addSuggestion("选择一个不同的标识符名称")
                .addSuggestion("可以在名称前后添加下划线，如: _" + keyword + "_ 或 my" +
                        keyword.substring(0, 1).toUpperCase() + keyword.substring(1));
    }

    /**
     * 生成重定义标签的修复建议
     */
    private static void generateRedefinedLabelSuggestions(ParseError error, String labelName) {
        error.addSuggestion("标签 '" + labelName + "' 已经定义，请使用不同的标签名")
                .addSuggestion("在同一作用域内，每个标签名只能定义一次")
                .addSuggestion("考虑使用更具描述性的标签名，如: " + labelName + "_1, " + labelName + "_loop 等");
    }

    /**
     * 生成return后有语句的修复建议
     */
    private static void generateActionAfterReturnSuggestions(ParseError error) {
        error.addSuggestion("移除 return 语句后的代码，因为它们永远不会被执行")
                .addSuggestion("如果需要条件返回，使用 if 语句包装 return")
                .addSuggestion("将 return 语句移到函数的最后");
    }

    /**
     * 生成局部变量限制的修复建议
     */
    private static void generateLocalLimitSuggestions(ParseError error) {
        error.addSuggestion("减少局部变量的数量，Lua限制每个函数最多200个局部变量")
                .addSuggestion("考虑将一些变量合并到表中")
                .addSuggestion("将大函数拆分为多个小函数")
                .addSuggestion("重用变量名，让变量在不同的作用域中使用");
    }

    /**
     * 生成可变参数后有参数的修复建议
     */
    private static void generateArgsAfterDotsSuggestions(ParseError error) {
        error.addSuggestion("可变参数 '...' 必须是参数列表的最后一个参数")
                .addSuggestion("将 '...' 移到参数列表的末尾")
                .addSuggestion("语法: function name(arg1, arg2, ...)");
    }

    /**
     * 生成编译错误的修复建议
     */
    private static void generateCompileErrorSuggestions(ParseError error, String message) {
        error.addSuggestion("检查代码语法是否符合Lua规范")
                .addSuggestion("确认所有的括号、引号、关键字都正确配对")
                .addSuggestion("使用Lua语法检查工具验证代码");

        if (message != null) {
            if (message.contains("UTF-8")) {
                error.addSuggestion("检查文件编码，确保使用UTF-8编码");
            }
            if (message.contains("unexpected")) {
                error.addSuggestion("检查是否有意外的字符或符号");
            }
        }
    }

    /**
     * 生成通用修复建议
     */
    private static void generateGenericSuggestions(ParseError error, String luaSource) {
        error.addSuggestion("检查代码语法是否正确")
                .addSuggestion("参考Lua官方文档确认语法规范")
                .addSuggestion("使用代码编辑器的语法高亮功能辅助检查");
    }

    /**
     * 建议相似的符号
     */
    private static String suggestSimilarSymbol(String unknown) {
        // 检查是否与关键字相似
        for (String keyword : LUA_KEYWORDS) {
            if (isStringSimilar(unknown, keyword)) {
                return keyword;
            }
        }

        // 检查是否与常见函数相似
        for (String func : COMMON_FUNCTIONS) {
            if (isStringSimilar(unknown, func)) {
                return func;
            }
        }

        return null;
    }

    /**
     * 检查两个字符串是否相似（简单的编辑距离）
     */
    private static boolean isStringSimilar(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        if (Math.abs(s1.length() - s2.length()) > 2) return false;

        int distance = calculateEditDistance(s1.toLowerCase(), s2.toLowerCase());
        return distance <= Math.max(1, Math.min(s1.length(), s2.length()) / 3);
    }

    /**
     * 计算编辑距离
     */
    private static int calculateEditDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * 为Android Lua开发生成特定建议
     */
    public static void generateAndroidSpecificSuggestions(ParseError error, String luaSource) {
        String errorType = error.getType();

        if ("MISS_SYMBOL".equals(errorType) && ")".equals(error.getMessage())) {
            // 检查是否是Android API调用
            String context = getErrorContext(luaSource, error.getStart(), 50);
            if (context.contains("findViewById") || context.contains("setContentView") ||
                    context.contains("setOnClickListener")) {
                error.addSuggestion("检查Android API调用的括号配对")
                        .addSuggestion("确认R.id.xxx或R.layout.xxx等资源引用语法正确")
                        .addSuggestion("示例: findViewById(R.id.button)");
            }
        }

        if ("UNKNOWN_SYMBOL".equals(errorType)) {
            String symbol = error.getMessage();
            if (symbol != null && symbol.contains("R.")) {
                error.addSuggestion("检查资源引用语法，确保资源文件存在")
                        .addSuggestion("Android资源语法: R.id.name, R.layout.name, R.string.name");
            }
        }
    }

    /**
     * 获取错误上下文
     */
    private static String getErrorContext(String source, int position, int length) {
        if (source == null || position < 0) return "";

        int start = Math.max(0, position - length / 2);
        int end = Math.min(source.length(), position + length / 2);

        return source.substring(start, end);
    }
}
