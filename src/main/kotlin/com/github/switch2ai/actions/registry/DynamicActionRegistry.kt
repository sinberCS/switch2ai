package com.github.switch2ai.actions.registry

import com.github.switch2ai.actions.processor.CommandProcessor
import com.github.switch2ai.config.model.CustomCommand
import com.github.switch2ai.config.model.FileContext
import com.github.switch2ai.config.model.PluginConfig
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import javax.swing.KeyStroke

/**
 * Dynamic Action Registry
 * Responsible for dynamic registration and management of custom commands
 */
@Service(Service.Level.PROJECT)
class DynamicActionRegistry {

    private val logger = Logger.getInstance(DynamicActionRegistry::class.java)
    private val commandProcessor = CommandProcessor()

    // Currently registered action ID list
    private val registeredActionIds = mutableSetOf<String>()

    // Currently effective configuration cache
    private var currentConfig: PluginConfig? = null

    companion object {
        fun getInstance(project: Project): DynamicActionRegistry {
            return project.getService(DynamicActionRegistry::class.java)
        }
    }

    /**
     * Register all custom commands
     */
    fun registerAllCustomCommands(project: Project, config: PluginConfig) {
        if (isConfigSame(config)) {
            logger.info("Configuration unchanged, skipping re-registration")
            return
        }

        // Clean up old registrations
        unregisterAllActions()

        // Register custom commands
        config.customCommands.values.forEach { command ->
            registerCustomCommand(project, command)
        }

        // Register prompt action (fixed shortcut)
        registerPromptAction(project, config)

        // Register context menus
        registerContextMenus(project, config.customCommands.values)

        // Update configuration cache
        currentConfig = deepCopyConfig(config)

        logger.info("Registered ${registeredActionIds.size} actions")
    }

    /**
     * Register single custom command
     */
    private fun registerCustomCommand(project: Project, command: CustomCommand) {
        try {
            val actionManager = ActionManager.getInstance()
            val actionId = getActionId(command.id)

            // Create dynamic action
            val action = CustomCommandAction(command, commandProcessor)

            // Register action
            actionManager.registerAction(actionId, action)
            registeredActionIds.add(actionId)

            // Register shortcut
            if (command.shortcut.isNotEmpty()) {
                registerShortcut(project, actionId, command.shortcut)
            }

            logger.info("Custom command registered: ${command.id} (${actionId}) shortcut: ${command.shortcut}")

        } catch (e: Exception) {
            logger.error("Failed to register custom command: ${command.id}", e)
        }
    }

    /**
     * Register prompt action
     */
    private fun registerPromptAction(project: Project, config: PluginConfig) {
        try {
            val actionManager = ActionManager.getInstance()
            val actionId = "switch2ai.promptAction"

            // Create prompt action
            val action = PromptAction(config.promptConfiguration, commandProcessor)

            // Register action
            actionManager.registerAction(actionId, action)
            registeredActionIds.add(actionId)

            // Register fixed shortcut Alt+Shift+P
            registerShortcut(project, actionId, "Alt+Shift+K")

            logger.info("Prompt action registered: $actionId")

        } catch (e: Exception) {
            logger.error("Failed to register prompt action", e)
        }
    }

    /**
     * Process shortcut format, convert to IDEA plugin standard format
     */
    private fun processKeyMap(shortcut: String): String {
        return shortcut.lowercase()
                .replace("option", "alt")
                .replace("cmd", "meta")
                .replace("command", "meta")
                .replace("ctrl", "control")
                .split("+")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .let { parts ->
                    val modifiers = parts.dropLast(1).joinToString(" ")
                    val key = parts.lastOrNull()?.uppercase() ?: ""
                    if (modifiers.isNotEmpty()) "$modifiers $key" else key
                }
    }

    /**
     * Register shortcut with conflict detection and user choice
     */
    private fun registerShortcut(project: Project, actionId: String, shortcutText: String) {
        try {
            // Use unified shortcut processing method
            val normalizedShortcut = processKeyMap(shortcutText)
            val keyStroke = KeyStroke.getKeyStroke(normalizedShortcut)

            if (keyStroke != null) {
                val keymap = com.intellij.openapi.keymap.KeymapManager.getInstance().activeKeymap
                val shortcut = com.intellij.openapi.actionSystem.KeyboardShortcut(keyStroke, null)
                
                // Check for conflicts before registering
                val conflicts = checkShortcutConflicts(keymap, shortcut, actionId)
                
                if (conflicts.isNotEmpty()) {
                    logger.warn("Shortcut conflicts detected for $shortcutText:")
                    conflicts.forEach { conflict ->
                        logger.warn("  - Conflicts with action: ${conflict.actionId} (${conflict.description})")
                    }
                    
                    // Show user dialog to choose whether to replace
                    val shouldReplace = showShortcutConflictDialog(project, shortcutText, conflicts)
                    
                    if (!shouldReplace) {
                        logger.info("User chose not to replace shortcut: $shortcutText")
                        return
                    }
                    
                    // Remove conflicting shortcuts first
                    removeConflictingShortcuts(keymap, shortcut, conflicts)
                }
                
                // Register shortcut
                keymap.addShortcut(actionId, shortcut)
                logger.info("Shortcut registered: $shortcutText -> $actionId")
                
            } else {
                logger.error("Unable to parse shortcut: $shortcutText (processed: $normalizedShortcut)")
            }
        } catch (e: Exception) {
            logger.error("Failed to register shortcut: $shortcutText -> $actionId", e)
        }
    }

