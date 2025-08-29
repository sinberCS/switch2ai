package com.github.switch2ai.core.startup

import com.github.switch2ai.actions.registry.DynamicActionRegistry
import com.github.switch2ai.config.settings.AppSettingsState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * New Project Startup Listener
 * Responsible for initializing the new configuration system when the project starts
 */
class ProjectStartupListener : ProjectActivity {
    
    private val logger = Logger.getInstance(ProjectStartupListener::class.java)
    
    override suspend fun execute(project: Project) {
        try {
            logger.info("New project startup listener execution started: ${project.name}")
            
            // Get configuration state manager
            val settingsState = AppSettingsState.getInstance()
            val config = settingsState.getCurrentConfig()
            
            // Get action registry
            val actionRegistry = DynamicActionRegistry.getInstance(project)
            
            // Register all custom commands and prompt actions
            actionRegistry.registerAllCustomCommands(project, config)
            
            // Add configuration change listener
            settingsState.addConfigChangeListener { newConfig ->
                // Re-register actions when configuration changes
                actionRegistry.registerAllCustomCommands(project, newConfig)
            }
            
            logger.info("New project startup initialization completed")
            
        } catch (e: Exception) {
            logger.error("New project startup initialization failed: ${e.message}", e)
        }
    }
}