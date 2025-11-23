package com.yan.luaeditor.tools


import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.yan.luaeditor.CompletionName
import com.yan.luaide.R
import dalvik.system.DexFile
import io.github.rosemoe.sora.lang.completion.CompletionItemKind
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException

object PackageUtil {

    private var packages: JSONObject? = null
    private val classMap = HashMap<String, MutableList<String>>()
    var classNames = mutableListOf<String>()

    private val cacheClassed = mutableMapOf<String, List<CompletionName>>()

    @JvmStatic
    fun load(context: Context): HashMap<String, MutableList<String>> {
        if (packages != null) return classMap

        try {
            // 先尝试从缓存加载
            loadFromCache(context)
        } catch (e: Exception) {
            // 缓存加载失败，从原始资源加载
            loadFromRawResource(context)
        }
        return classMap
    }

    @JvmStatic
    fun load(context: Context, path: String) {
        if (packages != null) return

        try {
            File(path).readText().let { content ->
                initializePackages(context, content)
            }
        } catch (e: Exception) {
            // 如果自定义路径加载失败，回退到默认加载
            load(context)
        }
    }

    private fun loadFromCache(context: Context) {
        val cacheFile = File(context.cacheDir, "package_cache.json")
        if (!cacheFile.exists()) {
            throw FileNotFoundException("Cache file not found")
        }
        initializePackages(context, cacheFile.readText())
    }

    private fun loadFromRawResource(context: Context) {
        context.resources.openRawResource(R.raw.android).use { stream ->
            val content = stream.bufferedReader().use { it.readText() }
            initializePackages(context, content)

            // 保存到缓存
            try {
                val cacheFile = File(context.cacheDir, "package_cache.json")
                cacheFile.writeText(content)
            } catch (e: Exception) {
                // 缓存保存失败可以忽略
            }
        }
    }

    private fun initializePackages(context: Context, jsonContent: String) {
        packages = JSONObject(jsonContent)

        // 处理 DexFile 条目
        DexFile(context.packageCodePath).entries().asSequence()
            .map { it.replace("$", ".").split(".") }.forEach { parts ->
                var currentJson = packages
                parts.filter { it.length > 2 }.forEach { part ->
                    currentJson = currentJson?.let { json ->
                        when {
                            json.has(part) -> json.getJSONObject(part)
                            else -> JSONObject().also { json.put(part, it) }
                        }
                    }
                }
            }

        // 构建导入映射
        buildImports(packages!!, "")
    }

    private fun buildImports(json: JSONObject, prefix: String = "") {
        json.keys().forEach { key ->
            val subJson = json.optJSONObject(key) ?: return@forEach
            val current = if (prefix.isEmpty()) key else "$prefix.$key"

            val clazz = runCatching { Class.forName(current) }.getOrNull()

            if (clazz != null) {
                // 确认为类，记录并进入内部类模式
                classMap.getOrPut(key) { mutableListOf() } += current
                buildNested(subJson, current)
            } else {
                // 仍是包，继续按包递归
                buildImports(subJson, current)
                if (subJson.length() == 0) {
                    classNames += current   // 原逻辑：空对象代表叶子包
                }
            }
        }
    }

    private fun buildNested(json: JSONObject, parent: String) {
        json.keys().forEach { key ->
            val subJson = json.optJSONObject(key) ?: return@forEach
            val nested = "$parent$$key"

            if (runCatching { Class.forName(nested) }.isSuccess) {
                classMap.getOrPut(key) { mutableListOf() } += nested
                buildNested(subJson, nested)
            }
        }
    }


    @JvmStatic
    fun fix(name: String): List<String>? = classMap[name]

    @JvmStatic
    fun filter(name: String): List<CompletionName> {
        if (packages == null) return emptyList()

        val parts = name.split(".")
        val (searchDepth, searchTerm) = if (name.endsWith(".")) {
            Pair(parts.size, "")
        } else {
            Pair(parts.size - 1, parts.last())
        }

        var currentJson = packages
        // 遍历路径
        for (i in 0 until searchDepth) {
            currentJson = try {
                currentJson?.getJSONObject(parts[i])
            } catch (e: Exception) {
                return emptyList()
            }
        }

        val packages = currentJson?.keys()?.asSequence()?.filter { it.startsWith(searchTerm) }
            ?.map { CompletionName(it, CompletionItemKind.Text, " :package | :class","") }?.toList()
            ?: emptyList()

        val classes = classNames.filter { it.startsWith(searchTerm) }
            .map { CompletionName(it, CompletionItemKind.Text, " :class","") }.toList()

        return packages + classes
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic
    fun filterPackage(name: String, current: String): List<CompletionName> {
        if (packages == null) return emptyList()

        return classMap.keys.asSequence().filter {
            name.indexOf(it) != -1 && name.length > 3
        }.flatMap {
            classMap.getValue(it)
        }.mapNotNull {
            runCatching {
                Class.forName(it)
            }.getOrNull()
        }.flatMap {
            runCatching {
                cacheClassed.getOrPut(it.name) {
                    getJavaMethods(it) + getJavaFields(it)
                }
            }.getOrElse { emptyList() }
        }.distinct()
            .filter {
                it.name.startsWith(current)
            }.toList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getJavaMethods(clazz: Class<*>): List<CompletionName> {
        val methods = clazz.methods
        val names = mutableListOf<CompletionName>()
        for (method in methods) {
            if (method.name.contains("lambda")) continue
            names.add(CompletionName(method.name, CompletionItemKind.Method, " :method",ClassMethodScanner.getParameterTypesAsString(method)))

            if (method.parameters.isEmpty() && method.name.startsWith("get")) {
                val name = method.name.substring(3)
                names.add(
                    CompletionName(
                        name.substring(0, 1).lowercase() + name.substring(1),
                        CompletionItemKind.Property,
                        " :property",
                        ClassMethodScanner.getParameterTypesAsString(method)
                    )
                )
            }

            if (method.parameters.size == 1 && method.name.startsWith(
                    "set"
                )
            ) {
                var name = method.name.substring(3)

                if (name.endsWith("Listener")) {
                    name = name.substring(0, name.length - 8)
                }

                // sort the first char
                name = name.substring(0, 1).lowercase() + name.substring(1)

                names.add(CompletionName(name, CompletionItemKind.Field, " :field",""))
            }
        }

        return names
    }

    private fun getJavaFields(clazz: Class<*>): List<CompletionName> {
        val fields = clazz.fields
        val names = mutableListOf<CompletionName>()
        for (field in fields) {
            names.add(CompletionName(field.name, CompletionItemKind.Field, " :field",""))
        }

        return names
    }
}