    /**
     * Register context menu
     */
    private fun registerContextMenus(project: Project, commands: Collection<CustomCommand>) {
        try {
            val actionManager = ActionManager.getInstance()
            val editorPopupGroup = actionManager.getAction("EditorPopupMenu") as? DefaultActionGroup

            if (editorPopupGroup != null) {
                // Add separator
                editorPopupGroup.add(Separator("Switch2AI"))

                // Add custom commands to context menu
                commands.forEach { command ->
                    val actionId = getActionId(command.id)
                    val action = actionManager.getAction(actionId)
                    if (action != null) {
                        editorPopupGroup.add(action)
                    }
                }

                // Add prompt action
                val promptAction = actionManager.getAction("switch2ai.promptAction")
                if (promptAction != null) {
                    editorPopupGroup.add(promptAction)
                }

                logger.info("Context menu registered")
            }

        } catch (e: Exception) {
            logger.error("Failed to register context menu", e)
        }
    }

    /**
     * Unregister all actions
     */
    fun unregisterAllActions() {
        try {
            val actionManager = ActionManager.getInstance()
            val keymap = com.intellij.openapi.keymap.KeymapManager.getInstance().activeKeymap

            // Remove shortcuts and actions
            registeredActionIds.forEach { actionId ->
                try {
                    // Remove shortcuts
                    keymap.removeAllActionShortcuts(actionId)

                    // Unregister actions
                    actionManager.unregisterAction(actionId)
                } catch (e: Exception) {
                    logger.error("Failed to unregister action: $actionId", e)
                }
            }

            // Clean up context menu
            removeFromContextMenu()

            registeredActionIds.clear()
            logger.info("All actions unregistered")

        } catch (e: Exception) {
            logger.error("Failed to unregister actions", e)
        }
    }

    /**
     * Remove actions from context menu
     */
    private fun removeFromContextMenu() {
        try {
            val actionManager = ActionManager.getInstance()
            val editorPopupGroup = actionManager.getAction("EditorPopupMenu") as? DefaultActionGroup

            if (editorPopupGroup != null) {
                val actionsToRemove = mutableListOf<AnAction>()

                // Find actions to remove
                editorPopupGroup.childActionsOrStubs.forEach { action ->
                    if (action is AnAction) {
                        val actionId = ActionManager.getInstance().getId(action)
                        if (actionId?.startsWith("switch2ai.") == true) {
                            actionsToRemove.add(action)
                        }
                    } else if (action is Separator && action.text == "Switch2AI") {
                        actionsToRemove.add(action)
                    }
                }

                // Remove actions
                actionsToRemove.forEach { action ->
                    editorPopupGroup.remove(action)
                }

                logger.info("Removed ${actionsToRemove.size} items from context menu")
            }

        } catch (e: Exception) {
            logger.error("Failed to remove actions from context menu", e)
        }
    }

    /**
     * Check if configuration is the same
     */
    private fun isConfigSame(newConfig: PluginConfig): Boolean {
        val current = currentConfig ?: return false

        // Compare custom commands
        if (current.customCommands.size != newConfig.customCommands.size) return false

        for ((key, newCommand) in newConfig.customCommands) {
            val currentCommand = current.customCommands[key] ?: return false

            if (currentCommand.id != newCommand.id ||
                    currentCommand.shortcut != newCommand.shortcut ||
                    currentCommand.command != newCommand.command) {
                return false
            }
        }

        // Compare prompt configuration
        if (current.promptConfiguration.currentAI != newConfig.promptConfiguration.currentAI ||
                current.promptConfiguration.customAIs != newConfig.promptConfiguration.customAIs ||
                current.promptConfiguration.shortcutCommands != newConfig.promptConfiguration.shortcutCommands) {
            return false
        }

        return true
    }

    /**
     * Deep copy configuration
     */
    private fun deepCopyConfig(config: PluginConfig): PluginConfig {
        return config.copy(
                promptConfiguration = config.promptConfiguration.copy(
                        customAIs = config.promptConfiguration.customAIs.toMutableMap(),
                        shortcutCommands = config.promptConfiguration.shortcutCommands.toMutableMap()
                ),
                customCommands = config.customCommands.toMutableMap()
        )
    }

