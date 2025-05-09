package com.yan.luaeditor.tools;

import android.content.Context;

import com.yan.luaeditor.CompletionName;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.rosemoe.sora.lang.completion.CompletionItemKind;

public class CompleteHashmapUtils {
    // 序列化方法
    public static void saveHashMapToFile(Context context, HashMap<String, HashMap<String, CompletionName>> hashMap, String fileName) {
        try (FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream)) {

            // 写入外层 HashMap 的大小
            dataOutputStream.writeInt(hashMap.size());
            for (Map.Entry<String, HashMap<String, CompletionName>> outerEntry : hashMap.entrySet()) {
                // 写入外层键
                dataOutputStream.writeUTF(outerEntry.getKey());
                HashMap<String, CompletionName> innerHashMap = outerEntry.getValue();
                // 写入内层 HashMap 的大小
                dataOutputStream.writeInt(innerHashMap.size());
                for (Map.Entry<String, CompletionName> innerEntry : innerHashMap.entrySet()) {
                    // 写入内层键
                    dataOutputStream.writeUTF(innerEntry.getKey());
                    CompletionName completionName = innerEntry.getValue();
                    // 写入 CompletionName 对象的属性
                    dataOutputStream.writeUTF(completionName.getName());
                    dataOutputStream.writeUTF(completionName.getType().name());
                    dataOutputStream.writeUTF(completionName.getDescription());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 反序列化方法
    public static HashMap<String, HashMap<String, CompletionName>> loadHashMapFromFile(Context context, String fileName) {
        try (FileInputStream fileInputStream = context.openFileInput(fileName);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             DataInputStream dataInputStream = new DataInputStream(bufferedInputStream)) {

            HashMap<String, HashMap<String, CompletionName>> hashMap = new HashMap<>();
            // 读取外层 HashMap 的大小
            int outerSize = dataInputStream.readInt();
            for (int i = 0; i < outerSize; i++) {
                String outerKey = dataInputStream.readUTF();
                HashMap<String, CompletionName> innerHashMap = new HashMap<>();
                // 读取内层 HashMap 的大小
                int innerSize = dataInputStream.readInt();
                for (int j = 0; j < innerSize; j++) {
                    String innerKey = dataInputStream.readUTF();
                    String name = dataInputStream.readUTF();
                    CompletionItemKind type = CompletionItemKind.valueOf(dataInputStream.readUTF());
                    String description = dataInputStream.readUTF();
                    CompletionName completionName = new CompletionName(name, type, description);
                    innerHashMap.put(innerKey, completionName);
                }
                hashMap.put(outerKey, innerHashMap);
            }
            return hashMap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // 序列化方法，将 HashMap<String, List<String>> 保存到文件
    public static void saveHashMapToFile2(Context context, HashMap<String, List<String>> hashMap, String fileName) {
        try (FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream)) {

            // 写入 HashMap 的大小
            dataOutputStream.writeInt(hashMap.size());

            // 遍历 HashMap 中的每个键值对
            for (Map.Entry<String, List<String>> entry : hashMap.entrySet()) {
                // 写入键
                dataOutputStream.writeUTF(entry.getKey());

                List<String> stringList = entry.getValue();
                // 写入列表的大小
                dataOutputStream.writeInt(stringList.size());

                // 遍历列表中的每个字符串并写入
                for (String str : stringList) {
                    dataOutputStream.writeUTF(str);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 反序列化方法，从文件中读取数据并重建 HashMap<String, List<String>>
    public static HashMap<String, List<String>> loadHashMapFromFile2(Context context, String fileName) {
        try (FileInputStream fileInputStream = context.openFileInput(fileName);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             DataInputStream dataInputStream = new DataInputStream(bufferedInputStream)) {

            HashMap<String, List<String>> hashMap = new HashMap<>();
            // 读取 HashMap 的大小
            int mapSize = dataInputStream.readInt();

            // 根据 HashMap 的大小循环读取键值对
            for (int i = 0; i < mapSize; i++) {
                // 读取键
                String key = dataInputStream.readUTF();
                List<String> stringList = new ArrayList<>();

                // 读取列表的大小
                int listSize = dataInputStream.readInt();

                // 根据列表大小循环读取字符串并添加到列表中
                for (int j = 0; j < listSize; j++) {
                    stringList.add(dataInputStream.readUTF());
                }

                // 将键和对应的列表添加到 HashMap 中
                hashMap.put(key, stringList);
            }

            return hashMap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
