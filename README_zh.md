# switch2ai

> **注意**: 本项目基于 [Switch2Cursor](https://github.com/qczone/switch2cursor) 修改而来，从仅支持 Cursor 扩展到全面的 AI 代理协作，包括 Cursor、Qoder、Claude code 等。

[English](README.md)

> 💡 推荐在 Qoder(or Cursor) 中配合 [Switch2IDEA](https://github.com/qczone/switch2idea) 使用


## 🔍 项目简介
switch2ai - 一个 JetBrains IDE 插件，实现 JetBrains IDE 与各种 AI 代理（Cursor、Qoder、Claude code 等）之间的无缝协作。主要功能包括在不同 AI 编辑器之间即时切换、支持 AI 选择的快速 AI 提示词输入，以及支持变量的完全可配置自定义命令，为增强的 AI 协作工作流程提供支持。


## 🌟 功能特性

- 🚀 **即时 AI 编辑器切换**
  - 在 JetBrains IDE 和各种 AI 编辑器（Cursor、Qoder、Claude code 等）之间一键切换
  - 自动定位到相同的光标位置（行号和列号）
  - 完美保持编辑上下文，不中断思路

- 🤖 **快速 AI 提示词输入**
  - 支持 AI 选择的快速提示词输入弹窗
  - 支持多种 AI 代理（Cursor、Qoder、Claude code 等）
  - 智能上下文感知的提示词处理
  - 支持快捷命令如 `$test`、`$refactor`、`$explain`

- ⌨️ **便捷的快捷键支持**
  - macOS:
    - `Option+Shift+K` - 打开 AI 提示词输入弹窗
    - `Option+Shift+P` - 在 Cursor 编辑器中打开项目
    - `Option+Shift+O` - 在 Cursor 编辑器中打开当前文件
    - `Option+Shift+U` - 在 Qoder 中打开当前文件
    - `Option+Shift+I` - 在 Qoder 中打开项目
  - Windows:
    - `Alt+Shift+K` - 打开 AI 提示词输入弹窗
    - `Alt+Shift+P` - 在 Cursor 编辑器中打开项目
    - `Alt+Shift+O` - 在 Cursor 编辑器中打开当前文件
    - `Alt+Shift+U` - 在 Qoder 中打开当前文件
    - `Alt+Shift+I` - 在 Qoder 中打开项目

- 🔧 **多样化的访问方式**
  - 快捷键操作
  - 编辑器右键菜单
  - IDE 工具菜单

- ⚡ **完全可配置的自定义命令**
  - 支持全面变量替换的自定义命令执行
  - 变量：`$filePath`、`$projectPath`、`$line`、`$column`、`$selectedText`、`$prompt`
  - 支持快捷命令如 `$test`、`$refactor`、`$explain`
  - 完全可配置的命令模板
  - 动态命令注册和管理

## 📦 下载插件

### 最新版本
- **下载**: [switch2ai-0.1.1.zip](https://github.com/sinberCS/switch2ai/releases/latest/download/switch2ai-0.1.1.zip)
- **版本**: 0.1.1
- **发布日期**: 最新
- **兼容性**: JetBrains IDEs 2022.3+

### 历史版本
- [所有版本](https://github.com/sinberCS/switch2ai/releases)

## 🛠️ 安装指南

### 方式一：本地安装（推荐）
1. **下载** 最新插件包，从 [Releases 页面](https://github.com/sinberCS/switch2ai/releases)
2. **打开** 你的 JetBrains IDE
3. **进入** `设置` (Windows/Linux) 或 `偏好设置` (macOS)
4. **导航到** `插件` → `⚙️` → `从磁盘安装插件...`
5. **选择** 下载的 `.zip` 文件
6. **点击** `确定` 应用更改
7. **重启** IDE（当提示时）

### 方式二：从源码构建
```bash
# 克隆仓库
git clone https://github.com/sinberCS/switch2ai.git

# 构建插件
cd switch2ai
./gradlew buildPlugin

# 插件包将生成在 build/distributions/ 目录中
# 使用方法 1 安装生成的 .zip 文件
```

### 方式三：通过 JetBrains 市场安装（即将推出）
- 插件将在官方 JetBrains 市场上可用
- 直接从 IDE 一键安装
- 自动更新

## 🚀 使用说明

### 基础使用

#### AI 提示词输入
- 快捷键：
  - macOS: `Option+Shift+K` 
  - Windows: `Alt+Shift+K`
- 右键菜单：在编辑器中右键 → `AI 提示词输入`
- 工具菜单：`Tools` → `AI 提示词输入`
- 功能特性：
  - 从下拉菜单选择 AI 代理（Cursor、Qoder、Claude code 等）
  - 智能上下文感知的提示词输入
  - 支持快捷命令如 `$test`、`$refactor`、`$explain`
  - 弹窗在光标位置显示，快速访问

#### 在 Cursor 编辑器中打开项目
- 快捷键：
  - macOS: `Option+Shift+P` 
  - Windows: `Alt+Shift+P`
- 右键菜单：在项目视图中右键 → `在 Cursor 编辑器中打开项目`
- 工具菜单：`Tools` → `在 Cursor 编辑器中打开项目`

#### 在 Cursor 编辑器中打开当前文件
- 快捷键：
  - macOS: `Option+Shift+O` 
  - Windows: `Alt+Shift+O`
- 右键菜单：在编辑器中右键 → `在 Cursor 编辑器中打开文件`
- 工具菜单：`Tools` → `在 Cursor 编辑器中打开文件`

#### 在 Qoder 中打开
- 快捷键：
  - macOS: `Option+Shift+U` (文件), `Option+Shift+I` (项目)
  - Windows: `Alt+Shift+U` (文件), `Alt+Shift+I` (项目)
- 右键菜单：在编辑器中右键 → `在 Qoder 中打开`
- 工具菜单：`Tools` → `在 Qoder 中打开`

### 配置
- 在 `Settings/Preferences` → `Tools` → `switch2ai` 中：
  - **AI 配置选项卡**: 配置 AI 代理（Cursor、Qoder、Claude code 等）
    - 设置 AI 代理名称、显示名称、命令模板和描述
    - 配置支持变量替换的命令模板
  - **自定义命令选项卡**: 配置支持全面变量替换的自定义命令
    - 设置命令 ID、快捷键、命令模板和描述
    - 支持变量：`$filePath`、`$projectPath`、`$line`、`$column`、`$selectedText`
  - **快捷命令选项卡**: 设置快捷命令如 `$test`、`$refactor`、`$explain`
    - 定义扩展为完整提示词的简短命令
    - 示例：`$test` 扩展为 "请为这个函数编写单元测试..."
  - 通过 Keymap 设置自定义快捷键

### 默认配置
插件预配置了以下内容：
- **AI 代理**: Claude Code、iflow
- **快捷命令**: `$test`、`$refactor`、`$explain`、`$optimize`、`$debug`、`$doc`
- **自定义命令**: 支持文件和项目的 Cursor/Qoder 跳转

### 环境要求
- 已安装任意 AI 编辑器（Cursor、Qoder、Claude code 等）
- 兼容所有 JetBrains IDE（2022.3 及以上版本）
- 启用 Terminal 插件以支持集成终端执行

## 🧑‍💻 开发者指南

### 项目结构
```
src/main/kotlin/com/github/switch2ai/
├── actions/
│   ├── processor/          # 命令执行逻辑
│   └── registry/           # 动态动作注册
├── config/
│   ├── model/              # 配置数据模型
│   └── settings/           # 设置 UI 和状态管理
├── core/
│   └── startup/            # 项目启动初始化
├── ui/
│   └── dialog/             # 用户界面组件
└── utils/                  # 工具类
```

### 项目构建
```bash
# 克隆仓库
git clone https://github.com/sinberCS/switch2ai.git 

# 构建插件
cd switch2ai
./gradlew buildPlugin  
# 生成插件包在 build/distributions/ 目录下
```

### 核心组件
- **DynamicActionRegistry**: 管理自定义命令和动作的动态注册
- **CommandProcessor**: 处理命令执行和 AI 提示词处理
- **AppSettingsConfigurable**: 提供全面的配置界面
- **PromptInputPopup**: 光标位置的快速提示词输入弹窗
- **ShortcutCommandReplacer**: 处理快捷命令扩展

### 贡献代码
1. Fork 本仓库
2. 创建特性分支
3. 提交修改
4. 推送分支
5. 提交 Pull Request

## 🙋 常见问题 

### 1. 都支持哪些 IDE？
支持所有 JetBrains 系列的 IDE，如：IntelliJ IDEA、PyCharm、WebStorm、GoLand、RustRover、Android Studio 等

### 2. 都支持哪些版本？
插件基于 JDK 17 开发，目前仅支持 JetBrains IDE 2022.3 及以上版本

### 3. 如何修改插件的快捷键？
在 `Settings` → `Keymap` → `Plugins` → `switch2ai` 中修改

### 4. 如何配置自定义命令？
进入 `Settings` → `Tools` → `switch2ai` 并配置支持变量替换的自定义命令

### 5. 快捷命令是如何工作的？
快捷命令如 `$test` 在 AI 提示词输入中会自动扩展为完整的提示词

### 6. 可以添加自己的 AI 代理吗？
是的，您可以在 AI 配置选项卡中添加自定义 AI 代理，支持自定义命令模板和变量替换

### 7. 插件如何处理光标定位？
插件自动捕获当前文件路径、行号和列号，然后将这些信息传递给 AI 编辑器以实现精确定位
