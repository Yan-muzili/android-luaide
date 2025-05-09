package com.yan.luaeditor;

import java.util.Stack;



public class MyPrefixChecker implements CompletionHelper.PrefixChecker {

    @Override
    public boolean check(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '.' || ch == '_'||ch=='('||ch==')'||ch=='$';
    }
}
