# switch2ai

> **Note**: This project is modified from [Switch2Cursor](https://github.com/qczone/switch2cursor), expanding from Cursor-only support to comprehensive AI agent collaboration including Cursor, Qoder, Claude code, and more.

[中文文档](README_zh.md)

> 💡 Recommended to use with [Switch2IDEA](https://github.com/qczone/switch2idea) in Cursor

## 🔍 Introduction
switch2ai - A JetBrains IDE plugin enabling seamless collaboration between JetBrains IDEs and various AI agents (Cursor, Qoder, Claude code etc.). Key features include instant switching between different AI editors, rapid AI prompt input with AI selection, and fully configurable custom commands with variable support for enhanced AI collaboration workflow.


## 🌟 Features

- 🚀 **Instant AI Editor Switching**
    - One-click switch between JetBrains IDE and various AI editors (Cursor, Qoder, Claude code, etc.)
    - Automatically positions to the same cursor location (line and column)
    - Perfectly maintains editing context without interrupting workflow

- 🤖 **Rapid AI Prompt Input**
    - Quick AI prompt input with AI selection dropdown
    - Support for multiple AI agents (Cursor, Qoder, Claude code, etc.)
    - Intelligent context-aware prompt processing

- ⌨️ **Convenient Shortcut Support**
    - macOS:
        - `Option+Shift+K` - Open AI prompt input dialog
        - `Option+Shift+P` - Open project in AI Editor
        - `Option+Shift+O` - Open current file in AI Editor
        - `Option+Shift+C` - Execute custom command
    - Windows:
        - `Alt+Shift+K` - Open AI prompt input dialog
        - `Alt+Shift+P` - Open project in AI Editor
        - `Alt+Shift+O` - Open current file in AI Editor
        - `Alt+Shift+C` - Execute custom command

- 🔧 **Multiple Access Methods**
    - Keyboard shortcuts
    - Editor context menu
    - IDE tools menu

- ⚡ **Fully Configurable Custom Commands**
    - Execute custom commands with comprehensive variable support
    - Variables: `$filePath`, `$projectPath`, `$line`, `$column`, `$selectedText`, `$prompt`
    - Support for shortcut commands like `$test`, `$refactor`, `$explain`
    - Completely customizable command templates

## 🛠️ Installation Guide

### Method 1: Local Installation
1. Download the latest plugin package
2. IDE → `Settings` → `Plugins` → `⚙️`→ `Install Plugin from Disk...`
3. Select the downloaded plugin package
4. Click `OK` to apply changes

### Method 2 (Not Supported Yet): Install via JetBrains Marketplace

## 🚀 Usage Guide

### Basic Usage

#### AI Prompt Input
- Shortcuts:
    - macOS: `Option+Shift+K`
    - Windows: `Alt+Shift+K`
- Context Menu: Right-click in editor → `AI Prompt Input`
- Tools Menu: `Tools` → `AI Prompt Input`
- Features:
    - Select AI agent from dropdown (Cursor, Qoder, Claude code, etc.)
    - Input prompts with intelligent context awareness
    - Support for shortcut commands like `$test`, `$refactor`, `$explain`

#### Open Project in AI Editor
- Shortcuts:
    - macOS: `Option+Shift+P`
    - Windows: `Alt+Shift+P`
- Context Menu: Right-click in project view → `Open Project In AI Editor`
- Tools Menu: `Tools` → `Open Project In AI Editor`

#### Open Current File in AI Editor
- Shortcuts:
    - macOS: `Option+Shift+O`
    - Windows: `Alt+Shift+O`
- Context Menu: Right-click in editor → `Open File In AI Editor`
- Tools Menu: `Tools` → `Open File In AI Editor`

#### Execute Custom Command
- Shortcuts:
    - macOS: `Option+Shift+C`
    - Windows: `Alt+Shift+C`
- Context Menu: Right-click in editor → `Execute Custom Command`
- Tools Menu: `Tools` → `Execute Custom Command`

### Configuration
- In `Settings/Preferences` → `Tools` → `switch2ai`:
    - Configure AI agents (Cursor, Qoder, Claude code, etc.)
    - Set AI agent executable paths and display names
    - Configure custom commands with comprehensive variable support
    - Set up shortcut commands like `$test`, `$refactor`, `$explain`
    - Customize shortcuts through Keymap settings

### Requirements
- Any AI editor installed (Cursor, Qoder, Claude code, etc.)
- Compatible with all JetBrains IDEs (version 2022.3 and above)

## 🧑‍💻 Developer Guide

### Build Project
```bash
# Clone repository
git clone http://gitlab.alibaba-inc.com/jiangxinhao.jxh/switch2ai.git

# Build plugin
cd switch2ai
./gradlew buildPlugin  
# Plugin package will be generated in build/distributions/ directory
```

### Contributing
1. Fork this repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Submit a Pull Request

## 🙋 FAQ

### 1. Which IDEs are supported?
Supports all JetBrains IDEs, including: IntelliJ IDEA, PyCharm, WebStorm, GoLand, RustRover, Android Studio, etc.

### 2. Which versions are supported?
The plugin is developed based on JDK 17 and currently only supports JetBrains IDE version 2022.3 and above

### 3. How to modify plugin shortcuts?
Modify in `Settings` → `Keymap` → `Plugins` → `switch2ai`

### 4. How to configure custom commands?
Go to `Settings` → `Tools` → `switch2ai` and configure custom commands with variable support