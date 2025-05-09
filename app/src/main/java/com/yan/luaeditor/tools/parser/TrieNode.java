package com.yan.luaeditor.tools.parser;

import java.util.HashMap;
import java.util.Map;

class TrieNode {
    Map<Character, TrieNode> children;
    boolean isEndOfWord;
    String key;

    public TrieNode() {
        children = new HashMap<>();
        isEndOfWord = false;
        key = null;
    }
}    