package com.yan.luaeditor.scheme;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class SchemeSublime extends EditorColorScheme {
    public SchemeSublime() {
        super(false);
    }

    @Override
    public void applyDefault() {
        super.applyDefault();
        setColor(ANNOTATION, 0xff999999);
        setColor(FUNCTION_NAME, 0xff0086b3);
        setColor(IDENTIFIER_NAME, 0xff0086b3);
        setColor(IDENTIFIER_VAR, 0xff0086b3);
        setColor(LITERAL, 0xffa31515);
        setColor(OPERATOR, 0xff0086b3);
        setColor(COMMENT, 0xff999999);
        setColor(KEYWORD, 0xff569cd6);
        setColor(WHOLE_BACKGROUND, 0xffffffff);
        setColor(TEXT_NORMAL, 0xff000000);
        setColor(LINE_NUMBER_BACKGROUND, 0xffe8e8e8);
        setColor(LINE_NUMBER, 0xff999999);
        setColor(LINE_NUMBER_CURRENT, 0xff999999);
        setColor(SELECTED_TEXT_BACKGROUND, 0xffc6e2ff);
        setColor(MATCHED_TEXT_BACKGROUND, 0xfff9f9f9);
        setColor(CURRENT_LINE, 0xfff0f0f0);
        setColor(SELECTION_INSERT, 0xff0086b3);
        setColor(SELECTION_HANDLE, 0xff0086b3);
        setColor(BLOCK_LINE, 0xffd8d8d8);
        setColor(BLOCK_LINE_CURRENT, 0);
        setColor(TEXT_SELECTED, 0xffffffff);
    }
}