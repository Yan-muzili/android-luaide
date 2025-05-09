/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 */
package com.yan.luaeditor.lualanguage;

import static java.lang.Character.isWhitespace;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yan.luaeditor.CompletionHelper;
import com.yan.luaeditor.CompletionName;
import com.yan.luaeditor.MyIdentifierAutoComplete;
import com.yan.luaeditor.MyPrefixChecker;
import com.yan.luaeditor.tools.parser.LuaLexer;
import com.yan.luaeditor.tools.parser.LuaParser;
import com.yan.luaeditor.tools.parser.Token;
import com.yan.luaide.LuaUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.rosemoe.sora.lang.EmptyLanguage;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lang.QuickQuoteHandler;
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;
import io.github.rosemoe.sora.lang.format.Formatter;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandleResult;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.lang.styling.StylesUtils;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.text.TextUtils;
import io.github.rosemoe.sora.widget.SymbolPairMatch;

/**
 * Java language. Simple implementation.
 *
 * @author Rosemoe
 */
public class LuaLanguage implements Language {

    private static final CodeSnippet FOR_SNIPPET =
            CodeSnippetParser.parse("for i = 1,num do\n    $0\nend");
    private static final CodeSnippet STATIC_CONST_SNIPPET =
            CodeSnippetParser.parse(
                    "private final static ${1:type} ${2/(.*)/${1:/upcase}/} = ${3:value};");
    private static final CodeSnippet CLIPBOARD_SNIPPET = CodeSnippetParser.parse("${1:${CLIPBOARD}}");

    private MyIdentifierAutoComplete autoComplete;
    private final LuaIncrementalAnalyzeManager manager;
    private final LuaQuoteHandler javaQuoteHandler = new LuaQuoteHandler();

    HashMap<String, HashMap<String, CompletionName>> baseMap;
    HashMap<String, List<String>> classMap;
    public LuaLanguage(HashMap<String, HashMap<String, CompletionName>> map, HashMap<String, List<String>> classmap) {
        autoComplete = new MyIdentifierAutoComplete(LuaTextTokenizer.sKeywords, map);
        manager = new LuaIncrementalAnalyzeManager();
        this.baseMap = map;
        this.classMap=classmap;
        // edit.setDiagnostics(manager.diagnosticsContainer);
    }

    public LuaLanguage(String[] keywords, HashMap<String, HashMap<String, CompletionName>> map,HashMap<String, List<String>> classmap) {
        autoComplete = new MyIdentifierAutoComplete(keywords, map);
        manager = new LuaIncrementalAnalyzeManager();
        this.baseMap = map;
        this.classMap=classmap;
        // edit.setDiagnostics(manager.diagnosticsContainer);
    }


    @NonNull
    @Override
    public AnalyzeManager getAnalyzeManager() {
        return manager;
    }

    @Nullable
    @Override
    public QuickQuoteHandler getQuickQuoteHandler() {
        return javaQuoteHandler;
    }

    @Override
    public void destroy() {
        autoComplete = null;
    }

    @Override
    public int getInterruptionLevel() {
        return INTERRUPTION_LEVEL_STRONG;
    }

    HashMap<String, String> map = new HashMap<>();

