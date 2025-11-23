package com.luaparser;

import com.luaparser.parser.ParseError;
import com.luaparser.parser.ParseState;
import com.luaparser.utils.LineTracker;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 错误位置辅助类 - 帮助获取和格式化错误位置信息
 */
public class ErrorLocationHelper {

    /**
     * 错误位置信息类
     */
    public static class ErrorLocation {
        private final ParseError error;
        private final int line;
        private final int column;
        private final int endLine;
        private final int endColumn;
        private final String lineText;
        private final String contextBefore;
        private final String contextAfter;
        private final String errorText; // 错误范围内的文本

        public ErrorLocation(ParseError error, int line, int column, int endLine, int endColumn,
                             String lineText, String contextBefore, String contextAfter, String errorText) {
            this.error = error;
            this.line = line;
            this.column = column;
            this.endLine = endLine;
            this.endColumn = endColumn;
            this.lineText = lineText;
            this.contextBefore = contextBefore;
            this.contextAfter = contextAfter;
            this.errorText = errorText;
        }

        public ParseError getError() { return error; }
        public int getLine() { return line; }
        public int getColumn() { return column; }
        public int getEndLine() { return endLine; }
        public int getEndColumn() { return endColumn; }
        public String getLineText() { return lineText; }
        public String getContextBefore() { return contextBefore; }
        public String getContextAfter() { return contextAfter; }
        public String getErrorText() { return errorText; }

        /**
         * 检查错误是否跨越多行
         */
        public boolean isMultiLine() {
            return line != endLine;
        }

        /**
         * 获取错误的文本范围描述
         */
        public String getRangeDescription() {
            if (isMultiLine()) {
                return String.format("lines %d-%d, columns %d-%d", line, endLine, column, endColumn);
            } else {
                if (column == endColumn) {
                    return String.format("line %d, column %d", line, column);
                } else {
                    return String.format("line %d, columns %d-%d", line, column, endColumn);
                }
            }
        }

        @Override
        public String toString() {
            return String.format("Line %d, Column %d: %s", line, column, error.getType());
        }

        /**
         * 获取详细的错误信息
         */
        public String getDetailedMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Error: %s at %s\n", error.getType(), getRangeDescription()));
            sb.append(String.format("Text Range: %d-%d (length %d)\n",
                    error.getStart(), error.getFinish(), error.getLength()));

            if (error.getMessage() != null) {
                sb.append("Message: ").append(error.getMessage()).append("\n");
            }

            if (errorText != null && !errorText.isEmpty()) {
                sb.append("Error Text: '").append(errorText).append("'\n");
            }

            // 添加修复建议
            if (error.hasSuggestions()) {
                sb.append("💡 Suggestions:\n");
                List<String> suggestions = error.getSuggestions();
                for (int i = 0; i < suggestions.size(); i++) {
                    sb.append("   ").append(i + 1).append(". ").append(suggestions.get(i)).append("\n");
                }
            }

            if (lineText != null && !lineText.isEmpty()) {
                sb.append("Line: ").append(lineText).append("\n");

                // 添加指示符
                sb.append("      ");
                for (int i = 1; i < column; i++) {
                    sb.append(" ");
                }
                if (isMultiLine() || column == endColumn) {
                    sb.append("^\n");
                } else {
                    // 显示错误范围
                    for (int i = column; i <= endColumn; i++) {
                        sb.append("^");
                    }
                    sb.append("\n");
                }
            }

            return sb.toString();
        }

        /**
         * 获取带上下文的错误信息
         */
        public String getContextualMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append(getDetailedMessage());

            if (contextBefore != null && !contextBefore.isEmpty()) {
                sb.append("Context before: '").append(contextBefore).append("'\n");
            }

            if (contextAfter != null && !contextAfter.isEmpty()) {
                sb.append("Context after: '").append(contextAfter).append("'\n");
            }

