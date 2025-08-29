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
 * 新的命令处理器
 * 负责处理自定义命令执行和提示词命令
 */
class CommandProcessor {

    private val logger = Logger.getInstance(CommandProcessor::class.java)

    /**
     * 执行自定义命令
     */
    fun executeCustomCommand(command: CustomCommand, context: FileContext, project: Project) {
        try {
            val expandedCommand = expandCommandTemplate(command.command, context)
            logger.info("执行自定义命令: ${command.id}, 展开后命令: $expandedCommand")
            executeCommandBackground(expandedCommand, project, "执行命令: ${command.id}")
        } catch (e: Exception) {
            logger.error("执行自定义命令失败: ${command.id}", e)
            Messages.showErrorDialog(project, "执行命令失败: ${e.message}", "命令执行错误")
        }
    }

    /**
     * 处理提示词命令
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
            logger.error("处理提示词命令失败", e)
            Messages.showErrorDialog(project, "处理提示词命令失败: ${e.message}", "提示词处理错误")
        }
    }

    /**
     * 执行提示词命令
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
                Messages.showErrorDialog(project, "未找到AI配置: $aiName", "配置错误")
                return
            }
            val shortcutReplacer = ShortcutCommandReplacer(promptConfig)
            val expandedPrompt = shortcutReplacer.replaceShortcuts(promptText)
            val commandContext = context.copy()
            val expandedCommand = expandCommandTemplate(aiConfig.command, expandedPrompt, commandContext)
            logger.info("执行提示词命令 - AI: $aiName, 原始提示词: $promptText, 展开后提示词: $expandedPrompt")
            logger.info("展开后命令: $expandedCommand")
            executeInIntegratedTerminal(expandedCommand, project, "AI提示词执行: ${aiConfig.displayName}")
        } catch (e: Exception) {
            logger.error("执行提示词命令失败", e)
            Messages.showErrorDialog(project, "执行AI命令失败: ${e.message}", "AI命令执行错误")
        }
    }

    /**
     * 扩展命令模板（通用）
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
     * 扩展提示词命令模板
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
     * 在IDE集成的终端中执行命令 (使用 TerminalToolWindowManager 的正确方式)
     */
    private fun executeInIntegratedTerminal(command: String, project: Project, title: String) {
        try {
            val terminalManager = project.getService(TerminalToolWindowManager::class.java)


            var localShellWidgt = terminalManager.createLocalShellWidget(project.basePath, "temp");

            localShellWidgt.executeCommand(command)

            logger.info("命令已通过 TerminalToolWindowManager 发送至集成终端: $command")
        } catch (e: Exception) {
            logger.error("无法在集成终端中执行命令: $command", e)
            Messages.showErrorDialog(
                    project,
                    "无法在集成终端中执行命令: ${e.message}\n请确保Terminal插件已启用。",
                    "终端执行错误"
            )
        }
    }

    /**
     * 在IDE终端中执行命令
     */
    private fun executeCommandBackground(command: String, project: Project, title: String) {
        executeRuntimeCommand(command, project)
    }

    /**
     * 使用Runtime执行命令（备用方案）
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

            logger.info("Runtime命令执行: $command")

        } catch (e: Exception) {
            logger.error("Runtime命令执行失败: $command", e)
            Messages.showErrorDialog(
                    project,
                    "命令执行失败: ${e.message}",
                    "执行错误"
            )
        }
    }


    /**
     * 获取当前编辑器实例
     */
    private fun getCurrentEditor(project: Project): com.intellij.openapi.editor.Editor? {
        return try {
            val fileEditorManager = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
            fileEditorManager.selectedTextEditor
        } catch (e: Exception) {
            logger.error("获取当前编辑器失败", e)
            null
        }
    }

    /**
     * 获取当前文件上下文
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
            logger.error("获取文件上下文失败", e)
            null
        }
    }

}
