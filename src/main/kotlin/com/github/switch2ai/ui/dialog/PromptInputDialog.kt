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
 * Prompt Input Dialog
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
        title = "AI Prompt Input"
        setOKButtonText("Execute")
        setCancelButtonText("Cancel")
        init()
        
        // Set dialog size and position
        setSize(600, 400)
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        panel.border = JBUI.Borders.empty(10)
        
        val gbc = GridBagConstraints()
        
        // File information display
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 2
        gbc.anchor = GridBagConstraints.WEST
        gbc.insets = JBUI.insets(0, 0, 10, 0)
        
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
        gbc.fill = GridBagConstraints.NONE
        gbc.weightx = 0.0
        gbc.insets = JBUI.insets(10, 0, 5, 10)
        panel.add(JBLabel("Prompt:"), gbc)
        
        // Prompt input field
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
        promptTextArea.emptyText.text = "Please enter prompt, supports shortcut commands like: \$test, \$refactor, \$explain..."
        
        // Add keyboard listener
        promptTextArea.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER && !e.isShiftDown) {
                    // Enter key executes, Shift+Enter new line
                    e.consume()
                    doOKAction()
                } else if (e.keyCode == KeyEvent.VK_ESCAPE) {
                    // Esc key cancels
                    e.consume()
                    doCancelAction()
                }
            }
        })
        
        val scrollPane = JBScrollPane(promptTextArea)
        scrollPane.preferredSize = Dimension(500, 150)
        panel.add(scrollPane, gbc)
        
        // Shortcut command help
        gbc.gridy = 4
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = JBUI.insets(0, 0, 0, 0)
        
        val shortcuts = promptConfig.shortcutCommands.keys.joinToString(", ")
        shortcutHelpLabel = JBLabel("Available shortcut commands: $shortcuts")
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
            return ValidationInfo("Please enter prompt", promptTextArea)
        }
        return null
    }

    override fun doOKAction() {
        promptText = promptTextArea.text?.trim() ?: ""
        super.doOKAction()
    }

    /**
     * Get selected AI
     */
    fun getSelectedAI(): String = selectedAI

    /**
     * Get input prompt text
     */
    fun getPromptText(): String = promptText

    /**
     * Show dialog and return result
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
 * Prompt input result
 */
data class PromptInputResult(
    val aiName: String,
    val promptText: String
)