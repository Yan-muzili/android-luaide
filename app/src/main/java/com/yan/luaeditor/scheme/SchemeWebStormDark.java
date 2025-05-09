package com.yan.luaeditor.scheme;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class SchemeWebStormDark extends EditorColorScheme {
    public SchemeWebStormDark() {
        super(true);
    }

    @Override
    public void applyDefault() {
        super.applyDefault();
        setColor(ANNOTATION, 0xff999999);
        setColor(FUNCTION_NAME, 0xffdcdcdc);
        setColor(IDENTIFIER_NAME, 0xffdcdcdc);
        setColor(IDENTIFIER_VAR, 0xff9876aa);
        setColor(LITERAL, 0xff6a8759);
        setColor(OPERATOR, 0xffdcdcdc);
        setColor(COMMENT, 0xff808080);
        setColor(KEYWORD, 0xffcc7832);
        setColor(WHOLE_BACKGROUND, 0xff1c1c1c);
        setColor(TEXT_NORMAL, 0xffdcdcdc);
        setColor(LINE_NUMBER_BACKGROUND, 0xff252526);
        setColor(LINE_NUMBER, 0xff606366);
        setColor(LINE_NUMBER_CURRENT, 0xff606366);
        setColor(SELECTED_TEXT_BACKGROUND, 0xff3676b8);
        setColor(MATCHED_TEXT_BACKGROUND, 0xff32593d);
        setColor(CURRENT_LINE, 0xff323232);
        setColor(SELECTION_INSERT, 0xffdcdcdc);
        setColor(SELECTION_HANDLE, 0xffdcdcdc);
        setColor(BLOCK_LINE, 0xff575757);
        setColor(BLOCK_LINE_CURRENT, 0xdd575757);
        setColor(TEXT_SELECTED, 0xffffffff);
    }
}