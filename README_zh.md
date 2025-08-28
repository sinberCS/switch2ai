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
  - 支持 AI 选择的快速提示词输入对话框
  - 支持多种 AI 代理（Cursor、Qoder、Claude code 等）
  - 智能上下文感知的提示词处理

- ⌨️ **便捷的快捷键支持**
  - macOS:
    - `Option+Shift+K` - 打开 AI 提示词输入对话框
    - `Option+Shift+P` - 在 AI 编辑器中打开整个项目
    - `Option+Shift+O` - 在 AI 编辑器中打开当前文件
    - `Option+Shift+C` - 执行自定义命令
  - Windows:
    - `Alt+Shift+K` - 打开 AI 提示词输入对话框
    - `Alt+Shift+P` - 在 AI 编辑器中打开整个项目
    - `Alt+Shift+O` - 在 AI 编辑器中打开当前文件
    - `Alt+Shift+C` - 执行自定义命令

- 🔧 **多样化的访问方式**
  - 快捷键操作
  - 编辑器右键菜单
  - IDE 工具菜单

- ⚡ **完全可配置的自定义命令**
  - 支持全面变量替换的自定义命令执行
  - 变量：`$filePath`、`$projectPath`、`$line`、`$column`、`$selectedText`、`$prompt`
  - 支持快捷命令如 `$test`、`$refactor`、`$explain`
  - 完全可配置的命令模板

## 🛠️ 安装指南

### 方式一：本地安装
1. 下载最新版插件包
2. IDE → `Settings` → `Plugins` → `⚙️`→ `Install Plugin from Disk...`
3. 选择下载的插件包
4. 点击 `OK` 生效

### 方式二(暂未支持)：通过 JetBrains 插件市场安装

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

#### 在 AI 编辑器中打开项目
- 快捷键：
  - macOS: `Option+Shift+P` 
  - Windows: `Alt+Shift+P`
- 右键菜单：在项目视图中右键 → `在 AI 编辑器中打开项目`
- 工具菜单：`Tools` → `在 AI 编辑器中打开项目`

#### 在 AI 编辑器中打开当前文件
- 快捷键：
  - macOS: `Option+Shift+O` 
  - Windows: `Alt+Shift+O`
- 右键菜单：在编辑器中右键 → `在 AI 编辑器中打开文件`
- 工具菜单：`Tools` → `在 AI 编辑器中打开文件`

#### 执行自定义命令
- 快捷键：
  - macOS: `Option+Shift+C` 
  - Windows: `Alt+Shift+C`
- 右键菜单：在编辑器中右键 → `执行自定义命令`
- 工具菜单：`Tools` → `执行自定义命令`

### 配置
- 在 `Settings/Preferences` → `Tools` → `switch2ai` 中：
  - 配置 AI 代理（Cursor、Qoder、Claude code 等）
  - 设置 AI 代理可执行文件路径和显示名称
  - 配置支持全面变量替换的自定义命令
  - 设置快捷命令如 `$test`、`$refactor`、`$explain`
  - 通过 Keymap 设置自定义快捷键

### 环境要求
- 已安装任意 AI 编辑器（Cursor、Qoder、Claude code 等）
- 兼容所有 JetBrains IDE（2022.3 及以上版本）

## 🧑‍💻 开发者指南

### 项目构建
```bash
# 克隆仓库
git clone https://github.com/sinberCS/switch2ai.git 

# 构建插件
cd switch2ai
./gradlew buildPlugin  
# 生成插件包在 build/distributions/ 目录下
```

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
