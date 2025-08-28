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
 * 应用设置配置界面
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
        
        // 创建选项卡面板
        val tabbedPane = JTabbedPane()
        
        // AI配置选项卡
        tabbedPane.addTab("AI配置", createAIConfigPanel())
        
        // 自定义命令选项卡
        tabbedPane.addTab("自定义命令", createCustomCommandPanel())
        
        // 快捷命令选项卡
        tabbedPane.addTab("快捷命令", createShortcutCommandPanel())
        
        mainPanel!!.add(tabbedPane, BorderLayout.CENTER)
        
        // 加载当前配置
        loadCurrentConfig()
        
        return mainPanel!!
    }
    
    /**
     * 创建AI配置面板
     */
    private fun createAIConfigPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)
        
        // 当前AI选择
        val topPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.anchor = GridBagConstraints.WEST
        gbc.insets = JBUI.insets(0, 0, 10, 10)
        topPanel.add(JBLabel("当前AI:"), gbc)
        
        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        currentAIComboBox = JComboBox<String>()
        topPanel.add(currentAIComboBox, gbc)
        
        panel.add(topPanel, BorderLayout.NORTH)
        
        // AI配置表格
        val tableModel = object : DefaultTableModel() {
            init {
                setColumnIdentifiers(arrayOf("名称", "显示名称", "命令模板", "描述"))
            }
            
            override fun getColumnClass(columnIndex: Int): Class<*> {
                return String::class.java
            }
            
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return true // 所有列都可编辑
            }
        }
        
        aiConfigTable = JBTable(tableModel)
        aiConfigTable!!.fillsViewportHeight = true
        
        val scrollPane = JScrollPane(aiConfigTable)
        panel.add(scrollPane, BorderLayout.CENTER)
        
        // 按钮面板
        val buttonPanel = JPanel()
        val addButton = JButton("添加AI")
        val removeButton = JButton("删除AI")
        val resetButton = JButton("重置默认")
        
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
     * 创建自定义命令面板
     */
    private fun createCustomCommandPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)
        
        // 自定义命令表格
        val tableModel = object : DefaultTableModel() {
            init {
                setColumnIdentifiers(arrayOf("ID", "快捷键", "命令", "描述"))
            }
            
            override fun getColumnClass(columnIndex: Int): Class<*> {
                return String::class.java
            }
            
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return column != 0 // ID列不可编辑
            }
        }
        
        customCommandTable = JBTable(tableModel)
        customCommandTable!!.fillsViewportHeight = true
        
        val scrollPane = JScrollPane(customCommandTable)
        panel.add(scrollPane, BorderLayout.CENTER)
        
        // 按钮面板
        val buttonPanel = JPanel()
        val addButton = JButton("添加命令")
        val removeButton = JButton("删除命令")
        val resetButton = JButton("重置默认")
        
        addButton.addActionListener { addCustomCommand() }
        removeButton.addActionListener { removeCustomCommand() }
        resetButton.addActionListener { resetCustomCommands() }
        
        buttonPanel.add(addButton)
        buttonPanel.add(removeButton)
        buttonPanel.add(resetButton)
        
        panel.add(buttonPanel, BorderLayout.SOUTH)
        
        return panel
    }
    
    /**
     * 创建快捷命令面板
     */
    private fun createShortcutCommandPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)
        
        // 说明标签
        val infoLabel = JBLabel(
            "<html>快捷命令允许您定义简短的命令替换为完整的提示词。<br>" +
            "例如：\$test 将被替换为 \"请为这个函数编写单元测试...\"</html>"
        )
        panel.add(infoLabel, BorderLayout.NORTH)
        
        // 快捷命令表格
        val tableModel = object : DefaultTableModel() {
            init {
                setColumnIdentifiers(arrayOf("快捷命令", "替换提示词"))
            }
            
            override fun isCellEditable(row: Int, column: Int): Boolean = true
        }
        
        shortcutCommandTable = JBTable(tableModel)
        shortcutCommandTable!!.fillsViewportHeight = true
        
        val scrollPane = JScrollPane(shortcutCommandTable)
        panel.add(scrollPane, BorderLayout.CENTER)
        
        // 按钮面板
        val buttonPanel = JPanel()
        val addButton = JButton("添加快捷命令")
        val removeButton = JButton("删除快捷命令")
        val resetButton = JButton("重置默认")
        
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
     * 加载当前配置
     */
    private fun loadCurrentConfig() {
        val config = settingsState.getCurrentConfig()
        
        // 加载AI配置
        loadAIConfig(config.promptConfiguration)
        
        // 加载自定义命令配置
        loadCustomCommandConfig(config.customCommands)
        
        // 加载快捷命令配置
        loadShortcutCommandConfig(config.promptConfiguration.shortcutCommands)
    }
    
    /**
     * 加载AI配置
     */
    private fun loadAIConfig(promptConfig: PromptAIConfiguration) {
        // 更新当前AI下拉框
        val comboBoxModel = DefaultComboBoxModel<String>()
        promptConfig.customAIs.values.forEach { ai ->
            comboBoxModel.addElement(ai.displayName)
        }
        currentAIComboBox?.model = comboBoxModel
        
        // 设置当前选中的AI
        val currentAI = promptConfig.getCurrentAI()
        if (currentAI != null) {
            currentAIComboBox?.selectedItem = currentAI.displayName
        }
        
        // 更新AI配置表格
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
     * 加载自定义命令配置
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
     * 加载快捷命令配置
     */
    private fun loadShortcutCommandConfig(shortcuts: Map<String, String>) {
        val tableModel = shortcutCommandTable?.model as DefaultTableModel
        tableModel.rowCount = 0
        
        shortcuts.forEach { (shortcut, replacement) ->
            tableModel.addRow(arrayOf(shortcut, replacement))
        }
    }
    
    /**
     * 添加AI配置
     */
    private fun addAIConfig() {
        val name = JOptionPane.showInputDialog(mainPanel, "请输入AI名称:")
        if (!name.isNullOrBlank()) {
            val displayName = JOptionPane.showInputDialog(mainPanel, "请输入显示名称:", name)
            val command = JOptionPane.showInputDialog(mainPanel, "请输入命令模板:")
            val description = JOptionPane.showInputDialog(mainPanel, "请输入描述信息:")
            
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
     * 删除AI配置
     */
    private fun removeAIConfig() {
        val selectedRow = aiConfigTable?.selectedRow ?: -1
        if (selectedRow >= 0) {
            val tableModel = aiConfigTable?.model as DefaultTableModel
            tableModel.removeRow(selectedRow)
        }
    }
    
    /**
     * 重置AI配置
     */
    private fun resetAIConfig() {
        val defaultConfig = PluginConfig.getDefaultConfig()
        loadAIConfig(defaultConfig.promptConfiguration)
    }
    
    /**
     * 添加自定义命令
     */
    private fun addCustomCommand() {
        val id = JOptionPane.showInputDialog(mainPanel, "请输入命令ID:")
        if (!id.isNullOrBlank()) {
            val shortcut = JOptionPane.showInputDialog(mainPanel, "请输入快捷键:")
            val command = JOptionPane.showInputDialog(mainPanel, "请输入命令模板:")
            val description = JOptionPane.showInputDialog(mainPanel, "请输入描述信息:")
            
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
     * 删除自定义命令
     */
    private fun removeCustomCommand() {
        val selectedRow = customCommandTable?.selectedRow ?: -1
        if (selectedRow >= 0) {
            val tableModel = customCommandTable?.model as DefaultTableModel
            tableModel.removeRow(selectedRow)
        }
    }
    
    /**
     * 重置自定义命令
     */
    private fun resetCustomCommands() {
        val defaultConfig = PluginConfig.getDefaultConfig()
        loadCustomCommandConfig(defaultConfig.customCommands)
    }
    
    /**
     * 添加快捷命令
     */
    private fun addShortcutCommand() {
        val shortcut = JOptionPane.showInputDialog(mainPanel, "请输入快捷命令 (例如: \$test):")
        if (!shortcut.isNullOrBlank()) {
            val replacement = JOptionPane.showInputDialog(mainPanel, "请输入替换提示词:")
            if (!replacement.isNullOrBlank()) {
                val tableModel = shortcutCommandTable?.model as DefaultTableModel
                tableModel.addRow(arrayOf(shortcut.trim(), replacement.trim()))
            }
        }
    }
    
    /**
     * 删除快捷命令
     */
    private fun removeShortcutCommand() {
        val selectedRow = shortcutCommandTable?.selectedRow ?: -1
        if (selectedRow >= 0) {
            val tableModel = shortcutCommandTable?.model as DefaultTableModel
            tableModel.removeRow(selectedRow)
        }
    }
    
    /**
     * 重置快捷命令
     */
    private fun resetShortcutCommands() {
        val defaultConfig = PluginConfig.getDefaultConfig()
        loadShortcutCommandConfig(defaultConfig.promptConfiguration.shortcutCommands)
    }

    // AppSettingsConfigurable.kt
    override fun isModified(): Boolean {
        val currentUIConfig = buildConfigFromUI()
        val savedConfig = settingsState.getCurrentConfig()
        // data class 的 equals 会进行深比较
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
                "保存配置失败: ${e.message}",
                "错误",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }
    
    /**
     * 从UI构建配置
     */
    private fun buildConfigFromUI(): PluginConfig {
        // 构建AI配置
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
        
        // 构建快捷命令配置
        val shortcutCommandMap = mutableMapOf<String, String>()
        val shortcutTableModel = shortcutCommandTable?.model as DefaultTableModel
        
        for (i in 0 until shortcutTableModel.rowCount) {
            val shortcut = shortcutTableModel.getValueAt(i, 0) as String
            val replacement = shortcutTableModel.getValueAt(i, 1) as String
            shortcutCommandMap[shortcut] = replacement
        }
        
        // 构建自定义命令配置
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
        
        // 获取当前AI
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