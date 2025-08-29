package com.github.switch2ai.config.model

/**
 * AI配置数据类
 */
data class AIConfig(
        var name: String = "",               // AI标识符（唯一）
        var displayName: String = "",        // 显示名称
        var command: String = "",            // 命令模板
        var description: String = ""         // 描述信息
)


/**
 * 文件上下文信息
 */
data class FileContext(
        var filePath: String = "",
        var projectPath: String = "",
        var line: Int = 0,
        var column: Int = 0,
        var selectedText: String? = null
)

/**
 * 统一的自定义命令配置
 */
data class CustomCommand(
        var id: String = "",                 // 唯一标识符
        var shortcut: String = "",           // 快捷键
        var command: String = "",            // 命令模板
        var description: String = ""         // 描述信息
)

/**
 * 提示词AI配置
 */
data class PromptAIConfiguration(
        var currentAI: String = "claudeCode",                    // 当前选中的AI
        var customAIs: MutableMap<String, AIConfig> = mutableMapOf(),  // 自定义AI配置
        var shortcutCommands: MutableMap<String, String> = mutableMapOf() // 快捷命令映射
) {
    /**
     * 获取启用的AI列表
     */
    fun getAIs(): List<AIConfig> {
        return customAIs.values.toList()
    }

    /**
     * 获取当前AI配置
     */
    fun getCurrentAI(): AIConfig? {
        return customAIs[currentAI]
    }

    /**
     * 添加自定义AI
     */
    fun addCustomAI(ai: AIConfig): PromptAIConfiguration {
        customAIs[ai.name] = ai
        return this
    }

    /**
     * 移除自定义AI
     */
    fun removeCustomAI(name: String): PromptAIConfiguration {
        customAIs.remove(name)
        return this
    }

    /**
     * 更新AI命令
     */
    fun updateAICommand(name: String, command: String): PromptAIConfiguration {
        val ai = customAIs[name] ?: return this
        ai.command = command
        return this
    }
}

/**
 * 新的插件配置数据结构
 */
data class PluginConfig(
        var promptConfiguration: PromptAIConfiguration = PromptAIConfiguration(),      // 提示词AI配置
        var customCommands: MutableMap<String, CustomCommand> = mutableMapOf()        // 自定义命令配置
) {
    companion object {
        /**
         * 获取默认配置
         */
        fun getDefaultConfig(): PluginConfig {
            // 默认AI配置
            val defaultAIs = mutableMapOf(
                    "claudeCode" to AIConfig(
                            name = "claudeCode",
                            displayName = "Claude Code",
                            command = "claude \'\${prompt}' 文件位置仅供参考 \${filePath}:\${line}:\${column}\'",
                            description = "Claude Code AI"
                    ),
                    "iflow" to AIConfig(
                            name = "iflow",
                            displayName = "iflow",
                            command = "iflow \'\${prompt}' 文件位置仅供参考 \${filePath}:\${line}:\${column}\'",
                            description = "Claude Code AI"
                    )
            )

            // 默认快捷命令
            val defaultShortcutCommands = mutableMapOf(
                    "\$test" to "请为这个函数编写单元测试，确保覆盖边界条件和异常情况",
                    "\$refactor" to "请重构这段代码，提高可读性和性能，保持原有功能不变",
                    "\$explain" to "请详细解释这段代码的功能和实现原理",
                    "\$optimize" to "请优化这段代码的性能和内存使用",
                    "\$debug" to "请帮我分析这段代码可能存在的bug和问题",
                    "\$doc" to "请为这段代码生成详细的文档注释"
            )

            // 默认自定义命令（包含4个跳转命令）
            val defaultCustomCommands = mutableMapOf(
                    "jumpToCursorFile" to CustomCommand(
                            id = "jumpToCursorFile",
                            shortcut = "Alt+Shift+O",
                            command = "open -a cursor cursor://file\${filePath}:\${line}:\${column}",
                            description = "在Cursor中打开当前文件并定位"
                    ),
                    "jumpToCursorProject" to CustomCommand(
                            id = "jumpToCursorProject",
                            shortcut = "Alt+Shift+P",
                            command = "open -a cursor \${projectPath}",
                            description = "在Cursor中打开当前项目"
                    ),
                    "jumpToQoderFile" to CustomCommand(
                            id = "jumpToQoderFile",
                            shortcut = "Alt+Shift+U",
                            command = "open -a qoder qoder://file\${filePath}:\${line}:\${column}",
                            description = "在Qoder中打开当前文件并定位"
                    ),
                    "jumpToQoderProject" to CustomCommand(
                            id = "jumpToQoderProject",
                            shortcut = "Alt+Shift+I",
                            command = "open -a qoder \${projectPath}",
                            description = "在Qoder中打开当前项目"
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