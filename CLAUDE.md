# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a JetBrains IDE plugin called "switch2ai" that enables seamless switching between JetBrains IDE and any Agent Editor (like Cursor, Qoder) while maintaining precise cursor position. The plugin provides features for:
- Opening files/projects in AI editors with one click
- Maintaining exact cursor position (line and column)
- Custom command execution with variable support
- AI prompt input with AI selection

## Code Architecture

### Core Components

1. **Configuration System**
   - `AppSettingsState.kt`: Main configuration management with persistent state
   - `ConfigModel.kt`: Data models for AI configs, custom commands, and plugin configuration
   - Uses IntelliJ's `PersistentStateComponent` for configuration persistence

2. **Dynamic Action System**
   - `DynamicActionRegistry.kt`: Registers custom commands and AI prompt actions dynamically
   - Handles shortcut registration and context menu integration
   - Manages action lifecycle (registration/unregistration)

3. **Command Processing**
   - `CommandProcessor.kt`: Executes custom commands and AI prompts
   - Handles variable expansion (${filePath}, ${line}, ${column}, etc.)
   - Integrates with IDE terminal for command execution

4. **UI Components**
   - `PromptInputDialog.kt`: Dialog for AI prompt input with AI selection
   - Configuration UI integrated through `AppSettingsConfigurable.kt`

5. **Project Lifecycle**
   - `ProjectStartupListener.kt`: Initializes the plugin on project startup
   - Registers configuration change listeners for dynamic updates

### Key Features Implementation

1. **Custom Commands**: Configurable commands with shortcut keys and variable substitution
2. **AI Prompt System**: Interactive prompt input with AI selection and shortcut command replacement
3. **Dynamic Registration**: Actions, shortcuts, and context menus registered based on configuration
4. **Multi-AI Support**: Configurable AI editors with custom command templates

## Development Commands

### Build Project
```bash
./gradlew buildPlugin
```
Plugin package will be generated in `build/distributions/` directory.

### Run Tests
```bash
./gradlew test
```

### Run IntelliJ IDE with Plugin
```bash
./gradlew runIde
```

## Configuration Files

- `build.gradle.kts`: Build configuration
- `src/main/resources/META-INF/plugin.xml`: Plugin metadata and extension points
- Configuration stored in `switch2aiSettings.xml` (managed by IntelliJ platform)

## Key Extension Points

- `com.intellij.applicationService`: For `AppSettingsState`
- `com.intellij.projectService`: For `DynamicActionRegistry`
- `com.intellij.applicationConfigurable`: For settings UI
- `com.intellij.postStartupActivity`: For `ProjectStartupListener`

## Variable Substitution

The plugin supports these variables in command templates:
- `${filePath}`: Current file path
- `${projectPath}`: Project root path
- `${line}`: Current line number (1-based)
- `${column}`: Current column number (1-based)
- `${selectedText}`: Currently selected text (if any)
- `${prompt}`: AI prompt text (for AI commands)

## Testing

Test the plugin by:
1. Building with `./gradlew buildPlugin`
2. Installing the generated zip file in IntelliJ IDEA
3. Verifying configuration UI in Settings → Tools → Switch2AI
4. Testing shortcuts and context menu actions
5. Verifying AI prompt dialog functionality