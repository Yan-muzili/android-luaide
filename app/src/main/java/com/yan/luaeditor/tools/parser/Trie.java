package com.yan.luaeditor.tools.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Trie {
    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String key) {
        TrieNode node = root;
        for (char ch : key.toCharArray()) {
            node.children.putIfAbsent(ch, new TrieNode());
            node = node.children.get(ch);
        }
        node.isEndOfWord = true;
        node.key = key;
    }

    public List<String> searchPrefix(String prefix) {
        List<String> result = new ArrayList<>();
        TrieNode node = root;
        for (char ch : prefix.toCharArray()) {
            if (!node.children.containsKey(ch)) {
                return result;
            }
            node = node.children.get(ch);
        }
        collectKeys(node, result);
        return result;
    }

    private void collectKeys(TrieNode node, List<String> result) {
        if (node.isEndOfWord) {
            result.add(node.key);
        }
        for (TrieNode child : node.children.values()) {
            collectKeys(child, result);
        }
    }
}    