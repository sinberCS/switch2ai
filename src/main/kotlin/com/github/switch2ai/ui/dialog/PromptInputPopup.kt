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
 * Prompt Input Popup - Small popup displayed at cursor position
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
     * Show popup and return result asynchronously
     */
    fun showAndGetResult(onResult: (PromptResult?) -> Unit) {
        // Create panel first so promptTextArea is initialized
        val panel = createPanel(onResult)
        
        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, promptTextArea)  // Focus on text input
            .setTitle("AI Prompt Input")
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
        
        // Show popup at cursor position
        popup?.showInBestPositionFor(editor)
    }
    
    private fun createPanel(onResult: (PromptResult?) -> Unit): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.border = JBUI.Borders.empty(10)
        panel.minimumSize = Dimension(400, 300)
        panel.preferredSize = Dimension(400, 300)
        
        // Use system default background color, support dark theme
        panel.background = JBUI.CurrentTheme.DefaultTabs.background()
        
        val gbc = GridBagConstraints()
        
        // File information display
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 2
        gbc.anchor = GridBagConstraints.WEST
        gbc.insets = JBUI.insets(0, 0, 8, 0)
        
        fileInfoLabel = JBLabel("File: $currentFilePath | Line: $currentLine | Column: $currentColumn")
        fileInfoLabel.foreground = JBUI.CurrentTheme.Label.disabledForeground()
        panel.add(fileInfoLabel, gbc)
        
        // AI selector label
        gbc.gridy = 1
        gbc.gridwidth = 1
        gbc.insets = JBUI.insets(0, 0, 5, 10)
        panel.add(JBLabel("AI Mode:"), gbc)
        
        // AI selector
        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        gbc.insets = JBUI.insets(0, 0, 5, 0)
        
        val enabledAIs = promptConfig.getAIs()
        val aiNames = enabledAIs.map { it.displayName }.toTypedArray()
        aiSelector = JComboBox(aiNames)
        
        // Set currently selected AI
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
        
        // Prompt input label
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.gridwidth = 1
        gbc.insets = JBUI.insets(0, 0, 5, 10)
        panel.add(JBLabel("Prompt:"), gbc)
        
        // Prompt input field
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
        
        // Add shortcut support
        promptTextArea.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.isControlDown && e.keyCode == KeyEvent.VK_ENTER) {
                    confirmAndClose(onResult)
                }
            }
        })
        
        val scrollPane = JBScrollPane(promptTextArea)
        panel.add(scrollPane, gbc)
        
        // Shortcut command help
        gbc.gridx = 0
        gbc.gridy = 3
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        gbc.weighty = 0.0
        gbc.insets = JBUI.insets(5, 0, 0, 0)
        
        shortcutHelpLabel = JBLabel("Shortcuts: Ctrl+Enter to execute, Esc to cancel")
        shortcutHelpLabel.foreground = JBUI.CurrentTheme.Label.disabledForeground()
        panel.add(shortcutHelpLabel, gbc)
        
        // Button panel
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
        
        val executeButton = JButton("Execute")
        executeButton.addActionListener { confirmAndClose(onResult) }
        
        val cancelButton = JButton("Cancel")
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