# 第三阶段：基于配置的AI、动作注册和快捷键

## 概述
第三阶段成功实现了"基于配置的AI、动作注册和快捷键"功能，使插件能够根据配置动态注册动作、快捷键和右键菜单，支持配置的持久化和动态更新。通过架构优化，实现了更好的代码复用和逻辑简化。

## 已实现的核心功能

### 1. 抽象动态动作注册器 (AbstractDynamicActionRegistry)
**文件**: `src/main/kotlin/com/github/switch2ai/actions/AbstractDynamicActionRegistry.kt`

**功能**:
- 提供动态动作注册的通用逻辑
- 支持快捷键注册、冲突检测、动作管理等
- 使用泛型设计支持不同类型的配置数据
- 实现代码复用和逻辑简化

**核心特性**:
- **泛型设计**: 使用泛型 `T` 支持不同类型的配置数据
- **通用逻辑**: 包含快捷键注册、冲突检测、动作管理等通用功能
- **抽象接口**: 定义子类必须实现的接口，确保一致性
- **代码复用**: 消除重复代码，提高维护性

### 2. 动态动作注册器 (DynamicActionRegistry)
**文件**: `src/main/kotlin/com/github/switch2ai/actions/DynamicActionRegistry.kt`

**功能**:
- 继承自 `AbstractDynamicActionRegistry`，专注于动作注册
- 根据配置动态创建和注册动作
- 自动注册快捷键绑定
- 在编辑器右键菜单中添加动作

**核心特性**:
- **继承架构**: 继承抽象父类，复用通用逻辑
- **代码简化**: 从 380 行减少到约 200 行
- **职责明确**: 专注于动作注册和右键菜单管理
- **配置驱动**: 从 `AppSettingsState` 获取配置

### 3. 动态AI切换动作注册器 (DynamicSwitchAIActionRegistry)
**文件**: `src/main/kotlin/com/github/switch2ai/actions/DynamicSwitchAIActionRegistry.kt`

**功能**:
- 继承自 `AbstractDynamicActionRegistry`，专注于AI切换动作
- 根据配置动态创建AI切换动作
- 支持AI切换的快捷键注册
- 与 `AIStatusBarWidget` 集成

**核心特性**:
- **继承架构**: 继承抽象父类，复用通用逻辑
- **代码简化**: 从 236 行减少到约 100 行
- **AI切换**: 支持动态AI切换动作注册
- **状态同步**: 与状态栏组件保持同步

### 4. 统一配置管理 (AppSettingsState)
**文件**: `src/main/kotlin/com/github/switch2ai/settings/AppSettingsState.kt`

**功能**:
- 合并了原有的 `ConfigurationManager` 功能
- 管理AI列表和动作配置
- 提供配置持久化和动态更新
- 支持配置变更监听和重启检测

**核心特性**:
- **统一管理**: 所有配置逻辑集中在一个地方
- **数据类合并**: 使用统一的 `AIConfigData` 和 `ActionConfigData`
- **持久化**: 利用 IntelliJ 的 `PersistentStateComponent` 机制
- **配置同步**: 支持配置的实时更新和同步

## 技术架构

### 架构优化
通过抽象父类和统一配置管理，实现了更好的代码复用和逻辑简化：

#### **抽象继承架构**
```
AbstractDynamicActionRegistry<T>
    ↓                    ↓
DynamicActionRegistry  DynamicSwitchAIActionRegistry
    ↓                    ↓
ActionManager          AIStatusBarWidget
```

#### **配置管理架构**
```
AppSettingsState (统一配置管理)
    ↓
PersistentStateComponent (持久化)
    ↓
DynamicActionRegistry + DynamicSwitchAIActionRegistry
```

### 模块依赖关系
```
AppSettingsState (统一配置源)
    ↓
AbstractDynamicActionRegistry (抽象父类)
    ↓                    ↓
DynamicActionRegistry  DynamicSwitchAIActionRegistry
    ↓                    ↓
ActionManager          AIStatusBarWidget
```

## 配置系统

### 统一配置管理
通过 `AppSettingsState` 统一管理所有配置，实现了更好的数据一致性和维护性：

#### **配置结构**
```kotlin
data class PluginConfig(
    val aiList: Map<String, AIConfigData>,      // AI配置列表
    val actions: Map<String, ActionConfigData>  // 动作配置列表
)

data class AIConfigData(
    var name: String = "",        // AI名称
    var shortcut: String = ""     // AI切换快捷键
)

data class ActionConfigData(
    var name: String = "",                    // 动作名称
    var description: String = "",             // 动作描述
    var shortCut: String = "",                // 动作快捷键
    var commands: MutableMap<String, String> = mutableMapOf()  // AI命令映射
)
```

