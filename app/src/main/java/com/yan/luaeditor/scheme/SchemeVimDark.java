package com.yan.luaeditor.scheme;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class SchemeVimDark extends EditorColorScheme {
    public SchemeVimDark() {
        super(true);
    }

    @Override
    public void applyDefault() {
        super.applyDefault();
        setColor(ANNOTATION, 0xff87ceeb);
        setColor(FUNCTION_NAME, 0xfff92672);
        setColor(IDENTIFIER_NAME, 0xfff8f8f2);
        setColor(IDENTIFIER_VAR, 0xfff8f8f2);
        setColor(LITERAL, 0xffae81ff);
        setColor(OPERATOR, 0xfff8f8f2);
        setColor(COMMENT, 0xff75715e);
        setColor(KEYWORD, 0xff66d9ef);
        setColor(WHOLE_BACKGROUND, 0xff000000);
        setColor(TEXT_NORMAL, 0xfff8f8f2);
        setColor(LINE_NUMBER_BACKGROUND, 0xff111111);
        setColor(LINE_NUMBER, 0xff75715e);
        setColor(LINE_NUMBER_CURRENT, 0xff75715e);
        setColor(SELECTED_TEXT_BACKGROUND, 0xff444444);
        setColor(MATCHED_TEXT_BACKGROUND, 0xff333333);
        setColor(CURRENT_LINE, 0xff222222);
        setColor(SELECTION_INSERT, 0xfff8f8f2);
        setColor(SELECTION_HANDLE, 0xfff8f8f2);
        setColor(BLOCK_LINE, 0xff444444);
        setColor(BLOCK_LINE_CURRENT, 0xdd444444);
        setColor(TEXT_SELECTED, 0xffffffff);
    }
}
