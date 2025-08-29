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
    - Support for shortcut commands like `$test`, `$refactor`, `$explain`

- ⌨️ **Convenient Shortcut Support**
    - macOS:
        - `Option+Shift+K` - Open AI prompt input popup
        - `Option+Shift+P` - Open project in Cursor Editor
        - `Option+Shift+O` - Open current file in Cursor Editor
        - `Option+Shift+U` - Open current file in Qoder
        - `Option+Shift+I` - Open project in Qoder
    - Windows:
        - `Alt+Shift+K` - Open AI prompt input popup
        - `Option+Shift+P` - Open project in Cursor Editor
        - `Option+Shift+O` - Open current file in Cursor Editor
        - `Alt+Shift+U` - Open current file in Qoder
        - `Alt+Shift+I` - Open project in Qoder

- 🔧 **Multiple Access Methods**
    - Keyboard shortcuts
    - Editor context menu
    - IDE tools menu

- ⚡ **Fully Configurable Custom Commands**
    - Execute custom commands with comprehensive variable support
    - Variables: `$filePath`, `$projectPath`, `$line`, `$column`, `$selectedText`, `$prompt`
    - Support for shortcut commands like `$test`, `$refactor`, `$explain`
    - Completely customizable command templates
    - Dynamic command registration and management

## 📦 Download Plugin

### Latest Release
- **Download**: [switch2ai-0.1.0.zip](https://github.com/sinberCS/switch2ai/releases/latest/download/switch2ai-0.1.0.zip)
- **Version**: 0.1.0
- **Release Date**: Latest
- **Compatibility**: JetBrains IDEs 2022.3+

### Previous Versions
- [All Releases](https://github.com/sinberCS/switch2ai/releases)

## 🛠️ Installation Guide

### Method 1: Local Installation (Recommended)
1. **Download** the latest plugin package from the [Releases page](https://github.com/sinberCS/switch2ai/releases)
2. **Open** your JetBrains IDE
3. **Go to** `Settings` (Windows/Linux) or `Preferences` (macOS)
4. **Navigate to** `Plugins` → `⚙️` → `Install Plugin from Disk...`
5. **Select** the downloaded `.zip` file
6. **Click** `OK` to apply changes
7. **Restart** your IDE when prompted

### Method 2: Build from Source
```bash
# Clone repository
git clone https://github.com/sinberCS/switch2ai.git

# Build plugin
cd switch2ai
./gradlew buildPlugin

# Plugin package will be generated in build/distributions/ directory
# Install the generated .zip file using Method 1
```

### Method 3: Install via JetBrains Marketplace (Coming Soon)
- Plugin will be available on the official JetBrains Marketplace
- One-click installation directly from IDE
- Automatic updates

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
    - Popup appears at cursor position for quick access

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

#### Open in Qoder
- Shortcuts:
    - macOS: `Option+Shift+U` (file), `Option+Shift+I` (project)
    - Windows: `Alt+Shift+U` (file), `Alt+Shift+I` (project)
- Context Menu: Right-click in editor → `Open In Qoder`
- Tools Menu: `Tools` → `Open In Qoder`

### Configuration
- In `Settings/Preferences` → `Tools` → `switch2ai`:
    - **AI Configuration Tab**: Configure AI agents (Cursor, Qoder, Claude code, etc.)
        - Set AI agent names, display names, command templates, and descriptions
        - Configure command templates with variable support
    - **Custom Commands Tab**: Configure custom commands with comprehensive variable support
        - Set command IDs, shortcuts, command templates, and descriptions
        - Support for variables: `$filePath`, `$projectPath`, `$line`, `$column`, `$selectedText`
    - **Shortcut Commands Tab**: Set up shortcut commands like `$test`, `$refactor`, `$explain`
        - Define short commands that expand to full prompts
        - Example: `$test` expands to "Please write unit tests for this function..."
    - Customize shortcuts through Keymap settings

### Default Configuration
The plugin comes with pre-configured:
- **AI Agents**: Claude Code, iflow
- **Shortcut Commands**: `$test`, `$refactor`, `$explain`, `$optimize`, `$debug`, `$doc`
- **Custom Commands**: Jump to Cursor/Qoder with file and project support

### Requirements
- Any AI editor installed (Cursor, Qoder, Claude code, etc.)
- Compatible with all JetBrains IDEs (version 2022.3 and above)
- Terminal plugin enabled for integrated terminal execution

## 🧑‍💻 Developer Guide

### Project Structure
```
src/main/kotlin/com/github/switch2ai/
├── actions/
│   ├── processor/          # Command execution logic
│   └── registry/           # Dynamic action registration
├── config/
│   ├── model/              # Configuration data models
│   └── settings/           # Settings UI and state management
├── core/
│   └── startup/            # Project startup initialization
├── ui/
│   └── dialog/             # User interface components
└── utils/                  # Utility classes
```

### Build Project
```bash
# Clone repository
git clone https://github.com/sinberCS/switch2ai.git 

# Build plugin
cd switch2ai
./gradlew buildPlugin  
# Plugin package will be generated in build/distributions/ directory
```

### Key Components
- **DynamicActionRegistry**: Manages dynamic registration of custom commands and actions
- **CommandProcessor**: Handles command execution and AI prompt processing
- **AppSettingsConfigurable**: Provides comprehensive configuration interface
- **PromptInputPopup**: Quick prompt input popup at cursor position
- **ShortcutCommandReplacer**: Processes shortcut command expansions

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

### 5. How do shortcut commands work?
Shortcut commands like `$test` automatically expand to full prompts when you type them in the AI prompt input

### 6. Can I add my own AI agents?
Yes, you can add custom AI agents in the AI Configuration tab with custom command templates and variable support

### 7. How does the plugin handle cursor positioning?
The plugin automatically captures the current file path, line, and column, then passes this information to AI editors for precise positioning