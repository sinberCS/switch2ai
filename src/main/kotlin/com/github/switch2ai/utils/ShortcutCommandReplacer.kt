package com.github.switch2ai.utils

import com.github.switch2ai.config.model.PromptAIConfiguration

/**
 * Shortcut Command Replacer
 * Responsible for replacing user input shortcut commands with complete prompts
 */
class ShortcutCommandReplacer(private val promptConfig: PromptAIConfiguration) {
    
    /**
     * Replace shortcut commands
     * @param input User input text
     * @return Replaced text
     */
    fun replaceShortcuts(input: String): String {
        var result = input
        
        // Iterate through all shortcut commands for replacement
        promptConfig.shortcutCommands.forEach { (shortcut, replacement) ->
            // Support standalone shortcut commands or shortcut commands + additional content
            result = result.replace(shortcut, replacement)
        }
        
        return result
    }
    
    /**
     * Check if input contains shortcut commands
     * @param input User input
     * @return Whether shortcut commands are contained
     */
    fun containsShortcuts(input: String): Boolean {
        return promptConfig.shortcutCommands.keys.any { shortcut ->
            input.contains(shortcut)
        }
    }
    
    /**
     * Get available shortcut command list
     * @return Shortcut command list
     */
    fun getAvailableShortcuts(): List<String> {
        return promptConfig.shortcutCommands.keys.toList()
    }
    
    /**
     * Get complete description of shortcut commands
     * @return Shortcut command description mapping
     */
    fun getShortcutDescriptions(): Map<String, String> {
        return promptConfig.shortcutCommands
    }
    
    /**
     * Validate if shortcut command exists
     * @param shortcut Shortcut command
     * @return Whether it exists
     */
    fun isValidShortcut(shortcut: String): Boolean {
        return promptConfig.shortcutCommands.containsKey(shortcut)
    }
}