    @Override
    public void requireAutoComplete(
            @NonNull ContentReference content,
            @NonNull CharPosition position,
            @NonNull CompletionPublisher publisher,
            @NonNull Bundle extraArguments) {
        try {
            var prefix = CompletionHelper.computePrefix(content, position, new MyPrefixChecker());
            final var idt = manager.identifiers;
            if (idt != null) {
                //System.out.println("lll2");
                if (!prefix.equals("")) {
                    try {
                        //System.out.println("lll3");
                        com.yan.luaeditor.format.LuaLexer le= new com.yan.luaeditor.format.LuaLexer(content);
                        //System.out.println("lll4"+le.yytext());
                        /**
                        * 最耗时操作
                        * */
                        LuaLexer lexer;
                        List<Token> tokens=null;
                        try {
                            lexer = new LuaLexer(content.toString());
                            tokens = lexer.tokenize();
                        } catch (RuntimeException e) {
                            System.out.println(e.getMessage());
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                        //LuaUtil.save2("/sdcard/Luaide/yyy.log",tokens.toString());
                        //System.out.println(tokens.toString());
                        try {
                            LuaParser parser = new LuaParser(tokens);
                            HashMap<String,HashMap<String,CompletionName>> importmap=new HashMap<>();
                            //long startTime = System.currentTimeMillis();
                            Set<String> baseKeys = new HashSet<>(baseMap.keySet());
                            for (String str : parser.parseImports()) {
                                String ss = str.replace(".*", "");
                                for (String key : baseKeys) {
                                    if (key.startsWith(ss)) {
                                        importmap.put(key, baseMap.get(key));
                                    }
                                }
                            }
                            //long endTime = System.currentTimeMillis();
                            //long duration = endTime - startTime;
                            //System.out.println("代码运行时长: " + duration + " 毫秒");
                            autoComplete.setMmap((HashMap<String, String>) parser.parseVariables());
                            autoComplete.setClassmap(classMap);
                            autoComplete.setImportlist(importmap);
                            //LuaUtil.save2("/sdcard/Luaide/luaide.log", importmap.toString());
                        } catch (Exception e) {
                            LuaUtil.save2("/sdcard/Luaide/luaide.log", e.getMessage());
                        }
                    } catch (Exception e) {
                        LuaUtil.save2("/sdcard/Luaide/luaide.log", e.getMessage());
                    }
                    autoComplete.requireAutoComplete(content, position, prefix, publisher, idt);
                }
            }
            if ("fori".startsWith(prefix) && prefix.length() > 0) {
                publisher.addItem(
                        new SimpleSnippetCompletionItem(
                                "fori",
                                "Snippet - For loop on index",
                                new SnippetDescription(prefix.length(), FOR_SNIPPET, true)));
            }
        } catch (Exception e) {
            LuaUtil.save2("/sdcard/Luaide/yyy.log", e.getMessage());
        }
    }


    @Override
    public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {
        var content = text.getLine(line).substring(0, column);
        return getIndentAdvance(content);
    }

    private int getIndentAdvance(String content) {
        LuaTextTokenizer t = new LuaTextTokenizer(content);
        Tokens token;
        int advance = 0;
        while ((token = t.nextToken()) != Tokens.EOF) {
            if (token == Tokens.FUNCTION || token == Tokens.FOR || token == Tokens.SWITCH || token == Tokens.CASE || token == Tokens.WHILE || token == Tokens.IF || token == Tokens.UNTIL || token == Tokens.DO) {
                advance++;
            } else if (token == Tokens.END || token == Tokens.RETURN || token == Tokens.BREAK) {
                advance--;
            }
        }
        advance = Math.max(0, advance);
        return advance * 4;
    }

    private final NewlineHandler[] newlineHandlers = new NewlineHandler[]{new BraceHandler()};

    @Override
    public boolean useTab() {
        return false;
    }

    @NonNull
    @Override
    public Formatter getFormatter() {
        return EmptyLanguage.EmptyFormatter.INSTANCE;
    }

    @Override
    public SymbolPairMatch getSymbolPairs() {
        return new SymbolPairMatch.DefaultSymbolPairs();
    }

    @Override
    public NewlineHandler[] getNewlineHandlers() {
        return newlineHandlers;
    }

    private static String getNonEmptyTextBefore(CharSequence text, int index, int length) {
        while (index > 0 && isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        return text.subSequence(Math.max(0, index - length), index).toString();
    }

    private static String getNonEmptyTextAfter(CharSequence text, int index, int length) {
        while (index < text.length() && isWhitespace(text.charAt(index))) {
            index++;
        }
        return text.subSequence(index, Math.min(index + length, text.length())).toString();
    }

    class BraceHandler implements NewlineHandler {

        @Override
        public boolean matchesRequirement(
                @NonNull Content text, @NonNull CharPosition position, @Nullable Styles style) {
            var line = text.getLine(position.line);
            return !StylesUtils.checkNoCompletion(style, position)
                    && (getNonEmptyTextBefore(line, position.column, 1).equals("function")
                    || getNonEmptyTextBefore(line, position.column, 1).equals("for")
                    || getNonEmptyTextBefore(line, position.column, 1).equals("while")
                    || getNonEmptyTextBefore(line, position.column, 1).equals("if")
                    || getNonEmptyTextBefore(line, position.column, 1).equals("repeat"))
                    || getNonEmptyTextBefore(line, position.column, 1).equals("switch")
                    && getNonEmptyTextAfter(line, position.column, 1).equals("end");
        }

        @NonNull
        @Override
        public NewlineHandleResult handleNewline(
                @NonNull Content text,
                @NonNull CharPosition position,
                @Nullable Styles style,
                int tabSize) {
            var line = text.getLine(position.line);
            int index = position.column;
            var beforeText = line.subSequence(0, index).toString();
            var afterText = line.subSequence(index, line.length()).toString();
            return handleNewline(beforeText, afterText, tabSize);
        }

        @NonNull
        public NewlineHandleResult handleNewline(String beforeText, String afterText, int tabSize) {
            int count = TextUtils.countLeadingSpaceCount(beforeText, tabSize);
            int advanceBefore = getIndentAdvance(beforeText);
            int advanceAfter = getIndentAdvance(afterText);
            String text;
            StringBuilder sb =
                    new StringBuilder("\n")
                            .append(TextUtils.createIndent(count + advanceBefore, tabSize, useTab()))
                            .append('\n')
                            .append(text = TextUtils.createIndent(count + advanceAfter, tabSize, useTab()));
            int shiftLeft = text.length() + 1;
            return new NewlineHandleResult(sb, shiftLeft);
        }
    }
}
