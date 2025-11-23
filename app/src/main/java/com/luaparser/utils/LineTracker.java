package com.luaparser.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 行位置跟踪器 - 完整对应lines.lua
 */
public class LineTracker {

    /**
     * 计算文本中每行的起始位置
     */
    public int[] calculateLines(String text) {
        if (text == null || text.isEmpty()) {
            return new int[]{0}; // 第一行从位置0开始（0基索引）
        }

        List<Integer> lines = new ArrayList<>();
        lines.add(0); // 第一行从位置0开始

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                lines.add(i + 1); // 下一行从i+1开始
            } else if (ch == '\r') {
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    lines.add(i + 2); // \r\n的情况，下一行从i+2开始
                    i++; // 跳过\n
                } else {
                    lines.add(i + 1); // 单独的\r，下一行从i+1开始
                }
            }
        }

        return lines.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * 位置类
     */
    public static class Position {
        public final int row;
        public final int col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() { return row; }
        public int getCol() { return col; }

        @Override
        public String toString() {
            return row + ":" + col;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Position)) return false;
            Position other = (Position) obj;
            return row == other.row && col == other.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    /**
     * 将位置拆分为行号和列号
     */
    public static Position rowColOf(int position) {
        return new Position(position / 10000, position % 10000);
    }

    /**
     * 将行列号合并为位置
     */
    public static int positionOf(int row, int col) {
        return row * 10000 + Math.min(col, 9999);
    }

    /**
     * 根据行信息将位置转换为偏移量
     */
    public static int positionToOffsetByLines(int[] lines, int position) {
        if (lines == null || lines.length == 0) return 0;

        Position pos = rowColOf(position);
        int row = pos.row;
        int col = pos.col;

        if (row < 0) return 0;
        if (row >= lines.length) {
            return lines[lines.length - 1] - 1;
        }

        int offset = lines[row] + col - 2; // 转换为0基索引

        // 确保不超过下一行的开始
        if (row + 1 < lines.length && offset >= lines[row + 1] - 1) {
            return lines[row + 1] - 2;
        }

        return Math.max(0, offset);
    }

    /**
     * 根据行信息将偏移量转换为位置
     */
    public static int offsetToPositionByLines(int[] lines, int offset) {
        if (lines == null || lines.length == 0) return positionOf(0, 1);

        offset = Math.max(0, offset);

        // 二分查找找到对应的行
        int left = 0;
        int right = lines.length - 1;
        int row = 0;

        while (left <= right) {
            int mid = (left + right) / 2;
            int lineStart = lines[mid] - 1; // 转换为0基索引

            if (mid == lines.length - 1 || offset < lines[mid + 1] - 1) {
                if (offset >= lineStart) {
                    row = mid;
                    break;
                } else {
                    right = mid - 1;
                }
            } else {
                left = mid + 1;
            }
        }

        int col = offset - (lines[row] - 1) + 1; // 转换为1基索引
        return positionOf(row, Math.max(1, col));
    }

    /**
     * 获取指定偏移量的行列位置
     */
    public Position getPosition(String text, int offset) {
        if (text == null || offset < 0) {
            return new Position(1, 1);
        }

        // 确保offset不超过文本长度
        offset = Math.min(offset, text.length());

        int line = 1;
        int col = 1;

        for (int i = 0; i < offset; i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                line++;
                col = 1;
            } else if (ch == '\r') {
                line++;
                col = 1;
                // 处理\r\n的情况
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++; // 跳过\n
                    // 如果跳过后超过了offset，需要调整
                    if (i >= offset) {
                        break;
                    }
                }
            } else {
                col++;
            }
        }

        return new Position(line, col);
    }

    /**
     * 获取指定行列位置的偏移量
     */
    public int getOffset(String text, int targetLine, int targetCol) {
        if (text == null || targetLine < 1 || targetCol < 1) {
            return 0;
        }

        int line = 1;
        int col = 1;

        for (int i = 0; i < text.length(); i++) {
            if (line == targetLine && col == targetCol) {
                return i;
            }

            char ch = text.charAt(i);
            if (ch == '\n') {
                line++;
                col = 1;
            } else if (ch == '\r') {
                line++;
                col = 1;
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++; // 跳过\n
                }
            } else {
                col++;
            }
        }

        return text.length();
    }
}
