package com.github.switch2ai.actions.processor

import com.github.switch2ai.config.model.CustomCommand
import com.github.switch2ai.config.model.FileContext
import com.github.switch2ai.config.model.PromptAIConfiguration
import com.github.switch2ai.ui.dialog.PromptInputPopup
import com.github.switch2ai.utils.ShortcutCommandReplacer
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.jetbrains.plugins.terminal.TerminalToolWindowManager

/**
 * New Command Processor
 * Responsible for handling custom command execution and prompt commands
 */
class CommandProcessor {

    private val logger = Logger.getInstance(CommandProcessor::class.java)

    /**
     * Execute custom command
     */
    fun executeCustomCommand(command: CustomCommand, context: FileContext, project: Project) {
        try {
            val expandedCommand = expandCommandTemplate(command.command, context)
            logger.info("Executing custom command: ${command.id}, expanded command: $expandedCommand")
            executeCommandBackground(expandedCommand, project, "Execute command: ${command.id}")
        } catch (e: Exception) {
            logger.error("Failed to execute custom command: ${command.id}", e)
            Messages.showErrorDialog(project, "Command execution failed: ${e.message}", "Command Execution Error")
        }
    }

    /**
     * Handle prompt command
     */
    fun handlePromptCommand(promptConfig: PromptAIConfiguration, project: Project) {
        try {
            val context = getCurrentFileContext(project) ?: return
            val editor = getCurrentEditor(project) ?: return
            val popup = PromptInputPopup(project, editor, promptConfig, context.filePath, context.line, context.column)
            popup.showAndGetResult { result ->
                if (result != null) {
                    executePromptCommand(result.aiName, result.promptText, context, promptConfig, project)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to handle prompt command", e)
            Messages.showErrorDialog(project, "Failed to handle prompt command: ${e.message}", "Prompt Processing Error")
        }
    }

    /**
     * Execute prompt command
     */
    private fun executePromptCommand(
            aiName: String,
            promptText: String,
            context: FileContext,
            promptConfig: PromptAIConfiguration,
            project: Project
    ) {
        try {
            val aiConfig = promptConfig.customAIs[aiName]
            if (aiConfig == null) {
                Messages.showErrorDialog(project, "AI configuration not found: $aiName", "Configuration Error")
                return
            }
            val shortcutReplacer = ShortcutCommandReplacer(promptConfig)
            val expandedPrompt = shortcutReplacer.replaceShortcuts(promptText)
            val commandContext = context.copy()
            val expandedCommand = expandCommandTemplate(aiConfig.command, expandedPrompt, commandContext)
            logger.info("Executing prompt command - AI: $aiName, original prompt: $promptText, expanded prompt: $expandedPrompt")
            logger.info("Expanded command: $expandedCommand")
            executeInIntegratedTerminal(expandedCommand, project, "AI Prompt Execution: ${aiConfig.displayName}")
        } catch (e: Exception) {
            logger.error("Failed to execute prompt command", e)
            Messages.showErrorDialog(project, "Failed to execute AI command: ${e.message}", "AI Command Execution Error")
        }
    }

    /**
     * Expand command template (general)
     */
    private fun expandCommandTemplate(template: String, context: FileContext): String {
        return template
                .replace("\${filePath}", context.filePath)
                .replace("\${projectPath}", context.projectPath)
                .replace("\${line}", context.line.toString())
                .replace("\${column}", context.column.toString())
                .replace("\${selectedText}", context.selectedText ?: "")
    }

    /**
     * Expand prompt command template
     */
    private fun expandCommandTemplate(template: String, prompt: String, context: FileContext): String {
        val safePrompt = prompt.replace("'", "'\\''")
        return template
                .replace("\${prompt}", safePrompt)
                .replace("\${filePath}", context.filePath)
                .replace("\${projectPath}", context.projectPath)
                .replace("\${line}", context.line.toString())
                .replace("\${column}", context.column.toString())
                .replace("\${selectedText}", context.selectedText ?: "")
    }

    /**
     * Execute command in IDE integrated terminal (using TerminalToolWindowManager correctly)
     */
    private fun executeInIntegratedTerminal(command: String, project: Project, title: String) {
        try {
            val terminalManager = project.getService(TerminalToolWindowManager::class.java)


            var localShellWidgt = terminalManager.createLocalShellWidget(project.basePath, "temp");

            localShellWidgt.executeCommand(command)

            logger.info("Command sent to integrated terminal via TerminalToolWindowManager: $command")
        } catch (e: Exception) {
            logger.error("Unable to execute command in integrated terminal: $command", e)
            Messages.showErrorDialog(
                    project,
                    "Unable to execute command in integrated terminal: ${e.message}\nPlease ensure Terminal plugin is enabled.",
                    "Terminal Execution Error"
            )
        }
    }

    /**
     * Execute command in IDE terminal
     */
    private fun executeCommandBackground(command: String, project: Project, title: String) {
        executeRuntimeCommand(command, project)
    }

    /**
     * Execute command using Runtime (fallback)
     */
    private fun executeRuntimeCommand(command: String, project: Project) {
        try {
            val runtime = Runtime.getRuntime()
            val process = when {
                System.getProperty("os.name").toLowerCase().contains("win") -> {
                    runtime.exec(arrayOf("cmd", "/c", command))
                }
                else -> {
                    runtime.exec(arrayOf("/bin/sh", "-c", command))
                }
            }

            logger.info("Runtime command execution: $command")

        } catch (e: Exception) {
            logger.error("Runtime command execution failed: $command", e)
            Messages.showErrorDialog(
                    project,
                    "Command execution failed: ${e.message}",
                    "Execution Error"
            )
        }
    }


    /**
     * Get current editor instance
     */
    private fun getCurrentEditor(project: Project): com.intellij.openapi.editor.Editor? {
        return try {
            val fileEditorManager = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
            fileEditorManager.selectedTextEditor
        } catch (e: Exception) {
            logger.error("Failed to get current editor", e)
            null
        }
    }

    /**
     * Get current file context
     */
    private fun getCurrentFileContext(project: Project): FileContext? {
        return try {
            val editor = getCurrentEditor(project) ?: return null
            val fileEditorManager = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
            val virtualFile = fileEditorManager.selectedFiles.firstOrNull() ?: return null
            val caretModel = editor.caretModel
            val line = caretModel.logicalPosition.line + 1
            val column = caretModel.logicalPosition.column + 1
            val selectedText = editor.selectionModel.selectedText
            FileContext(
                    filePath = virtualFile.path,
                    projectPath = project.basePath ?: "",
                    line = line,
                    column = column,
                    selectedText = selectedText
            )
        } catch (e: Exception) {
            logger.error("Failed to get file context", e)
            null
        }
    }

}
