package com.yan.luaeditor;

import java.util.Objects;

import io.github.rosemoe.sora.lang.completion.CompletionItemKind;

public class CompletionName {
    private final String name;
    private final CompletionItemKind type;
    private final String description;

    public CompletionName(String name, CompletionItemKind type, String description) {
        this.name = name;
        this.type = type;
        this.description = description == null ? "" : description;
    }

    public CompletionName(String name, CompletionItemKind type) {
        this(name, type, "");
    }

    public String getName() {
        return name;
    }

    public CompletionItemKind getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompletionName that = (CompletionName) o;
        return Objects.equals(name, that.name) && type == that.type && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, description);
    }

    @Override
    public String toString() {
        return "CompletionName{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", description='" + description + '\'' +
                '}';
    }
}    