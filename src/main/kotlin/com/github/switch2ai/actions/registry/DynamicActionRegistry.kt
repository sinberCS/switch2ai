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
 * 动态动作注册器
 * 负责自定义命令的动态注册和管理
 */
@Service(Service.Level.PROJECT)
class DynamicActionRegistry {

    private val logger = Logger.getInstance(DynamicActionRegistry::class.java)
    private val commandProcessor = CommandProcessor()

    // 当前注册的动作ID列表
    private val registeredActionIds = mutableSetOf<String>()

    // 当前生效的配置缓存
    private var currentConfig: PluginConfig? = null

    companion object {
        fun getInstance(project: Project): DynamicActionRegistry {
            return project.getService(DynamicActionRegistry::class.java)
        }
    }

    /**
     * 注册所有自定义命令
     */
    fun registerAllCustomCommands(project: Project, config: PluginConfig) {
        if (isConfigSame(config)) {
            logger.info("配置未变更，跳过重新注册")
            return
        }

        // 清理旧的注册
        unregisterAllActions()

        // 注册自定义命令
        config.customCommands.values.forEach { command ->
            registerCustomCommand(project, command)
        }

        // 注册提示词命令（固定快捷键）
        registerPromptAction(project, config)

        // 注册右键菜单
        registerContextMenus(project, config.customCommands.values)

        // 更新配置缓存
        currentConfig = deepCopyConfig(config)

        logger.info("已注册 ${registeredActionIds.size} 个动作")
    }

    /**
     * 注册单个自定义命令
     */
    private fun registerCustomCommand(project: Project, command: CustomCommand) {
        try {
            val actionManager = ActionManager.getInstance()
            val actionId = getActionId(command.id)

            // 创建动态动作
            val action = CustomCommandAction(command, commandProcessor)

            // 注册动作
            actionManager.registerAction(actionId, action)
            registeredActionIds.add(actionId)

            // 注册快捷键
            if (command.shortcut.isNotEmpty()) {
                registerShortcut(project, actionId, command.shortcut)
            }

            logger.info("已注册自定义命令: ${command.id} (${actionId}) 快捷键: ${command.shortcut}")

        } catch (e: Exception) {
            logger.error("注册自定义命令失败: ${command.id}", e)
        }
    }

    /**
     * 注册提示词动作
     */
    private fun registerPromptAction(project: Project, config: PluginConfig) {
        try {
            val actionManager = ActionManager.getInstance()
            val actionId = "switch2ai.promptAction"

            // 创建提示词动作
            val action = PromptAction(config.promptConfiguration, commandProcessor)

            // 注册动作
            actionManager.registerAction(actionId, action)
            registeredActionIds.add(actionId)

            // 注册固定快捷键 Alt+Shift+P
            registerShortcut(project, actionId, "Alt+Shift+K")

            logger.info("已注册提示词动作: $actionId")

        } catch (e: Exception) {
            logger.error("注册提示词动作失败", e)
        }
    }

    /**
     * 处理快捷键格式，转换为IDEA插件标准格式
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
     * 注册快捷键
     */
    private fun registerShortcut(project: Project, actionId: String, shortcutText: String) {
        try {
            // 使用统一的快捷键处理方法
            val normalizedShortcut = processKeyMap(shortcutText)
            val keyStroke = KeyStroke.getKeyStroke(normalizedShortcut)

            if (keyStroke != null) {
                val keymap = com.intellij.openapi.keymap.KeymapManager.getInstance().activeKeymap
                val shortcut = com.intellij.openapi.actionSystem.KeyboardShortcut(keyStroke, null)
                keymap.addShortcut(actionId, shortcut)
                logger.info("已注册快捷键: $shortcutText -> $actionId")
            } else {
                logger.error("无法解析快捷键: $shortcutText (处理后: $normalizedShortcut)")
            }
        } catch (e: Exception) {
            logger.error("注册快捷键失败: $shortcutText -> $actionId", e)
        }
    }

