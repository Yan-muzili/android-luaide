package com.yan.luaeditor.scheme;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class YluaScheme extends EditorColorScheme {
    public YluaScheme() {
        super(true);
    }
    @Override
    public void applyDefault() {
        super.applyDefault();
        setColor(EditorColorScheme.FUNCTION_NAME, -24064);
        setColor(EditorColorScheme.IDENTIFIER_VAR, -11801614);
        setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, 0xff292424);
        setColor(EditorColorScheme.LINE_NUMBER, -1);
        setColor(EditorColorScheme.LINE_DIVIDER, -1);
        setColor(EditorColorScheme.WHOLE_BACKGROUND, -14080988);
        setColor(EditorColorScheme.TEXT_NORMAL, 0xffffffff);
        setColor(EditorColorScheme.KEYWORD, -2081218);
        setColor(EditorColorScheme.LINE_NUMBER_CURRENT, -14080988);
        setColor(EditorColorScheme.CURRENT_LINE, 0x1E888888);
        setColor(EditorColorScheme.BLOCK_LINE_CURRENT, 0xff000000);
        setColor(EditorColorScheme.BLOCK_LINE, 0xff000000);
        setColor(EditorColorScheme.HIGHLIGHTED_DELIMITERS_FOREGROUND, -515);
        setColor(EditorColorScheme.SIDE_BLOCK_LINE, 0xff999999);
        setColor(EditorColorScheme.COMMENT, -13194665);
        setColor(EditorColorScheme.OPERATOR, -4095420);
        setColor(EditorColorScheme.LITERAL, 0xFF008080);
    }
}
