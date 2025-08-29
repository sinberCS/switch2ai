package com.github.switch2ai.config.model

/**
 * AI Configuration Data Class
 */
data class AIConfig(
        var name: String = "",               // AI identifier (unique)
        var displayName: String = "",        // Display name
        var command: String = "",            // Command template
        var description: String = ""         // Description
)


/**
 * File Context Information
 */
data class FileContext(
        var filePath: String = "",
        var projectPath: String = "",
        var line: Int = 0,
        var column: Int = 0,
        var selectedText: String? = null
)

/**
 * Unified Custom Command Configuration
 */
data class CustomCommand(
        var id: String = "",                 // Unique identifier
        var shortcut: String = "",           // Shortcut
        var command: String = "",            // Command template
        var description: String = ""         // Description
)

/**
 * Prompt AI Configuration
 */
data class PromptAIConfiguration(
        var currentAI: String = "claudeCode",                    // Currently selected AI
        var customAIs: MutableMap<String, AIConfig> = mutableMapOf(),  // Custom AI configuration
        var shortcutCommands: MutableMap<String, String> = mutableMapOf() // Shortcut command mapping
) {
    /**
     * Get enabled AI list
     */
    fun getAIs(): List<AIConfig> {
        return customAIs.values.toList()
    }

    /**
     * Get current AI configuration
     */
    fun getCurrentAI(): AIConfig? {
        return customAIs[currentAI]
    }

    /**
     * Add custom AI
     */
    fun addCustomAI(ai: AIConfig): PromptAIConfiguration {
        customAIs[ai.name] = ai
        return this
    }

    /**
     * Remove custom AI
     */
    fun removeCustomAI(name: String): PromptAIConfiguration {
        customAIs.remove(name)
        return this
    }

    /**
     * Update AI command
     */
    fun updateAICommand(name: String, command: String): PromptAIConfiguration {
        val ai = customAIs[name] ?: return this
        ai.command = command
        return this
    }
}

/**
 * New Plugin Configuration Data Structure
 */
data class PluginConfig(
        var promptConfiguration: PromptAIConfiguration = PromptAIConfiguration(),      // Prompt AI configuration
        var customCommands: MutableMap<String, CustomCommand> = mutableMapOf()        // Custom command configuration
) {
    companion object {
        /**
         * Get default configuration
         */
        fun getDefaultConfig(): PluginConfig {
            // Default AI configuration
            val defaultAIs = mutableMapOf(
                    "claudeCode" to AIConfig(
                            name = "claudeCode",
                            displayName = "Claude Code",
                            command = "claude \'\${prompt} File location for reference only \${filePath}:\${line}:\${column}\'",
                            description = "Claude Code AI"
                    ),
                    "iflow" to AIConfig(
                            name = "iflow",
                            displayName = "iflow",
                            command = "iflow \'\${prompt} File location for reference only \${filePath}:\${line}:\${column}\'",
                            description = "Claude Code AI"
                    )
            )

            // Default shortcut commands
            val defaultShortcutCommands = mutableMapOf(
                    "\$test" to "Please write unit tests for this function, ensuring coverage of edge cases and exception scenarios",
                    "\$refactor" to "Please refactor this code to improve readability and performance while maintaining original functionality",
                    "\$explain" to "Please explain in detail the functionality and implementation principles of this code",
                    "\$optimize" to "Please optimize the performance and memory usage of this code",
                    "\$debug" to "Please help me analyze potential bugs and issues in this code",
                    "\$doc" to "Please generate detailed documentation comments for this code"
            )

            // Default custom commands (including 4 jump commands)
            val defaultCustomCommands = mutableMapOf(
                    "jumpToCursorFile" to CustomCommand(
                            id = "jumpToCursorFile",
                            shortcut = "Alt+Shift+O",
                            command = "open -a cursor cursor://file\${filePath}:\${line}:\${column}",
                            description = "Open current file in Cursor and locate position"
                    ),
                    "jumpToCursorProject" to CustomCommand(
                            id = "jumpToCursorProject",
                            shortcut = "Alt+Shift+P",
                            command = "open -a cursor \${projectPath}",
                            description = "Open current project in Cursor"
                    ),
                    "jumpToQoderFile" to CustomCommand(
                            id = "jumpToQoderFile",
                            shortcut = "Alt+Shift+U",
                            command = "open -a qoder qoder://file\${filePath}:\${line}:\${column}",
                            description = "Open current file in Qoder and locate position"
                    ),
                    "jumpToQoderProject" to CustomCommand(
                            id = "jumpToQoderProject",
                            shortcut = "Alt+Shift+I",
                            command = "open -a qoder \${projectPath}",
                            description = "Open current project in Qoder"
                    )
            )

            return PluginConfig(
                    promptConfiguration = PromptAIConfiguration(
                            currentAI = "cursor",
                            customAIs = defaultAIs,
                            shortcutCommands = defaultShortcutCommands
                    ),
                    customCommands = defaultCustomCommands
            )
        }
    }
}