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
    /**
     * 将 HashMap<String, Object> 保存到文件
     * @param context 上下文
     * @param hashMap 要保存的 HashMap
     * @param fileName 文件名
     */
    public static void saveHashMapToFile(Context context, HashMap<String, Object> hashMap, String fileName) {
        try (FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream)) {

            // 写入外层 HashMap 的大小
            dataOutputStream.writeInt(hashMap.size());
            for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
                // 写入键（String类型）
                dataOutputStream.writeUTF(entry.getKey());
                // 写入值（Object类型，通过辅助方法处理）
                writeObject(dataOutputStream, entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从文件加载 HashMap<String, Object>
     * @param context 上下文
     * @param fileName 文件名
     * @return 加载后的 HashMap
     */
    public static HashMap<String, Object> loadHashMapFromFile(Context context, String fileName) {
        try (FileInputStream fileInputStream = context.openFileInput(fileName);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             DataInputStream dataInputStream = new DataInputStream(bufferedInputStream)) {

            HashMap<String, Object> hashMap = new HashMap<>();
            // 读取外层 HashMap 的大小
            int size = dataInputStream.readInt();
            for (int i = 0; i < size; i++) {
                // 读取键（String类型）
                String key = dataInputStream.readUTF();
                // 读取值（Object类型，通过辅助方法处理）
                Object value = readObject(dataInputStream);
                hashMap.put(key, value);
            }
            return hashMap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 辅助方法：序列化 Object（递归处理嵌套类型）
     * @param out 输出流
     * @param obj 要序列化的对象
     */
    private static void writeObject(DataOutputStream out, Object obj) throws IOException {
        if (obj == null) {
            // 标记空值
            out.writeUTF("null");
            return;
        }

        // 写入类型标识，用于反序列化时区分类型
        if (obj instanceof HashMap) {
            // 处理嵌套的 HashMap<String, Object>
            out.writeUTF("HashMap");
            HashMap<String, Object> map = (HashMap<String, Object>) obj;
            // 写入嵌套 HashMap 的大小
            out.writeInt(map.size());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                out.writeUTF(entry.getKey()); // 写入键
                writeObject(out, entry.getValue()); // 递归写入值
            }
        } else if (obj instanceof CompletionName) {
            // 处理自定义 CompletionName 对象
            out.writeUTF("CompletionName");
            CompletionName cn = (CompletionName) obj;
            out.writeUTF(cn.getName());
            out.writeUTF(cn.getType().name()); // 枚举类型保存名称
            out.writeUTF(cn.getDescription());
        } else if (obj instanceof String) {
            // 处理字符串类型
            out.writeUTF("String");
            out.writeUTF((String) obj);
        } else if (obj instanceof Integer) {
            // 处理整数类型（可扩展其他基础类型）
            out.writeUTF("Integer");
            out.writeInt((Integer) obj);
        } else {
            // 不支持的类型（可根据需求扩展）
            throw new IOException("不支持的序列化类型：" + obj.getClass().getName());
        }
    }

    /**
     * 辅助方法：反序列化 Object（递归处理嵌套类型）
     * @param in 输入流
     * @return 反序列化后的对象
     */
    private static Object readObject(DataInputStream in) throws IOException {
        String type = in.readUTF(); // 读取类型标识
        switch (type) {
            case "null":
                return null;
            case "HashMap":
                // 反序列化嵌套的 HashMap<String, Object>
                int size = in.readInt();
                HashMap<String, Object> map = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    String key = in.readUTF(); // 读取键
                    Object value = readObject(in); // 递归读取值
                    map.put(key, value);
                }
                return map;
            case "CompletionName":
                // 反序列化自定义 CompletionName 对象
                String name = in.readUTF();
                CompletionItemKind typeEnum = CompletionItemKind.valueOf(in.readUTF()); // 枚举类型解析
                String description = in.readUTF();
                return new CompletionName(name, typeEnum, description, ""); // 最后一个参数根据实际构造方法调整
            case "String":
                // 反序列化字符串
                return in.readUTF();
            case "Integer":
                // 反序列化整数（可扩展其他基础类型）
                return in.readInt();
            default:
                throw new IOException("不支持的反序列化类型：" + type);
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