    /**
     * 注册右键菜单
     */
    private fun registerContextMenus(project: Project, commands: Collection<CustomCommand>) {
        try {
            val actionManager = ActionManager.getInstance()
            val editorPopupGroup = actionManager.getAction("EditorPopupMenu") as? DefaultActionGroup

            if (editorPopupGroup != null) {
                // 添加分隔符
                editorPopupGroup.add(Separator("Switch2AI"))

                // 添加自定义命令到右键菜单
                commands.forEach { command ->
                    val actionId = getActionId(command.id)
                    val action = actionManager.getAction(actionId)
                    if (action != null) {
                        editorPopupGroup.add(action)
                    }
                }

                // 添加提示词动作
                val promptAction = actionManager.getAction("switch2ai.promptAction")
                if (promptAction != null) {
                    editorPopupGroup.add(promptAction)
                }

                logger.info("已注册右键菜单")
            }

        } catch (e: Exception) {
            logger.error("注册右键菜单失败", e)
        }
    }

    /**
     * 注销所有动作
     */
    fun unregisterAllActions() {
        try {
            val actionManager = ActionManager.getInstance()
            val keymap = com.intellij.openapi.keymap.KeymapManager.getInstance().activeKeymap

            // 移除快捷键和动作
            registeredActionIds.forEach { actionId ->
                try {
                    // 移除快捷键
                    keymap.removeAllActionShortcuts(actionId)

                    // 注销动作
                    actionManager.unregisterAction(actionId)
                } catch (e: Exception) {
                    logger.error("注销动作失败: $actionId", e)
                }
            }

            // 清理右键菜单
            removeFromContextMenu()

            registeredActionIds.clear()
            logger.info("已注销所有动作")

        } catch (e: Exception) {
            logger.error("注销动作失败", e)
        }
    }

    /**
     * 从右键菜单移除动作
     */
    private fun removeFromContextMenu() {
        try {
            val actionManager = ActionManager.getInstance()
            val editorPopupGroup = actionManager.getAction("EditorPopupMenu") as? DefaultActionGroup

            if (editorPopupGroup != null) {
                val actionsToRemove = mutableListOf<AnAction>()

                // 查找需要移除的动作
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

                // 移除动作
                actionsToRemove.forEach { action ->
                    editorPopupGroup.remove(action)
                }

                logger.info("已从右键菜单移除 ${actionsToRemove.size} 个项目")
            }

        } catch (e: Exception) {
            logger.error("从右键菜单移除动作失败", e)
        }
    }

    /**
     * 检查配置是否相同
     */
    private fun isConfigSame(newConfig: PluginConfig): Boolean {
        val current = currentConfig ?: return false

        // 比较自定义命令
        if (current.customCommands.size != newConfig.customCommands.size) return false

        for ((key, newCommand) in newConfig.customCommands) {
            val currentCommand = current.customCommands[key] ?: return false

            if (currentCommand.id != newCommand.id ||
                    currentCommand.shortcut != newCommand.shortcut ||
                    currentCommand.command != newCommand.command) {
                return false
            }
        }

        // 比较提示词配置
        if (current.promptConfiguration.currentAI != newConfig.promptConfiguration.currentAI ||
                current.promptConfiguration.customAIs != newConfig.promptConfiguration.customAIs ||
                current.promptConfiguration.shortcutCommands != newConfig.promptConfiguration.shortcutCommands) {
            return false
        }

        return true
    }

    /**
     * 深度复制配置
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
     * 获取动作ID
     */
    private fun getActionId(commandId: String): String {
        return "switch2ai.$commandId"
    }

    /**
     * 自定义命令动作类
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
     * 提示词动作类
     */
    private inner class PromptAction(
            private val promptConfig: com.github.switch2ai.config.model.PromptAIConfiguration,
            private val processor: CommandProcessor
    ) : AnAction("switch2ai.prompt", "打开AI提示词输入弹窗", null) {

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
     * 获取当前文件上下文
     */
    private fun getCurrentFileContext(project: Project): FileContext? {
        try {
            // 获取当前编辑器和文件信息
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
            logger.error("获取文件上下文失败", e)
            return null
        }
    }
}