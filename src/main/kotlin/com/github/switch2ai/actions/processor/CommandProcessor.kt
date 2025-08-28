package com.github.switch2ai.actions.processor

import com.github.switch2ai.config.model.*
import com.github.switch2ai.ui.dialog.PromptInputPopup
import com.github.switch2ai.utils.InformationReader
import com.github.switch2ai.utils.ShortcutCommandReplacer
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import java.io.File

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
            
            executeTerminalCommand(expandedCommand, project, "执行命令: ${command.id}")
            
        } catch (e: Exception) {
            logger.error("执行自定义命令失败: ${command.id}", e)
            Messages.showErrorDialog(
                project,
                "执行命令失败: ${e.message}",
                "命令执行错误"
            )
        }
    }
    
    /**
     * 处理提示词命令
     */
    fun handlePromptCommand(promptConfig: PromptAIConfiguration, project: Project) {
        try {
            // 获取当前文件上下文
            val context = getCurrentFileContext(project) ?: return
            
            // 获取当前编辑器实例
            val editor = getCurrentEditor(project) ?: return
            
            // 显示提示词输入弹窗（在光标位置的小弹窗）
            val popup = PromptInputPopup(
                project,
                editor,
                promptConfig,
                context.filePath,
                context.line,
                context.column
            )
            
            // 使用异步回调模式
            popup.showAndGetResult { result ->
                if (result != null) {
                    executePromptCommand(result.aiName, result.promptText, context, promptConfig, project)
                }
            }
            
        } catch (e: Exception) {
            logger.error("处理提示词命令失败", e)
            Messages.showErrorDialog(
                project,
                "处理提示词命令失败: ${e.message}",
                "提示词处理错误"
            )
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
            // 获取AI配置
            val aiConfig = promptConfig.customAIs[aiName]
            if (aiConfig == null) {
                Messages.showErrorDialog(project, "未找到AI配置: $aiName", "配置错误")
                return
            }

            
            // 创建快捷命令替换器并处理提示词
            val shortcutReplacer = ShortcutCommandReplacer(promptConfig)
            val expandedPrompt = shortcutReplacer.replaceShortcuts(promptText)
            
            // 扩展命令模板
            val commandContext = context.copy()
            val expandedCommand = expandPromptCommandTemplate(aiConfig.command, expandedPrompt, commandContext)
            
            logger.info("执行提示词命令 - AI: $aiName, 原始提示词: $promptText, 展开后提示词: $expandedPrompt")
            logger.info("展开后命令: $expandedCommand")
            
            // 在IDE终端中执行命令
            executeTerminalCommand(expandedCommand, project, "AI提示词执行: ${aiConfig.displayName}")
            
        } catch (e: Exception) {
            logger.error("执行提示词命令失败", e)
            Messages.showErrorDialog(
                project,
                "执行AI命令失败: ${e.message}",
                "AI命令执行错误"
            )
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
    private fun expandPromptCommandTemplate(template: String, prompt: String, context: FileContext): String {
        return template
            .replace("\${prompt}", prompt)
            .replace("\${filePath}", context.filePath)
            .replace("\${projectPath}", context.projectPath)
            .replace("\${line}", context.line.toString())
            .replace("\${column}", context.column.toString())
            .replace("\${selectedText}", context.selectedText ?: "")
    }
    
    /**
     * 在IDE终端中执行命令
     */
    private fun executeTerminalCommand(command: String, project: Project, title: String) {
        try {
            // 获取或创建终端工具窗口
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val terminalToolWindow = toolWindowManager.getToolWindow("Terminal")
            
            if (terminalToolWindow != null) {
                // 激活终端窗口
                terminalToolWindow.activate(null)
                
                // 创建命令行
                val commandLine = GeneralCommandLine()
                
                // 根据操作系统设置shell
                when {
                    System.getProperty("os.name").toLowerCase().contains("win") -> {
                        commandLine.exePath = "cmd"
                        commandLine.addParameter("/c")
                        commandLine.addParameter(command)
                    }
                    else -> {
                        commandLine.exePath = "/bin/sh"
                        commandLine.addParameter("-c")
                        commandLine.addParameter(command)
                    }
                }
                
                // 设置工作目录为项目根目录
                commandLine.workDirectory = File(project.basePath ?: ".")
                
                // 创建进程处理器
                val processHandler: ProcessHandler = ProcessHandlerFactory.getInstance()
                    .createColoredProcessHandler(commandLine)
                
                // 添加进程结束监听器
                ProcessTerminatedListener.attach(processHandler)
                
                // 启动进程
                processHandler.startNotify()
                
                logger.info("命令执行启动: $command")
                
            } else {
                // 如果没有终端窗口，使用运行时执行
                executeRuntimeCommand(command, project)
            }
            
        } catch (e: ExecutionException) {
            logger.error("终端命令执行失败: $command", e)
            // 回退到运行时执行
            executeRuntimeCommand(command, project)
        }
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
        try {
            val fileEditorManager = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
            return fileEditorManager.selectedTextEditor
        } catch (e: Exception) {
            logger.error("获取当前编辑器失败", e)
            return null
        }
    }
    
    /**
     * 获取当前文件上下文
     */
    private fun getCurrentFileContext(project: Project): FileContext? {
        try {
            // 获取当前编辑器和文件信息
            val editor = getCurrentEditor(project) ?: return null
            val fileEditorManager = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
            val virtualFile = fileEditorManager.selectedFiles.firstOrNull() ?: return null
            
            val caretModel = editor.caretModel
            val line = caretModel.logicalPosition.line + 1  // 转换为1基础行号
            val column = caretModel.logicalPosition.column + 1  // 转换为1基础列号
            
            val selectedText = editor.selectionModel.selectedText
            
            return FileContext(
                filePath = virtualFile.path,
                projectPath = project.basePath ?: "",
                line = line,
                column = column,
                selectedText = selectedText
            )
            
        } catch (e: Exception) {
            logger.error("获取文件上下文失败", e)
            return null
        }
    }
}