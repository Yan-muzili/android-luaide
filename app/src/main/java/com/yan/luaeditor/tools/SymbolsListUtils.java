//code by AI
package com.yan.luaeditor.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SymbolsListUtils {
    /**
     * 将字符串列表写入文件
     * @param list 要保存的字符串列表
     * @param filePath 文件路径
     * @throws IOException 如果文件操作失败
     */
    public static void writeListToFile(List<String> list, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            for (String line : list) {
                writer.write(line);
                writer.newLine(); // 写入换行符
            }
        }
    }

    /**
     * 从文件读取字符串列表
     * @param filePath 文件路径
     * @return 读取的字符串列表
     * @throws IOException 如果文件操作失败
     */
    public static List<String> readListFromFile(String filePath) throws IOException {
        List<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }

}
