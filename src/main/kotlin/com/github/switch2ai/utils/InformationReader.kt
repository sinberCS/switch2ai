package com.github.switch2ai.utils

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.diagnostic.Logger

/**
 * Information Reader Module
 * Responsible for obtaining the content required to execute commands before execution
 */
class InformationReader {
    private val logger = Logger.getInstance(InformationReader::class.java)
    
    /**
     * Information required to execute commands
     */
    data class CommandContext(
        val filePath: String,
        val line: Int,
        val column: Int,
        val selection: String?,
        val projectPath: String?
    )
    
    /**
     * Read command execution context information from ActionEvent
     */
    fun readCommandContext(event: AnActionEvent): CommandContext? {
        try {
            val virtualFile: VirtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val editor: Editor? = event.getData(CommonDataKeys.EDITOR)
            val project = event.project
            
            // Get file path
            val filePath = virtualFile.path
            
            // Get cursor position
            val line = editor?.caretModel?.logicalPosition?.line?.plus(1) ?: 1
            val column = editor?.caretModel?.logicalPosition?.column?.plus(1) ?: 1
            
            // Get selected text
            val selection = editor?.selectionModel?.selectedText
            
            // Get project path
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
     * Replace variables in command strings
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
     * Validate if command context is valid
     */
    fun isValidContext(context: CommandContext?): Boolean {
        return context != null && 
               context.filePath.isNotEmpty() && 
               context.line > 0 && 
               context.column > 0
    }
}