    /**
     * Get action ID
     */
    private fun getActionId(commandId: String): String {
        return "switch2ai.$commandId"
    }

    /**
     * Custom command action class
     */
    private inner class CustomCommandAction(
            private val command: CustomCommand,
            private val processor: CommandProcessor
    ) : AnAction(command.id, command.description, null) {

        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.BGT
        }

        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            val context = getCurrentFileContext(project) ?: return

            processor.executeCustomCommand(command, context, project)
        }

        override fun update(e: AnActionEvent) {
            val project = e.project
            val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)

            e.presentation.isEnabledAndVisible = project != null &&
                    virtualFile != null &&
                    !virtualFile.isDirectory
        }
    }

    /**
     * Prompt action class
     */
    private inner class PromptAction(
            private val promptConfig: com.github.switch2ai.config.model.PromptAIConfiguration,
            private val processor: CommandProcessor
    ) : AnAction("switch2ai.prompt", "Open AI prompt input popup", null) {

        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.BGT
        }

        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            processor.handlePromptCommand(promptConfig, project)
        }

        override fun update(e: AnActionEvent) {
            val project = e.project
            val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)

            e.presentation.isEnabledAndVisible = project != null &&
                    virtualFile != null &&
                    !virtualFile.isDirectory
        }
    }

    /**
     * Get current file context
     */
    private fun getCurrentFileContext(project: Project): FileContext? {
        try {
            // Get current editor and file information
            val fileEditorManager = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
            val editor = fileEditorManager.selectedTextEditor ?: return null
            val virtualFile = fileEditorManager.selectedFiles.firstOrNull() ?: return null

            val caretModel = editor.caretModel
            val line = caretModel.logicalPosition.line + 1
            val column = caretModel.logicalPosition.column + 1

            val selectedText = editor.selectionModel.selectedText

            return FileContext(
                    filePath = virtualFile.path,
                    projectPath = project.basePath ?: "",
                    line = line,
                    column = column,
                    selectedText = selectedText
            )

        } catch (e: Exception) {
            logger.error("Failed to get file context", e)
            return null
        }
    }

    /**
     * Check for shortcut conflicts
     */
    private fun checkShortcutConflicts(
        keymap: com.intellij.openapi.keymap.Keymap,
        shortcut: com.intellij.openapi.actionSystem.KeyboardShortcut,
        newActionId: String
    ): List<ShortcutConflict> {
        val conflicts = mutableListOf<ShortcutConflict>()
        
        try {
            // Get all actions that use this shortcut
            val conflictingActions = keymap.getActionIds(shortcut)
            
            conflictingActions.forEach { actionId ->
                // Skip if it's the same action (updating existing shortcut)
                if (actionId != newActionId) {
                    val action = ActionManager.getInstance().getAction(actionId)
                    val description = action?.templatePresentation?.text ?: actionId
                    
                    conflicts.add(ShortcutConflict(actionId, description))
                }
            }
            
        } catch (e: Exception) {
            logger.error("Failed to check shortcut conflicts", e)
        }
        
        return conflicts
    }

    /**
     * Show shortcut conflict dialog to user and get their choice
     */
    private fun showShortcutConflictDialog(
        project: Project,
        shortcutText: String,
        conflicts: List<ShortcutConflict>
    ): Boolean {
        return try {
            val conflictList = conflicts.joinToString("\n") { "• ${it.description} (${it.actionId})" }
            val message = """
                Shortcut conflict detected for: $shortcutText
                
                This shortcut is already used by:
                $conflictList
                
                Do you want to replace the existing shortcut with the new one?
                (The conflicting shortcuts will be removed first)
            """.trimIndent()
            
            val result = javax.swing.JOptionPane.showConfirmDialog(
                null,
                message,
                "Shortcut Conflict",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE
            )
            
            result == javax.swing.JOptionPane.YES_OPTION
            
        } catch (e: Exception) {
            logger.error("Failed to show shortcut conflict dialog", e)
            // Default to false (don't replace) if dialog fails
            false
        }
    }

    /**
     * Remove conflicting shortcuts from keymap
     */
    private fun removeConflictingShortcuts(
        keymap: com.intellij.openapi.keymap.Keymap,
        shortcut: com.intellij.openapi.actionSystem.KeyboardShortcut,
        conflicts: List<ShortcutConflict>
    ) {
        try {
            conflicts.forEach { conflict ->
                // Remove the specific shortcut from the conflicting action
                keymap.removeShortcut(conflict.actionId, shortcut)
                logger.info("Removed conflicting shortcut from action: ${conflict.actionId}")
            }
        } catch (e: Exception) {
            logger.error("Failed to remove conflicting shortcuts", e)
        }
    }

    /**
     * Data class for shortcut conflict information
     */
    private data class ShortcutConflict(
        val actionId: String,
        val description: String
    )
}