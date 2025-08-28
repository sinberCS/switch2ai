package com.github.switch2ai.ui.dialog

import com.github.switch2ai.config.model.PromptAIConfiguration
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
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
 * 提示词输入弹窗 - 在光标位置显示的小弹窗
 */
class PromptInputPopup(
    private val project: Project,
    private val editor: Editor,
    private val promptConfig: PromptAIConfiguration,
    private val currentFilePath: String,
    private val currentLine: Int,
    private val currentColumn: Int
) {
    
    private lateinit var aiSelector: JComboBox<String>
    private lateinit var promptTextArea: JBTextArea
    private lateinit var fileInfoLabel: JBLabel
    private lateinit var shortcutHelpLabel: JBLabel
    
    private var selectedAI: String = promptConfig.currentAI
    private var promptText: String = ""
    
    private var popup: JBPopup? = null
    
    data class PromptResult(
        val aiName: String,
        val promptText: String
    )
    
    /**
     * 显示弹窗并异步返回结果
     */
    fun showAndGetResult(onResult: (PromptResult?) -> Unit) {
        // 先创建面板，这样 promptTextArea 就会被初始化
        val panel = createPanel(onResult)
        
        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, promptTextArea)  // 聚焦到文本输入框
            .setTitle("AI提示词输入")
            .setResizable(true)
            .setModalContext(false)
            .setRequestFocus(true)
            .setShowBorder(true)
            .setShowShadow(true)
            .setCancelOnWindowDeactivation(true)
            .setCancelOnClickOutside(true)
            .setCancelKeyEnabled(true)
            .setKeyEventHandler { event ->
                when (event.keyCode) {
                    KeyEvent.VK_ENTER -> {
                        if (event.isControlDown) {
                            confirmAndClose(onResult)
                            true
                        } else {
                            false
                        }
                    }
                    KeyEvent.VK_ESCAPE -> {
                        cancelAndClose(onResult)
                        true
                    }
                    else -> false
                }
            }
            .createPopup()
        
        // 在光标位置显示弹窗
        popup?.showInBestPositionFor(editor)
    }
    
    private fun createPanel(onResult: (PromptResult?) -> Unit): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.border = JBUI.Borders.empty(10)
        panel.minimumSize = Dimension(400, 300)
        panel.preferredSize = Dimension(400, 300)
        
        // 使用系统默认背景色，支持暗色主题
        panel.background = JBUI.CurrentTheme.DefaultTabs.background()
        
        val gbc = GridBagConstraints()
        
        // 文件信息显示
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 2
        gbc.anchor = GridBagConstraints.WEST
        gbc.insets = JBUI.insets(0, 0, 8, 0)
        
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
        gbc.gridwidth = 1
        gbc.insets = JBUI.insets(0, 0, 5, 10)
        panel.add(JBLabel("提示词:"), gbc)
        
        // 提示词输入框
        gbc.gridx = 1
        gbc.gridy = 2
        gbc.gridwidth = 1
        gbc.fill = GridBagConstraints.BOTH
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.insets = JBUI.insets(0, 0, 5, 0)
        
        promptTextArea = JBTextArea()
        promptTextArea.lineWrap = true
        promptTextArea.wrapStyleWord = true
        promptTextArea.preferredSize = Dimension(300, 150)
        
        // 添加快捷键支持
        promptTextArea.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.isControlDown && e.keyCode == KeyEvent.VK_ENTER) {
                    confirmAndClose(onResult)
                }
            }
        })
        
        val scrollPane = JBScrollPane(promptTextArea)
        panel.add(scrollPane, gbc)
        
        // 快捷命令帮助
        gbc.gridx = 0
        gbc.gridy = 3
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        gbc.weighty = 0.0
        gbc.insets = JBUI.insets(5, 0, 0, 0)
        
        shortcutHelpLabel = JBLabel("快捷键: Ctrl+Enter 执行, Esc 取消")
        shortcutHelpLabel.foreground = JBUI.CurrentTheme.Label.disabledForeground()
        panel.add(shortcutHelpLabel, gbc)
        
        // 按钮面板
        gbc.gridx = 0
        gbc.gridy = 4
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.NONE
        gbc.anchor = GridBagConstraints.CENTER
        gbc.insets = JBUI.insets(10, 0, 0, 0)
        
        val buttonPanel = createButtonPanel(onResult)
        panel.add(buttonPanel, gbc)
        
        return panel
    }
    
    private fun createButtonPanel(onResult: (PromptResult?) -> Unit): JPanel {
        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.X_AXIS)
        
        val executeButton = JButton("执行")
        executeButton.addActionListener { confirmAndClose(onResult) }
        
        val cancelButton = JButton("取消")
        cancelButton.addActionListener { cancelAndClose(onResult) }
        
        buttonPanel.add(executeButton)
        buttonPanel.add(Box.createHorizontalStrut(10))
        buttonPanel.add(cancelButton)
        
        return buttonPanel
    }
    
    private fun confirmAndClose(onResult: (PromptResult?) -> Unit) {
        promptText = promptTextArea.text.trim()
        if (promptText.isNotEmpty()) {
            val result = PromptResult(selectedAI, promptText)
            popup?.closeOk(null)
            onResult(result)
        }
    }
    
    private fun cancelAndClose(onResult: (PromptResult?) -> Unit) {
        popup?.cancel()
        onResult(null)
    }
}