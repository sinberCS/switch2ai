package com.github.switch2ai.config.settings

import com.github.switch2ai.config.model.*
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*
import javax.swing.table.DefaultTableModel

/**
 * Application Settings Configuration Interface
 */
class AppSettingsConfigurable : Configurable {
    
    private var mainPanel: JPanel? = null
    private var aiConfigTable: JBTable? = null
    private var customCommandTable: JBTable? = null
    private var shortcutCommandTable: JBTable? = null
    private var currentAIComboBox: JComboBox<String>? = null
    
    private val settingsState = AppSettingsState.getInstance()
    
    override fun getDisplayName(): String = "Switch2AI"
    
    override fun createComponent(): JComponent {
        mainPanel = JPanel(BorderLayout())
        
        // Create tabbed panel
        val tabbedPane = JTabbedPane()
        
        // AI Configuration tab
        tabbedPane.addTab("AI Configuration", createAIConfigPanel())
        
        // Custom Commands tab
        tabbedPane.addTab("Custom Commands", createCustomCommandPanel())
        
        // Shortcut Commands tab
        tabbedPane.addTab("Shortcut Commands", createShortcutCommandPanel())
        
        mainPanel!!.add(tabbedPane, BorderLayout.CENTER)
        
        // Load current configuration
        loadCurrentConfig()
        
        return mainPanel!!
    }
    
