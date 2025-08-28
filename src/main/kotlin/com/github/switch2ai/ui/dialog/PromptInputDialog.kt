package com.github.switch2ai.ui.dialog

import com.github.switch2ai.config.model.PromptAIConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

/**
 * 提示词输入弹窗
 */
class PromptInputDialog(
    private val project: Project,
    private val promptConfig: PromptAIConfiguration,
    private val currentFilePath: String,
    private val currentLine: Int,
    private val currentColumn: Int
) : DialogWrapper(project) {

    private lateinit var aiSelector: JComboBox<String>
    private lateinit var promptTextArea: JBTextArea
    private lateinit var fileInfoLabel: JBLabel
    private lateinit var shortcutHelpLabel: JBLabel
    
    private var selectedAI: String = promptConfig.currentAI
    private var promptText: String = ""

    init {
        title = "AI提示词输入"
        setOKButtonText("执行")
        setCancelButtonText("取消")
        init()
        
        // 设置弹窗大小和位置
        setSize(600, 400)
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        panel.border = JBUI.Borders.empty(10)
        
        val gbc = GridBagConstraints()
        
        // 文件信息显示
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 2
        gbc.anchor = GridBagConstraints.WEST
        gbc.insets = JBUI.insets(0, 0, 10, 0)
        
        fileInfoLabel = JBLabel("文件: $currentFilePath | 行: $currentLine | 列: $currentColumn")
        fileInfoLabel.foreground = JBUI.CurrentTheme.Label.disabledForeground()
        panel.add(fileInfoLabel, gbc)
        
        // AI选择器标签
        gbc.gridy = 1
        gbc.gridwidth = 1
        gbc.insets = JBUI.insets(0, 0, 5, 10)
        panel.add(JBLabel("AI模式:"), gbc)
        
        // AI选择器
        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        gbc.insets = JBUI.insets(0, 0, 5, 0)
        
        val enabledAIs = promptConfig.getAIs()
        val aiNames = enabledAIs.map { it.displayName }.toTypedArray()
        aiSelector = JComboBox(aiNames)
        
        // 设置当前选中的AI
        val currentAI = promptConfig.getCurrentAI()
        if (currentAI != null) {
            val index = enabledAIs.indexOfFirst { it.name == currentAI.name }
            if (index >= 0) {
                aiSelector.selectedIndex = index
            }
        }
        
        aiSelector.addActionListener {
            val selectedIndex = aiSelector.selectedIndex
            if (selectedIndex >= 0) {
                selectedAI = enabledAIs[selectedIndex].name
            }
        }
        
        panel.add(aiSelector, gbc)
        
        // 提示词输入标签
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.fill = GridBagConstraints.NONE
        gbc.weightx = 0.0
        gbc.insets = JBUI.insets(10, 0, 5, 10)
        panel.add(JBLabel("提示词:"), gbc)
        
        // 提示词输入框
        gbc.gridx = 0
        gbc.gridy = 3
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.BOTH
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.insets = JBUI.insets(0, 0, 10, 0)
        
        promptTextArea = JBTextArea()
        promptTextArea.rows = 6
        promptTextArea.lineWrap = true
        promptTextArea.wrapStyleWord = true
        promptTextArea.emptyText.text = "请输入提示词，支持快捷命令如: \$test, \$refactor, \$explain..."
        
        // 添加键盘监听器
        promptTextArea.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER && !e.isShiftDown) {
                    // Enter键执行，Shift+Enter换行
                    e.consume()
                    doOKAction()
                } else if (e.keyCode == KeyEvent.VK_ESCAPE) {
                    // Esc键取消
                    e.consume()
                    doCancelAction()
                }
            }
        })
        
        val scrollPane = JBScrollPane(promptTextArea)
        scrollPane.preferredSize = Dimension(500, 150)
        panel.add(scrollPane, gbc)
        
        // 快捷命令帮助
        gbc.gridy = 4
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = JBUI.insets(0, 0, 0, 0)
        
        val shortcuts = promptConfig.shortcutCommands.keys.joinToString(", ")
        shortcutHelpLabel = JBLabel("可用快捷命令: $shortcuts")
        shortcutHelpLabel.foreground = JBUI.CurrentTheme.Label.disabledForeground()
        panel.add(shortcutHelpLabel, gbc)
        
        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return promptTextArea
    }

    override fun doValidate(): ValidationInfo? {
        val text = promptTextArea.text?.trim()
        if (text.isNullOrEmpty()) {
            return ValidationInfo("请输入提示词", promptTextArea)
        }
        return null
    }

    override fun doOKAction() {
        promptText = promptTextArea.text?.trim() ?: ""
        super.doOKAction()
    }

    /**
     * 获取选中的AI
     */
    fun getSelectedAI(): String = selectedAI

    /**
     * 获取输入的提示词
     */
    fun getPromptText(): String = promptText

    /**
     * 显示对话框并返回结果
     */
    fun showAndGetResult(): PromptInputResult? {
        return if (showAndGet()) {
            PromptInputResult(selectedAI, promptText)
        } else {
            null
        }
    }
}

/**
 * 提示词输入结果
 */
data class PromptInputResult(
    val aiName: String,
    val promptText: String
)