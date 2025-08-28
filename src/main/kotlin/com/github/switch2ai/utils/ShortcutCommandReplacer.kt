package com.github.switch2ai.utils

import com.github.switch2ai.config.model.PromptAIConfiguration

/**
 * 快捷命令替换器
 * 负责将用户输入的快捷命令替换为完整的提示词
 */
class ShortcutCommandReplacer(private val promptConfig: PromptAIConfiguration) {
    
    /**
     * 替换快捷命令
     * @param input 用户输入的文本
     * @return 替换后的文本
     */
    fun replaceShortcuts(input: String): String {
        var result = input
        
        // 遍历所有快捷命令进行替换
        promptConfig.shortcutCommands.forEach { (shortcut, replacement) ->
            // 支持单独的快捷命令或者快捷命令+额外内容
            result = result.replace(shortcut, replacement)
        }
        
        return result
    }
    
    /**
     * 检查输入是否包含快捷命令
     * @param input 用户输入
     * @return 是否包含快捷命令
     */
    fun containsShortcuts(input: String): Boolean {
        return promptConfig.shortcutCommands.keys.any { shortcut ->
            input.contains(shortcut)
        }
    }
    
    /**
     * 获取可用的快捷命令列表
     * @return 快捷命令列表
     */
    fun getAvailableShortcuts(): List<String> {
        return promptConfig.shortcutCommands.keys.toList()
    }
    
    /**
     * 获取快捷命令的完整说明
     * @return 快捷命令说明映射
     */
    fun getShortcutDescriptions(): Map<String, String> {
        return promptConfig.shortcutCommands
    }
    
    /**
     * 验证快捷命令是否存在
     * @param shortcut 快捷命令
     * @return 是否存在
     */
    fun isValidShortcut(shortcut: String): Boolean {
        return promptConfig.shortcutCommands.containsKey(shortcut)
    }
}