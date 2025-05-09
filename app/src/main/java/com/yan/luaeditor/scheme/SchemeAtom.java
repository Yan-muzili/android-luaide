package com.yan.luaeditor.scheme;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class SchemeAtom extends EditorColorScheme {
    public SchemeAtom() {
        super(false);
    }

    @Override
    public void applyDefault() {
        super.applyDefault();
        setColor(ANNOTATION, 0xff50a14f);
        setColor(FUNCTION_NAME, 0xff007acc);
        setColor(IDENTIFIER_NAME, 0xff007acc);
        setColor(IDENTIFIER_VAR, 0xff007acc);
        setColor(LITERAL, 0xffa626a4);
        setColor(OPERATOR, 0xff007acc);
        setColor(COMMENT, 0xff959da5);
        setColor(KEYWORD, 0xffa626a4);
        setColor(WHOLE_BACKGROUND, 0xffffffff);
        setColor(TEXT_NORMAL, 0xff282c34);
        setColor(LINE_NUMBER_BACKGROUND, 0xffeaeaea);
        setColor(LINE_NUMBER, 0xff959da5);
        setColor(LINE_NUMBER_CURRENT, 0xff959da5);
        setColor(SELECTED_TEXT_BACKGROUND, 0xffd1d1d1);
        setColor(MATCHED_TEXT_BACKGROUND, 0xffe1e1e1);
        setColor(CURRENT_LINE, 0xfff0f0f0);
        setColor(SELECTION_INSERT, 0xff50a14f);
        setColor(SELECTION_HANDLE, 0xff50a14f);
        setColor(BLOCK_LINE, 0xffd1d1d1);
        setColor(BLOCK_LINE_CURRENT, 0);
        setColor(TEXT_SELECTED, 0xffffffff);
    }
}