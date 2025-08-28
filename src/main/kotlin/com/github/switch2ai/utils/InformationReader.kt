package com.github.switch2ai.utils

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.diagnostic.Logger

/**
 * 信息读取模块
 * 负责在执行命令前获取执行命令所需的内容
 */
class InformationReader {
    private val logger = Logger.getInstance(InformationReader::class.java)
    
    /**
     * 执行命令所需的信息
     */
    data class CommandContext(
        val filePath: String,
        val line: Int,
        val column: Int,
        val selection: String?,
        val projectPath: String?
    )
    
    /**
     * 从ActionEvent中读取命令执行上下文信息
     */
    fun readCommandContext(event: AnActionEvent): CommandContext? {
        try {
            val virtualFile: VirtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val editor: Editor? = event.getData(CommonDataKeys.EDITOR)
            val project = event.project
            
            // 获取文件路径
            val filePath = virtualFile.path
            
            // 获取光标位置
            val line = editor?.caretModel?.logicalPosition?.line?.plus(1) ?: 1
            val column = editor?.caretModel?.logicalPosition?.column?.plus(1) ?: 1
            
            // 获取选中的文本
            val selection = editor?.selectionModel?.selectedText
            
            // 获取项目路径
            val projectPath = project?.basePath
            
            return CommandContext(
                filePath = filePath,
                line = line,
                column = column,
                selection = selection,
                projectPath = projectPath
            )
        } catch (e: Exception) {
            logger.error("Failed to read command context: ${e.message}", e)
            return null
        }
    }
    
    /**
     * 替换命令字符串中的变量
     */
    fun replaceVariables(command: String, context: CommandContext): String {
        return command
            .replace("\${filePath}", context.filePath)
            .replace("\${line}", context.line.toString())
            .replace("\${column}", context.column.toString())
            .replace("\${selection}", context.selection ?: "")
            .replace("\${projectPath}", context.projectPath ?: "")
    }
    
    /**
     * 验证命令上下文是否有效
     */
    fun isValidContext(context: CommandContext?): Boolean {
        return context != null && 
               context.filePath.isNotEmpty() && 
               context.line > 0 && 
               context.column > 0
    }
}