    /**
     * Create AI Configuration Panel
     */
    private fun createAIConfigPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)
        
        // Current AI Selection
        val topPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.anchor = GridBagConstraints.WEST
        gbc.insets = JBUI.insets(0, 0, 10, 10)
        topPanel.add(JBLabel("Current AI:"), gbc)
        
        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        currentAIComboBox = JComboBox<String>()
        topPanel.add(currentAIComboBox, gbc)
        
        panel.add(topPanel, BorderLayout.NORTH)
        
        // AI Configuration Table
        val tableModel = object : DefaultTableModel() {
            init {
                setColumnIdentifiers(arrayOf("Name", "Display Name", "Command Template", "Description"))
            }
            
            override fun getColumnClass(columnIndex: Int): Class<*> {
                return String::class.java
            }
            
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return true // All columns are editable
            }
        }
        
        aiConfigTable = JBTable(tableModel)
        aiConfigTable!!.fillsViewportHeight = true
        
        val scrollPane = JScrollPane(aiConfigTable)
        panel.add(scrollPane, BorderLayout.CENTER)
        
        // Button Panel
        val buttonPanel = JPanel()
        val addButton = JButton("Add AI")
        val removeButton = JButton("Remove AI")
        val resetButton = JButton("Reset Default")
        
        addButton.addActionListener { addAIConfig() }
        removeButton.addActionListener { removeAIConfig() }
        resetButton.addActionListener { resetAIConfig() }
        
        buttonPanel.add(addButton)
        buttonPanel.add(removeButton)
        buttonPanel.add(resetButton)
        
        panel.add(buttonPanel, BorderLayout.SOUTH)
        
        return panel
    }
    
    /**
     * Create Custom Command Panel
     */
    private fun createCustomCommandPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)
        
        // Custom Commands Table
        val tableModel = object : DefaultTableModel() {
            init {
                setColumnIdentifiers(arrayOf("ID", "Shortcut", "Command", "Description"))
            }
            
            override fun getColumnClass(columnIndex: Int): Class<*> {
                return String::class.java
            }
            
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return column != 0 // ID column is not editable
            }
        }
        
        customCommandTable = JBTable(tableModel)
        customCommandTable!!.fillsViewportHeight = true
        
        val scrollPane = JScrollPane(customCommandTable)
        panel.add(scrollPane, BorderLayout.CENTER)
        
        // Button Panel
        val buttonPanel = JPanel()
        val addButton = JButton("Add Command")
        val removeButton = JButton("Remove Command")
        val resetButton = JButton("Reset Default")
        val checkConflictsButton = JButton("Check Shortcut Conflicts")
        
        addButton.addActionListener { addCustomCommand() }
        removeButton.addActionListener { removeCustomCommand() }
        resetButton.addActionListener { resetCustomCommands() }
        checkConflictsButton.addActionListener { checkShortcutConflicts() }
        
        buttonPanel.add(addButton)
        buttonPanel.add(removeButton)
        buttonPanel.add(resetButton)
        buttonPanel.add(checkConflictsButton)
        
        panel.add(buttonPanel, BorderLayout.SOUTH)
        
        return panel
    }
    
    /**
     * Create Shortcut Command Panel
     */
    private fun createShortcutCommandPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)
        
        // Information Label
        val infoLabel = JBLabel(
            "<html>Shortcut commands allow you to define short commands that replace complete prompts.<br>" +
            "For example: \$test will be replaced with \"Please write unit tests for this function...\"</html>"
        )
        panel.add(infoLabel, BorderLayout.NORTH)
        
        // Shortcut Commands Table
        val tableModel = object : DefaultTableModel() {
            init {
                setColumnIdentifiers(arrayOf("Shortcut Command", "Replacement Prompt"))
            }
            
            override fun isCellEditable(row: Int, column: Int): Boolean = true
        }
        
        shortcutCommandTable = JBTable(tableModel)
        shortcutCommandTable!!.fillsViewportHeight = true
        
        val scrollPane = JScrollPane(shortcutCommandTable)
        panel.add(scrollPane, BorderLayout.CENTER)
        
        // Button Panel
        val buttonPanel = JPanel()
        val addButton = JButton("Add Shortcut Command")
        val removeButton = JButton("Remove Shortcut Command")
        val resetButton = JButton("Reset Default")
        
        addButton.addActionListener { addShortcutCommand() }
        removeButton.addActionListener { removeShortcutCommand() }
        resetButton.addActionListener { resetShortcutCommands() }
        
        buttonPanel.add(addButton)
        buttonPanel.add(removeButton)
        buttonPanel.add(resetButton)
        
        panel.add(buttonPanel, BorderLayout.SOUTH)
        
        return panel
    }
    
    /**
     * Load Current Configuration
     */
    private fun loadCurrentConfig() {
        val config = settingsState.getCurrentConfig()
        
        // Load AI Configuration
        loadAIConfig(config.promptConfiguration)
        
        // Load Custom Command Configuration
        loadCustomCommandConfig(config.customCommands)
        
        // Load Shortcut Command Configuration
        loadShortcutCommandConfig(config.promptConfiguration.shortcutCommands)
    }
    
    /**
     * Load AI Configuration
     */
    private fun loadAIConfig(promptConfig: PromptAIConfiguration) {
        // Update current AI dropdown
        val comboBoxModel = DefaultComboBoxModel<String>()
        promptConfig.customAIs.values.forEach { ai ->
            comboBoxModel.addElement(ai.displayName)
        }
        currentAIComboBox?.model = comboBoxModel
        
        // Set currently selected AI
        val currentAI = promptConfig.getCurrentAI()
        if (currentAI != null) {
            currentAIComboBox?.selectedItem = currentAI.displayName
        }
        
        // Update AI Configuration Table
        val tableModel = aiConfigTable?.model as DefaultTableModel
        tableModel.rowCount = 0
        
        promptConfig.customAIs.values.forEach { ai ->
            tableModel.addRow(arrayOf(
                ai.name,
                ai.displayName,
                ai.command,
                ai.description
            ))
        }
    }
    
    /**
     * Load Custom Command Configuration
     */
    private fun loadCustomCommandConfig(commands: Map<String, CustomCommand>) {
        val tableModel = customCommandTable?.model as DefaultTableModel
        tableModel.rowCount = 0
        
        commands.values.forEach { command ->
            tableModel.addRow(arrayOf(
                command.id,
                command.shortcut,
                command.command,
                command.description
            ))
        }
    }
    
    /**
     * Load Shortcut Command Configuration
     */
    private fun loadShortcutCommandConfig(shortcuts: Map<String, String>) {
        val tableModel = shortcutCommandTable?.model as DefaultTableModel
        tableModel.rowCount = 0
        
        shortcuts.forEach { (shortcut, replacement) ->
            tableModel.addRow(arrayOf(shortcut, replacement))
        }
    }
    
    /**
     * Add AI Configuration
     */
    private fun addAIConfig() {
        val name = JOptionPane.showInputDialog(mainPanel, "Please enter AI name:")
        if (!name.isNullOrBlank()) {
            val displayName = JOptionPane.showInputDialog(mainPanel, "Please enter display name:", name)
            val command = JOptionPane.showInputDialog(mainPanel, "Please enter command template:")
            val description = JOptionPane.showInputDialog(mainPanel, "Please enter description:")
            
            if (!displayName.isNullOrBlank() && !command.isNullOrBlank()) {
                val tableModel = aiConfigTable?.model as DefaultTableModel
                tableModel.addRow(arrayOf(
                    name.trim(),
                    displayName.trim(),
                    command.trim(),
                    description?.trim() ?: ""
                ))
            }
        }
    }
    
    /**
     * Remove AI Configuration
     */
    private fun removeAIConfig() {
        val selectedRow = aiConfigTable?.selectedRow ?: -1
        if (selectedRow >= 0) {
            val tableModel = aiConfigTable?.model as DefaultTableModel
            tableModel.removeRow(selectedRow)
        }
    }
    
    /**
     * Reset AI Configuration
     */
    private fun resetAIConfig() {
        val defaultConfig = PluginConfig.getDefaultConfig()
        loadAIConfig(defaultConfig.promptConfiguration)
    }
    
    /**
     * Add Custom Command
     */
    private fun addCustomCommand() {
        val id = JOptionPane.showInputDialog(mainPanel, "Please enter command ID:")
        if (!id.isNullOrBlank()) {
            val shortcut = JOptionPane.showInputDialog(mainPanel, "Please enter shortcut:")
            val command = JOptionPane.showInputDialog(mainPanel, "Please enter command template:")
            val description = JOptionPane.showInputDialog(mainPanel, "Please enter description:")
            
            if (!command.isNullOrBlank()) {
                val tableModel = customCommandTable?.model as DefaultTableModel
                tableModel.addRow(arrayOf(
                    id.trim(),
                    shortcut?.trim() ?: "",
                    command.trim(),
                    description?.trim() ?: ""
                ))
            }
        }
    }
    
    /**
     * Remove Custom Command
     */
    private fun removeCustomCommand() {
        val selectedRow = customCommandTable?.selectedRow ?: -1
        if (selectedRow >= 0) {
            val tableModel = customCommandTable?.model as DefaultTableModel
            tableModel.removeRow(selectedRow)
        }
    }
    
    /**
     * Reset Custom Commands
     */
    private fun resetCustomCommands() {
        val defaultConfig = PluginConfig.getDefaultConfig()
        loadCustomCommandConfig(defaultConfig.customCommands)
    }
    
    /**
     * Add Shortcut Command
     */
    private fun addShortcutCommand() {
        val shortcut = JOptionPane.showInputDialog(mainPanel, "Please enter shortcut command (e.g.: \$test):")
        if (!shortcut.isNullOrBlank()) {
            val replacement = JOptionPane.showInputDialog(mainPanel, "Please enter replacement prompt:")
            if (!replacement.isNullOrBlank()) {
                val tableModel = shortcutCommandTable?.model as DefaultTableModel
                tableModel.addRow(arrayOf(shortcut.trim(), replacement.trim()))
            }
        }
    }
    
    /**
     * Remove Shortcut Command
     */
    private fun removeShortcutCommand() {
        val selectedRow = shortcutCommandTable?.selectedRow ?: -1
        if (selectedRow >= 0) {
            val tableModel = shortcutCommandTable?.model as DefaultTableModel
            tableModel.removeRow(selectedRow)
        }
    }
    
    /**
     * Reset Shortcut Commands
     */
    private fun resetShortcutCommands() {
        val defaultConfig = PluginConfig.getDefaultConfig()
        loadShortcutCommandConfig(defaultConfig.promptConfiguration.shortcutCommands)
    }

    /**
     * Check for shortcut conflicts
     */
    private fun checkShortcutConflicts() {
        try {
            val keymap = com.intellij.openapi.keymap.KeymapManager.getInstance().activeKeymap
            val commandTableModel = customCommandTable?.model as DefaultTableModel
            val conflicts = mutableListOf<String>()
            
            // Check each custom command shortcut
            for (i in 0 until commandTableModel.rowCount) {
                val shortcut = commandTableModel.getValueAt(i, 1) as String
                val commandId = commandTableModel.getValueAt(i, 0) as String
                
                if (shortcut.isNotEmpty()) {
                    val normalizedShortcut = processKeyMap(shortcut)
                    val keyStroke = javax.swing.KeyStroke.getKeyStroke(normalizedShortcut)
                    
                    if (keyStroke != null) {
                        val keyboardShortcut = com.intellij.openapi.actionSystem.KeyboardShortcut(keyStroke, null)
                        val conflictingActions = keymap.getActionIds(keyboardShortcut)
                        
                        conflictingActions.forEach { actionId ->
                            if (actionId != "switch2ai.$commandId") {
                                val action = com.intellij.openapi.actionSystem.ActionManager.getInstance().getAction(actionId)
                                val description = action?.templatePresentation?.text ?: actionId
                                conflicts.add("• $shortcut: Conflicts with '$description' ($actionId)")
                            }
                        }
                    }
                }
            }
            
            // Check fixed shortcuts
            val fixedShortcuts = listOf(
                "Alt+Shift+K" to "switch2ai.promptAction"
            )
            
            fixedShortcuts.forEach { (shortcut, actionId) ->
                val normalizedShortcut = processKeyMap(shortcut)
                val keyStroke = javax.swing.KeyStroke.getKeyStroke(normalizedShortcut)
                
                if (keyStroke != null) {
                    val keyboardShortcut = com.intellij.openapi.actionSystem.KeyboardShortcut(keyStroke, null)
                    val conflictingActions = keymap.getActionIds(keyboardShortcut)
                    
                    conflictingActions.forEach { conflictActionId ->
                        if (conflictActionId != actionId) {
                            val action = com.intellij.openapi.actionSystem.ActionManager.getInstance().getAction(conflictActionId)
                            val description = action?.templatePresentation?.text ?: conflictActionId
                            conflicts.add("• $shortcut: Conflicts with '$description' ($conflictActionId)")
                        }
                    }
                }
            }
            
            // Show results
            if (conflicts.isEmpty()) {
                JOptionPane.showMessageDialog(
                    mainPanel,
                    "No shortcut conflicts detected!",
                    "Shortcut Conflict Check",
                    JOptionPane.INFORMATION_MESSAGE
                )
            } else {
                val message = "Shortcut conflicts detected:\n\n${conflicts.joinToString("\n")}\n\n" +
                        "You can resolve these conflicts in Settings → Keymap.\n\n" +
                        "Note: When registering shortcuts, you will be prompted to choose whether to replace conflicting shortcuts."
                JOptionPane.showMessageDialog(
                    mainPanel,
                    message,
                    "Shortcut Conflicts Found",
                    JOptionPane.WARNING_MESSAGE
                )
            }
            
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                mainPanel,
                "Failed to check shortcut conflicts: ${e.message}",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    /**
     * Process shortcut format, convert to IDEA plugin standard format
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

    // AppSettingsConfigurable.kt
    override fun isModified(): Boolean {
        val currentUIConfig = buildConfigFromUI()
        val savedConfig = settingsState.getCurrentConfig()
        // data class equals performs deep comparison
        return currentUIConfig != savedConfig
    }
    
    override fun apply() {
        try {
            val newConfig = buildConfigFromUI()
            val project = ProjectManager.getInstance().openProjects.firstOrNull()
            if (project != null) {
                settingsState.updateConfig(newConfig, project)
            }
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                mainPanel,
                "Failed to save configuration: ${e.message}",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }
    
    /**
     * Build Configuration from UI
     */
    private fun buildConfigFromUI(): PluginConfig {
        // Build AI Configuration
        val aiConfigMap = mutableMapOf<String, AIConfig>()
        val aiTableModel = aiConfigTable?.model as DefaultTableModel
        
        for (i in 0 until aiTableModel.rowCount) {
            val name = aiTableModel.getValueAt(i, 0) as String
            val displayName = aiTableModel.getValueAt(i, 1) as String
            val command = aiTableModel.getValueAt(i, 2) as String
            val description = aiTableModel.getValueAt(i, 3) as String
            
            aiConfigMap[name] = AIConfig(
                name = name,
                displayName = displayName,
                command = command,
                description = description
            )
        }
        
        // Build Shortcut Command Configuration
        val shortcutCommandMap = mutableMapOf<String, String>()
        val shortcutTableModel = shortcutCommandTable?.model as DefaultTableModel
        
        for (i in 0 until shortcutTableModel.rowCount) {
            val shortcut = shortcutTableModel.getValueAt(i, 0) as String
            val replacement = shortcutTableModel.getValueAt(i, 1) as String
            shortcutCommandMap[shortcut] = replacement
        }
        
        // Build Custom Command Configuration
        val customCommandMap = mutableMapOf<String, CustomCommand>()
        val commandTableModel = customCommandTable?.model as DefaultTableModel
        
        for (i in 0 until commandTableModel.rowCount) {
            val id = commandTableModel.getValueAt(i, 0) as String
            val shortcut = commandTableModel.getValueAt(i, 1) as String
            val command = commandTableModel.getValueAt(i, 2) as String
            val description = commandTableModel.getValueAt(i, 3) as String
            
            customCommandMap[id] = CustomCommand(
                id = id,
                shortcut = shortcut,
                command = command,
                description = description
            )
        }
        
        // Get current AI
        val currentAI = currentAIComboBox?.selectedItem as? String
        val currentAIName = aiConfigMap.values.find { it.displayName == currentAI }?.name ?: "cursor"
        
        return PluginConfig(
            promptConfiguration = PromptAIConfiguration(
                currentAI = currentAIName,
                customAIs = aiConfigMap,
                shortcutCommands = shortcutCommandMap
            ),
            customCommands = customCommandMap
        )
    }
    
    override fun reset() {
        loadCurrentConfig()
    }
    
    override fun disposeUIResources() {
        mainPanel = null
        aiConfigTable = null
        customCommandTable = null
        shortcutCommandTable = null
        currentAIComboBox = null
    }
}