package com.yan.luaeditor.scheme;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class SchemeGitHubDark extends EditorColorScheme {
    public SchemeGitHubDark() {
        super(true);
    }

    @Override
    public void applyDefault() {
        super.applyDefault();
        setColor(ANNOTATION, 0xffc792ea);
        setColor(FUNCTION_NAME, 0xffabb2bf);
        setColor(IDENTIFIER_NAME, 0xffabb2bf);
        setColor(IDENTIFIER_VAR, 0xffabb2bf);
        setColor(LITERAL, 0xff569cd6);
        setColor(OPERATOR, 0xff61afef);
        setColor(COMMENT, 0xff5c6370);
        setColor(KEYWORD, 0xffc678dd);
        setColor(WHOLE_BACKGROUND, 0xff282c34);
        setColor(TEXT_NORMAL, 0xffabb2bf);
        setColor(LINE_NUMBER_BACKGROUND, 0xff383c44);
        setColor(LINE_NUMBER, 0xff5c6370);
        setColor(LINE_NUMBER_CURRENT, 0xff5c6370);
        setColor(SELECTION_INSERT, 0xff3e4451);
        setColor(SELECTION_HANDLE, 0xff3e4451);
    }
}