#### **数据类合并优化**
- **统一类型**: 使用一套可变的数据类，避免重复定义
- **简化转换**: 不再需要类型转换，直接使用统一的数据类型
- **性能提升**: 避免了不必要的对象创建和复制

### 配置持久化
- 使用IntelliJ的`PersistentStateComponent`接口
- 自动保存到`switch2aiSettings.xml`文件
- 支持配置的热更新和冷启动
- 配置变更时自动同步到所有相关组件

## 快捷键系统

### 支持的快捷键格式
- `option+shift+o`: 打开文件到AI
- `option+shift+t`: 生成单元测试
- `option+shift+1/2/3`: 切换AI

## 右键菜单集成

### 支持的菜单位置
- **编辑器右键菜单**: 在代码编辑器中右键
- **项目视图右键菜单**: 在项目文件树中右键
- **工具菜单**: 在主菜单栏的工具菜单中

### 动态菜单项
- 根据配置自动生成菜单项
- 支持分隔符和分组
- 自动更新菜单状态

## 使用示例

### 基本配置
```kotlin
// 获取统一配置管理器
val appSettings = AppSettingsState.getInstance()

// 获取当前配置
val currentConfig = appSettings.getCurrentConfig()

// 更新配置
val newConfig = createNewConfig()
appSettings.updateConfig(newConfig, project)

// 监听配置变更
appSettings.addConfigChangeListener { newConfig ->
    println("配置已更新: $newConfig")
}
```

### 动态动作注册
```kotlin
// 获取动态注册器
val dynamicRegistry = DynamicActionRegistry.getInstance(project)

// 注册所有动作（自动从AppSettingsState获取配置）
dynamicRegistry.registerAllActions(project, appSettings.getCurrentConfig())

// 获取AI切换注册器
val aiSwitchRegistry = DynamicSwitchAIActionRegistry.getInstance(project)

// 注册AI切换动作
aiSwitchRegistry.registerAllActions(project, appSettings.getCurrentConfig())
```

### 抽象父类使用
```kotlin
// 创建自定义的动态注册器
class CustomActionRegistry : AbstractDynamicActionRegistry<CustomConfig>() {
    override fun getCurrentConfig(): Map<String, CustomConfig>? = currentConfig
    override fun setCurrentConfig(config: Map<String, CustomConfig>) { currentConfig = config }
    override fun isConfigSame(newConfig: Map<String, CustomConfig>): Boolean = /* 实现逻辑 */
    override fun deepCopyConfig(config: Map<String, CustomConfig>): Map<String, CustomConfig> = /* 实现逻辑 */
    override fun registerAction(project: Project, key: String, config: CustomConfig) { /* 实现逻辑 */ }
    override fun getActionIdPrefix(): String = "custom."
}
```


## 错误处理

### 异常捕获
- 完善的try-catch块
- 详细的错误日志
- 用户友好的错误提示

### 恢复机制
- 配置加载失败时使用默认配置
- 动作注册失败时的降级处理

## 下一步计划

根据项目规划，下一步可以考虑：
1. **YAML配置支持**: 实现完整的YAML配置文件加载
2. **配置导入/导出**: 支持配置的备份和恢复
3. **用户界面优化**: 改进配置界面的用户体验
4. **性能监控**: 添加性能指标和监控

## 总结

第三阶段成功实现了基于配置的动态动作注册和快捷键绑定系统，通过架构优化实现了更好的代码复用和逻辑简化，使插件具备了：

### **架构优化成果**
- **代码复用**: 通过抽象父类减少了约 300 行重复代码
- **逻辑统一**: 所有动态注册器使用相同的核心逻辑
- **维护简化**: 通用逻辑的修改只需在父类中进行
- **扩展便利**: 新增注册器变得非常简单

### **功能特性**
- **高度可配置性**: 支持灵活的配置定义
- **动态更新能力**: 配置变更可以实时生效
- **用户友好性**: 清晰的提示和错误处理
- **系统集成性**: 与IntelliJ平台深度集成
- **配置同步**: 所有组件自动同步配置变更

### **技术亮点**
- **抽象继承**: 使用泛型抽象父类实现代码复用
- **统一配置**: 通过 `AppSettingsState` 统一管理所有配置
- **数据类合并**: 使用统一的数据类型避免重复定义
- **自动同步**: 配置变更时自动同步到所有相关组件

这些功能为插件的后续扩展和维护奠定了坚实的基础，使插件能够更好地适应不同用户的需求和系统环境。通过架构优化，代码质量得到了显著提升，为未来的功能扩展提供了良好的基础。
