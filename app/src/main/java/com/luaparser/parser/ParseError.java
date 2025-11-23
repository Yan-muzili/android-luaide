package com.luaparser.parser;

import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

/**
 * 解析错误类 - 完整对应compile.lua中的错误处理
 */
public class ParseError {
    private final String type;
    private final int start;
    private final int finish;
    private final String message;
    private final Object info;
    private final List<String> suggestions; // 修复建议

    public ParseError(String type, int start, int finish) {
        this(type, start, finish, null, null);
    }

    public ParseError(String type, int start, int finish, String message) {
        this(type, start, finish, message, null);
    }

    public ParseError(String type, int start, int finish, String message, Object info) {
        this.type = type;
        this.start = start;
        this.finish = finish;
        this.message = message;
        this.info = info;
        this.suggestions = new ArrayList<>();
    }

    public String getType() { return type; }
    public int getStart() { return start; }
    public int getFinish() { return finish; }
    public String getMessage() { return message; }
    public Object getInfo() { return info; }
    public List<String> getSuggestions() { return new ArrayList<>(suggestions); }

    /**
     * 添加修复建议
     */
    public ParseError addSuggestion(String suggestion) {
        if (suggestion != null && !suggestion.trim().isEmpty()) {
            suggestions.add(suggestion.trim());
        }
        return this;
    }

    /**
     * 添加多个修复建议
     */
    public ParseError addSuggestions(List<String> suggestions) {
        if (suggestions != null) {
            for (String suggestion : suggestions) {
                addSuggestion(suggestion);
            }
        }
        return this;
    }

    /**
     * 检查是否有修复建议
     */
    public boolean hasSuggestions() {
        return !suggestions.isEmpty();
    }

    /**
     * 获取错误的长度
     */
    public int getLength() {
        return Math.max(0, finish - start);
    }

    /**
     * 检查错误是否包含指定位置
     */
    public boolean contains(int position) {
        return start <= position && position <= finish;
    }

    /**
     * 检查错误是否与指定范围重叠
     */
    public boolean overlaps(int rangeStart, int rangeEnd) {
        return !(finish < rangeStart || start > rangeEnd);
    }

    /**
     * 获取错误在文本中的范围描述
     */
    public String getRangeDescription() {
        if (start == finish) {
            return String.format("position %d", start);
        } else {
            return String.format("range %d-%d (length %d)", start, finish, getLength());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ParseError{").append(type);
        sb.append(", ").append(start).append("-").append(finish);
        if (message != null) {
            sb.append(", ").append(message);
        }
        if (info != null) {
            sb.append(", ").append(info);
        }
        if (hasSuggestions()) {
            sb.append(", suggestions=").append(suggestions.size());
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 获取详细的错误描述
     */
    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Error Type: ").append(type).append("\n");
        sb.append("Text Range: ").append(getRangeDescription()).append("\n");
        if (message != null) {
            sb.append("Message: ").append(message).append("\n");
        }
        if (info != null) {
            sb.append("Additional Info: ").append(info).append("\n");
        }
        if (hasSuggestions()) {
            sb.append("Suggestions:\n");
            for (int i = 0; i < suggestions.size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(suggestions.get(i)).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ParseError)) return false;
        ParseError other = (ParseError) obj;
        return Objects.equals(type, other.type) &&
                start == other.start &&
                finish == other.finish &&
                Objects.equals(message, other.message) &&
                Objects.equals(info, other.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, start, finish, message, info);
    }
}