            return sb.toString();
        }
    }

    /**
     * 从解析状态获取所有错误位置
     */
    public static List<ErrorLocation> getErrorLocations(ParseState state) {
        if (state == null || !state.hasErrors()) {
            return List.of();
        }

        String luaSource = state.getLua();
        int[] lines = state.getLines();
        LineTracker lineTracker = new LineTracker();

        return state.getErrors().stream()
                .map(error -> createErrorLocation(error, luaSource, lines, lineTracker))
                .collect(Collectors.toList());
    }

    /**
     * 从单个错误创建错误位置信息
     */
    public static ErrorLocation createErrorLocation(ParseError error, String luaSource,
                                                    int[] lines, LineTracker lineTracker) {
        if (error == null || luaSource == null) {
            return new ErrorLocation(error, 0, 0, 0, 0, "", "", "", "");
        }

        // 获取错误开始位置的行列信息
        LineTracker.Position startPos = lineTracker.getPosition(luaSource, error.getStart());
        int line = startPos.getRow();
        int column = startPos.getCol();

        // 获取错误结束位置的行列信息
        LineTracker.Position endPos = lineTracker.getPosition(luaSource, error.getFinish());
        int endLine = endPos.getRow();
        int endColumn = endPos.getCol();

        // 获取错误所在行的文本
        String lineText = getLineText(luaSource, line);

        // 获取错误范围内的文本
        String errorText = getTextInRange(luaSource, error.getStart(), error.getFinish()+1);

        // 获取上下文
        String contextBefore = getContextBefore(luaSource, error.getStart(), 20);
        String contextAfter = getContextAfter(luaSource, error.getFinish(), 20);

        return new ErrorLocation(error, line, column, endLine, endColumn,
                lineText, contextBefore, contextAfter, errorText);
    }

    /**
     * 获取指定范围内的文本
     */
    public static String getTextInRange(String text, int start, int end) {
        if (text == null || start < 0 || end < start) {
            return "";
        }

        int actualStart = Math.max(0, Math.min(start, text.length()));
        int actualEnd = Math.max(actualStart, Math.min(end, text.length()));

        if (actualStart >= actualEnd) {
            return "";
        }

        return text.substring(actualStart, actualEnd);
    }

    /**
     * 获取指定行的文本
     */
    public static String getLineText(String text, int lineNumber) {
        if (text == null || lineNumber < 1) {
            return "";
        }

        String[] lines = text.split("\r?\n");
        if (lineNumber <= lines.length) {
            return lines[lineNumber - 1];
        }

        return "";
    }

    /**
     * 获取指定位置之前的上下文
     */
    public static String getContextBefore(String text, int position, int maxLength) {
        if (text == null || position <= 0) {
            return "";
        }

        int start = Math.max(0, position - maxLength);
        int end = Math.min(position, text.length());

        if (start >= end) {
            return "";
        }

        return text.substring(start, end).trim();
    }

    /**
     * 获取指定位置之后的上下文
     */
    public static String getContextAfter(String text, int position, int maxLength) {
        if (text == null || position >= text.length()) {
            return "";
        }

        int start = Math.max(0, position);
        int end = Math.min(position + maxLength, text.length());

        if (start >= end) {
            return "";
        }

        return text.substring(start, end).trim();
    }

    /**
     * 格式化所有错误为可读的字符串
     */
    public static String formatErrors(ParseState state) {
        List<ErrorLocation> locations = getErrorLocations(state);
        if (locations.isEmpty()) {
            return "No errors found.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(locations.size()).append(" error(s):\n\n");

        for (int i = 0; i < locations.size(); i++) {
            ErrorLocation location = locations.get(i);
            sb.append(String.format("[%d] %s", i + 1, location.getDetailedMessage()));
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 格式化错误为IDE友好的格式
     */
    public static String formatErrorsForIDE(ParseState state) {
        List<ErrorLocation> locations = getErrorLocations(state);
        if (locations.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (ErrorLocation location : locations) {
            ParseError error = location.getError();
            sb.append(String.format("%s:%d:%d: error: %s",
                    state.getUri() != null ? state.getUri() : "file",
                    location.getLine(),
                    location.getColumn(),
                    error.getType()));

            if (error.getMessage() != null) {
                sb.append(" - ").append(error.getMessage());
            }

            sb.append(String.format(" [%d-%d]", error.getStart(), error.getFinish()));
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 格式化错误为JSON格式
     */
    public static String formatErrorsAsJSON(ParseState state) {
        List<ErrorLocation> locations = getErrorLocations(state);
        if (locations.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < locations.size(); i++) {
            ErrorLocation location = locations.get(i);
            ParseError error = location.getError();

            if (i > 0) sb.append(",\n");

            sb.append("  {\n");
            sb.append("    \"type\": \"").append(error.getType()).append("\",\n");
            sb.append("    \"message\": \"").append(error.getMessage() != null ? error.getMessage() : "").append("\",\n");
            sb.append("    \"startOffset\": ").append(error.getStart()).append(",\n");
            sb.append("    \"endOffset\": ").append(error.getFinish()).append(",\n");
            sb.append("    \"length\": ").append(error.getLength()).append(",\n");
            sb.append("    \"startLine\": ").append(location.getLine()).append(",\n");
            sb.append("    \"startColumn\": ").append(location.getColumn()).append(",\n");
            sb.append("    \"endLine\": ").append(location.getEndLine()).append(",\n");
            sb.append("    \"endColumn\": ").append(location.getEndColumn()).append(",\n");
            sb.append("    \"errorText\": \"").append(escapeJson(location.getErrorText())).append("\",\n");
            sb.append("    \"lineText\": \"").append(escapeJson(location.getLineText())).append("\"\n");
            sb.append("    \"suggestions\": [");
            List<String> suggestions = location.getError().getSuggestions();
            for (int j = 0; j < suggestions.size(); j++) {
                if (j > 0) sb.append(", ");
                sb.append("\"").append(escapeJson(suggestions.get(j))).append("\"");
            }
            sb.append("],\n");
            sb.append("  }");
        }

        sb.append("\n]");
        return sb.toString();
    }

    /**
     * 转义JSON字符串
     */
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 检查指定位置是否有错误
     */
    public static boolean hasErrorAt(ParseState state, int line, int column) {
        if (state == null || !state.hasErrors()) {
            return false;
        }

        List<ErrorLocation> locations = getErrorLocations(state);
        return locations.stream()
                .anyMatch(loc -> loc.getLine() == line && loc.getColumn() == column);
    }

    /**
     * 检查指定文本偏移量是否有错误
     */
    public static boolean hasErrorAtOffset(ParseState state, int offset) {
        if (state == null || !state.hasErrors()) {
            return false;
        }

        return state.getErrors().stream()
                .anyMatch(error -> error.contains(offset));
    }

    /**
     * 获取指定位置的错误
     */
    public static List<ErrorLocation> getErrorsAt(ParseState state, int line, int column) {
        if (state == null || !state.hasErrors()) {
            return List.of();
        }

        List<ErrorLocation> locations = getErrorLocations(state);
        return locations.stream()
                .filter(loc -> loc.getLine() == line && loc.getColumn() == column)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定文本偏移量的错误
     */
    public static List<ParseError> getErrorsAtOffset(ParseState state, int offset) {
        if (state == null || !state.hasErrors()) {
            return List.of();
        }

        return state.getErrors().stream()
                .filter(error -> error.contains(offset))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定范围内的错误
     */
    public static List<ErrorLocation> getErrorsInRange(ParseState state, int startOffset, int endOffset) {
        if (state == null || !state.hasErrors()) {
            return List.of();
        }

        return state.getErrors().stream()
                .filter(error -> error.overlaps(startOffset, endOffset))
                .map(error -> createErrorLocation(error, state.getLua(), state.getLines(), new LineTracker()))
                .collect(Collectors.toList());
    }

    /**
     * 按严重程度排序错误
     */
    public static List<ErrorLocation> sortErrorsBySeverity(List<ErrorLocation> errors) {
        return errors.stream()
                .sorted((a, b) -> {
                    // 按错误类型的严重程度排序
                    int severityA = getErrorSeverity(a.getError().getType());
                    int severityB = getErrorSeverity(b.getError().getType());

                    if (severityA != severityB) {
                        return Integer.compare(severityB, severityA); // 高严重度在前
                    }

                    // 相同严重度按位置排序
                    if (a.getLine() != b.getLine()) {
                        return Integer.compare(a.getLine(), b.getLine());
                    }

                    return Integer.compare(a.getColumn(), b.getColumn());
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取错误严重程度
     */
    private static int getErrorSeverity(String errorType) {
        return switch (errorType) {
            case "COMPILE_ERROR", "TOKENIZE_ERROR" -> 10; // 最高
            case "MISS_END", "MISS_SYMBOL" -> 8;
            case "MISS_NAME", "MISS_EXP" -> 6;
            case "UNKNOWN_SYMBOL", "UNEXPECT_SYMBOL" -> 4;
            case "ACTION_AFTER_RETURN", "REDEFINED_LABEL" -> 3;
            case "LOCAL_LIMIT", "ARGS_AFTER_DOTS" -> 2;
            default -> 1; // 最低
        };
    }
}
