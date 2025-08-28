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
 * 应用设置状态管理器
 */
@State(
    name = "com.github.switch2ai.settings.AppSettingsState",
    storages = [Storage("switch2aiSettings.xml")]
)
class AppSettingsState : PersistentStateComponent<AppSettingsState> {
    
    private val logger = Logger.getInstance(AppSettingsState::class.java)

    @JvmField
    @com.intellij.util.xmlb.annotations.Property(style = com.intellij.util.xmlb.annotations.Property.Style.ATTRIBUTE)
    // 直接使用PluginConfig作为主要数据源
    var pluginConfig: PluginConfig = PluginConfig.getDefaultConfig()
    
    // 配置变更监听器
    @Transient
    private val configChangeListeners = mutableListOf<(PluginConfig) -> Unit>()
    
    /**
     * 初始化默认配置
     */
    fun initIfNeed() {
        if (pluginConfig.promptConfiguration.customAIs.isEmpty() && pluginConfig.customCommands.isEmpty()) {
            pluginConfig = PluginConfig.getDefaultConfig()
        }
    }
    
    /**
     * 获取当前配置
     */
    fun getCurrentConfig(): PluginConfig {
        initIfNeed()
        return pluginConfig
    }
    
    /**
     * 更新配置
     */
    fun updateConfig(newConfig: PluginConfig, project: Project?) {
        try {
            val oldConfig = pluginConfig
            val requiresRestart = checkIfRequiresRestart(oldConfig, newConfig)
            
            // 更新配置
            pluginConfig = newConfig
            
            // 通知监听器
            notifyConfigChangeListeners(newConfig)
            
            if (project != null) {
                if (requiresRestart) {
                    // 显示重启提示
                    showRestartPrompt(project)
                    logger.info("配置已更新，但需要重启")
                } else {
                    Messages.showInfoMessage(project, "配置已更新，立即生效", "配置更新成功")
                }
            }
            
        } catch (e: Exception) {
            logger.error("配置更新失败: ${e.message}", e)
            if (project != null) {
                Messages.showErrorDialog(
                    project,
                    "配置更新失败: ${e.message}",
                    "配置更新错误"
                )
            }
        }
    }
    
    /**
     * 检查是否需要重启
     */
    private fun checkIfRequiresRestart(oldConfig: PluginConfig, newConfig: PluginConfig): Boolean {
        // 自定义命令的快捷键有变更时需要重启
        val oldShortcuts = oldConfig.customCommands.mapValues { it.value.shortcut }
        val newShortcuts = newConfig.customCommands.mapValues { it.value.shortcut }
        
        if (oldShortcuts != newShortcuts) {
            return true
        }
        
        // 自定义命令数量有变更时需要重启
        if (oldConfig.customCommands.keys != newConfig.customCommands.keys) {
            return true
        }
        
        return false
    }
    
    /**
     * 显示重启提示
     */
    private fun showRestartPrompt(project: Project) {
        Messages.showInfoMessage(
            project,
            "配置已更新，但涉及快捷键或命令修改。需要重启IDE才能使新配置生效。",
            "配置更新提示"
        )
    }
    
    /**
     * 添加配置变更监听器
     */
    fun addConfigChangeListener(listener: (PluginConfig) -> Unit) {
        configChangeListeners.add(listener)
    }
    
    /**
     * 移除配置变更监听器
     */
    fun removeConfigChangeListener(listener: (PluginConfig) -> Unit) {
        configChangeListeners.remove(listener)
    }
    
    /**
     * 通知配置变更监听器
     */
    private fun notifyConfigChangeListeners(newConfig: PluginConfig) {
        configChangeListeners.forEach { listener ->
            try {
                listener(newConfig)
            } catch (e: Exception) {
                logger.error("配置变更监听器出错: ${e.message}", e)
            }
        }
    }
    
    /**
     * 重置为默认配置
     */
    fun resetToDefault(project: Project) {
        val defaultConfig = PluginConfig.getDefaultConfig()
        updateConfig(defaultConfig, project)
    }
    
    /**
     * 获取指定AI的配置
     */
    fun getAIConfig(aiName: String): AIConfig? {
        return getCurrentConfig().promptConfiguration.customAIs[aiName]
    }
    
    /**
     * 获取指定ID的自定义命令
     */
    fun getCustomCommand(commandId: String): CustomCommand? {
        return getCurrentConfig().customCommands[commandId]
    }
    
    /**
     * 添加或更新AI配置
     */
    fun updateAIConfig(aiName: String, aiConfig: AIConfig) {
        val currentConfig = getCurrentConfig()
        currentConfig.promptConfiguration.customAIs[aiName] = aiConfig
        updateConfig(currentConfig, null)
    }
    
    /**
     * 添加或更新自定义命令
     */
    fun updateCustomCommand(commandId: String, customCommand: CustomCommand) {
        val currentConfig = getCurrentConfig()
        currentConfig.customCommands[commandId] = customCommand
        updateConfig(currentConfig, null)
    }
    
    /**
     * 移除AI配置
     */
    fun removeAIConfig(aiName: String) {
        val currentConfig = getCurrentConfig()
        currentConfig.promptConfiguration.customAIs.remove(aiName)
        updateConfig(currentConfig, null)
    }
    
    /**
     * 移除自定义命令
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
        // 如果加载的状态有数据，使用它；否则使用默认配置
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