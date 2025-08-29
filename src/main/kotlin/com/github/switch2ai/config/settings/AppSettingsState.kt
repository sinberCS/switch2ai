package com.github.switch2ai.config.settings

import com.github.switch2ai.config.model.AIConfig
import com.github.switch2ai.config.model.CustomCommand
import com.github.switch2ai.config.model.PluginConfig
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

/**
 * Application Settings State Manager
 */
@State(
    name = "com.github.switch2ai.settings.AppSettingsState",
    storages = [Storage("switch2aiSettings.xml")]
)
class AppSettingsState : PersistentStateComponent<AppSettingsState> {
    
    private val logger = Logger.getInstance(AppSettingsState::class.java)

    @JvmField
    @com.intellij.util.xmlb.annotations.Property(style = com.intellij.util.xmlb.annotations.Property.Style.ATTRIBUTE)
    // Directly use PluginConfig as the primary data source
    var pluginConfig: PluginConfig = PluginConfig.getDefaultConfig()
    
    // Configuration change listeners
    @Transient
    private val configChangeListeners = mutableListOf<(PluginConfig) -> Unit>()
    
    /**
     * Initialize default configuration
     */
    fun initIfNeed() {
        if (pluginConfig.promptConfiguration.customAIs.isEmpty() && pluginConfig.customCommands.isEmpty()) {
            pluginConfig = PluginConfig.getDefaultConfig()
        }
    }
    
    /**
     * Get current configuration
     */
    fun getCurrentConfig(): PluginConfig {
        initIfNeed()
        return pluginConfig
    }
    
    /**
     * Update configuration
     */
    fun updateConfig(newConfig: PluginConfig, project: Project?) {
        try {
            val oldConfig = pluginConfig
            val requiresRestart = checkIfRequiresRestart(oldConfig, newConfig)
            
            // Update configuration
            pluginConfig = newConfig
            
            // Notify listeners
            notifyConfigChangeListeners(newConfig)
            
            if (project != null) {
                if (requiresRestart) {
                    // Show restart prompt
                    showRestartPrompt(project)
                    logger.info("Configuration updated but requires restart")
                } else {
                    Messages.showInfoMessage(project, "Configuration updated and effective immediately", "Configuration Update Success")
                }
            }
            
        } catch (e: Exception) {
            logger.error("Configuration update failed: ${e.message}", e)
            if (project != null) {
                Messages.showErrorDialog(
                    project,
                    "Configuration update failed: ${e.message}",
                    "Configuration Update Error"
                )
            }
        }
    }
    
    /**
     * Check if restart is required
     */
    private fun checkIfRequiresRestart(oldConfig: PluginConfig, newConfig: PluginConfig): Boolean {
        // Restart required when custom command shortcuts change
        val oldShortcuts = oldConfig.customCommands.mapValues { it.value.shortcut }
        val newShortcuts = newConfig.customCommands.mapValues { it.value.shortcut }
        
        if (oldShortcuts != newShortcuts) {
            return true
        }
        
        // Restart required when custom command count changes
        if (oldConfig.customCommands.keys != newConfig.customCommands.keys) {
            return true
        }
        
        return false
    }
    
    /**
     * Show restart prompt
     */
    private fun showRestartPrompt(project: Project) {
        Messages.showInfoMessage(
            project,
            "Configuration updated but involves shortcut or command modifications. Restart IDE required for new configuration to take effect.",
            "Configuration Update Notice"
        )
    }
    
    /**
     * Add configuration change listener
     */
    fun addConfigChangeListener(listener: (PluginConfig) -> Unit) {
        configChangeListeners.add(listener)
    }
    
    /**
     * Remove configuration change listener
     */
    fun removeConfigChangeListener(listener: (PluginConfig) -> Unit) {
        configChangeListeners.remove(listener)
    }
    
    /**
     * Notify configuration change listeners
     */
    private fun notifyConfigChangeListeners(newConfig: PluginConfig) {
        configChangeListeners.forEach { listener ->
            try {
                listener(newConfig)
            } catch (e: Exception) {
                logger.error("Configuration change listener error: ${e.message}", e)
            }
        }
    }
    
    /**
     * Reset to default configuration
     */
    fun resetToDefault(project: Project) {
        val defaultConfig = PluginConfig.getDefaultConfig()
        updateConfig(defaultConfig, project)
    }
    
    /**
     * Get AI configuration by name
     */
    fun getAIConfig(aiName: String): AIConfig? {
        return getCurrentConfig().promptConfiguration.customAIs[aiName]
    }
    
    /**
     * Get custom command by ID
     */
    fun getCustomCommand(commandId: String): CustomCommand? {
        return getCurrentConfig().customCommands[commandId]
    }
    
    /**
     * Add or update AI configuration
     */
    fun updateAIConfig(aiName: String, aiConfig: AIConfig) {
        val currentConfig = getCurrentConfig()
        currentConfig.promptConfiguration.customAIs[aiName] = aiConfig
        updateConfig(currentConfig, null)
    }
    
    /**
     * Add or update custom command
     */
    fun updateCustomCommand(commandId: String, customCommand: CustomCommand) {
        val currentConfig = getCurrentConfig()
        currentConfig.customCommands[commandId] = customCommand
        updateConfig(currentConfig, null)
    }
    
    /**
     * Remove AI configuration
     */
    fun removeAIConfig(aiName: String) {
        val currentConfig = getCurrentConfig()
        currentConfig.promptConfiguration.customAIs.remove(aiName)
        updateConfig(currentConfig, null)
    }
    
    /**
     * Remove custom command
     */
    fun removeCustomCommand(commandId: String) {
        val currentConfig = getCurrentConfig()
        currentConfig.customCommands.remove(commandId)
        updateConfig(currentConfig, null)
    }
    
    override fun getState(): AppSettingsState {
        return this
    }
    
    override fun loadState(state: AppSettingsState) {
        // If loaded state has data, use it; otherwise use default configuration
        com.intellij.util.xmlb.XmlSerializerUtil.copyBean(state, this)
        initIfNeed()
    }
    
    companion object {
        fun getInstance(): AppSettingsState {
            val instance = ApplicationManager.getApplication().getService(AppSettingsState::class.java)
            instance.initIfNeed()
            return instance
        }
    }
}