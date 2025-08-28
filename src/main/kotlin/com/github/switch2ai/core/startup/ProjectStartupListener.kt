package com.github.switch2ai.core.startup

import com.github.switch2ai.actions.registry.DynamicActionRegistry
import com.github.switch2ai.config.settings.AppSettingsState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * 新的项目启动监听器
 * 负责在项目启动时初始化新的配置系统
 */
class ProjectStartupListener : ProjectActivity {
    
    private val logger = Logger.getInstance(ProjectStartupListener::class.java)
    
    override suspend fun execute(project: Project) {
        try {
            logger.info("新项目启动监听器开始执行: ${project.name}")
            
            // 获取配置状态管理器
            val settingsState = AppSettingsState.getInstance()
            val config = settingsState.getCurrentConfig()
            
            // 获取动作注册器
            val actionRegistry = DynamicActionRegistry.getInstance(project)
            
            // 注册所有自定义命令和提示词动作
            actionRegistry.registerAllCustomCommands(project, config)
            
            // 添加配置变更监听器
            settingsState.addConfigChangeListener { newConfig ->
                // 配置变更时重新注册动作
                actionRegistry.registerAllCustomCommands(project, newConfig)
            }
            
            logger.info("新项目启动初始化完成")
            
        } catch (e: Exception) {
            logger.error("新项目启动初始化失败: ${e.message}", e)
        }
    }
}