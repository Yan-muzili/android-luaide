
# 开发规范指南

为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。

## 一、技术栈要求

- **主框架**：Android 应用开发，基于 Gradle 构建工具。
- **语言版本**：
  - Java 版本：Java 17（用于构建和运行）
  - Kotlin 版本：Kotlin 21（支持 JDK 21）
- **核心依赖**：
  - Android SDK API Level 35 (targetSdk)
  - 使用 [Sora Editor](https://github.com/Rosemoe/sora-editor) 作为文本编辑器基础库
  - 使用 `libsu` 进行 Root 权限操作
  - 使用 `OkHttp` 网络请求库
  - 使用 `ColorPicker` 第三方组件库
  - 使用自定义签名工具模块 (`signer`)
- **使用的SDK版本**
  - 编译 SDK: Android API 35
  - 最低兼容 SDK: API 21
  - 目标 SDK: API 35

## 二、分层架构规范

### 目录结构说明

```
luaide/
├── app/                     # 主应用模块
│   └── src/main/java/com/yan/luaide/...
├── colorpick/               # 颜色选择器模块
│   └── src/main/java/com/jaredrummler/android/colorpicker/...
├── editor/                  # Sora 文本编辑器核心模块
│   └── src/main/java/io/github/rosemoe/sora/...
├── luaide-lualanguage/      # Lua语言相关功能模块
│   └── src/main/java/com/yan/luaide/lualanguage/...
└── signer/                  # 自定义APK签名模块
    └── src/main/java/com/mcal/apksigner/...
```

| 层级        | 职责说明                         | 开发约束与注意事项                                               |
|-------------|----------------------------------|----------------------------------------------------------------|
| **App Module** | 主程序入口，处理 UI 和业务逻辑       | 不得直接访问底层库或资源，需通过各子模块提供接口调用             |
| **Editor Library** | 提供高性能的文本编辑能力              | 基于 Sora Editor 封装定制功能                                   |
| **Color Picker** | 图形界面中的颜色选取控件              | 可复用性强，适用于多种场景                                      |
| **Lua Language Tools** | 处理 Lua 文件解析、格式化等任务     | 包含语法分析、自动补全等功能                                    |
| **Signer Module** | APK 签名相关实现                   | 用于对打包后的 APK 文件进行数字签名                             |

### 接口与实现分离

- 所有接口实现类需放在对应模块内的特定包下，例如 `com.yan.luaide.ui.impl`。

## 三、安全与性能规范

### 输入校验

- 对所有外部输入数据进行合法性验证（如文件路径、用户参数）。
- 在涉及敏感操作时使用权限检查机制（如 root 权限判断）。

### 事务管理 & 异步处理

- 所有后台线程执行耗时任务应采用异步方式避免阻塞主线程。
- 数据库操作应在 Service 层中完成，并合理控制并发访问数量。

### 安全防护措施

- 禁止硬编码密钥信息至源码中，如示例中的 `release_key.jks` 密码。
- 所有网络通信建议启用 HTTPS 协议以防止中间人攻击。
- 使用混淆保护关键类名及方法名称。

## 四、代码风格规范

### 命名规范

| 类型       | 命名方式             | 示例                  |
|------------|----------------------|-----------------------|
| 类名       | UpperCamelCase       | `MainActivity`, `LuaParser` |
| 方法/变量  | lowerCamelCase       | `onCreate()`, `isDebugMode()` |
| 常量       | UPPER_SNAKE_CASE     | `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT_MS` |

### 注释规范

- 所有公共类、方法、字段均需添加 Javadoc 注释。
- 若使用中文环境，则注释内容也应为中文。

### 实体类简化工具

- 使用 Lombok 或 KtLint 等自动化工具减少样板代码。

## 五、扩展性与日志规范

### 模块化设计原则

- 各个功能模块之间尽量解耦，方便后期替换或者新增其他语言的支持。
- 利用插件机制将部分通用功能抽象出来，便于复用。

### 日志记录

- 使用 Logcat 输出调试信息，禁止使用 System.out.println()
- 关键流程加入 TRACE 日志追踪执行状态。

## 六、编码原则总结

| 原则       | 说明                                       |
|------------|--------------------------------------------|
| **SOLID**  | 高内聚、低耦合，增强可维护性与可扩展性     |
| **DRY**    | 避免重复代码，提高复用性                   |
| **KISS**   | 保持代码简洁易懂                           |
| **YAGNI**  | 不实现当前不需要的功能                     |
| **OWASP**  | 防范常见安全漏洞，如 SQL 注入、XSS 等      |

---

> ⚙️ **作者**: 何呵呵  
> 📝 **生成时间**: 2025-09-26 22:27:55  
> 💻 **操作系统**: Windows 11  
> 🗂️ **工作目录**: D:\Yan\luaide
