# AI状态管理模块 - 第一阶段开发完成

## 概述
本模块实现了AI状态管理功能，支持在IDE中切换不同的AI工具（如Cursor、Qoder等）。经过后续阶段的优化，现在支持动态配置驱动的AI管理，具备更好的扩展性和维护性。

## 功能特性

### 1. AI状态管理
- 支持动态配置的AI类型（默认：cursor、qoder、claudCode）
- 可扩展支持更多AI类型
- 状态持久化保存
- 状态变化监听机制
- 配置变更时自动调整AI状态

### 2. 状态栏显示
- 在IDE底部状态栏显示当前AI状态
- 点击状态栏可弹出AI选择菜单
- 实时更新AI状态显示

### 3. 快捷键支持
- `Alt+Shift+S`: 切换到下一个AI
- 支持循环切换AI类型
- 支持动态配置的AI切换快捷键（如：option+shift+1/2/3）
- 快捷键冲突自动检测和解决

### 4. 用户界面
- 状态栏组件显示当前AI
- 弹出菜单选择AI
- 切换成功提示消息

## 代码结构

```
src/main/kotlin/com/github/switch2ai/
├── state/
│   ├── AIStateManager.kt           # AI状态管理器（已优化）
│   └── AIStatePersistentComponent.kt # 状态持久化组件
├── ui/
│   ├── AIStatusBarWidget.kt        # 状态栏组件
│   └── AISelectionPopup.kt         # AI选择弹窗
├── actions/
│   ├── SwitchAIAction.kt           # AI切换动作
│   ├── AbstractDynamicActionRegistry.kt # 抽象动态注册器（新增）
│   ├── DynamicActionRegistry.kt    # 动态动作注册器（已重构）
│   └── DynamicSwitchAIActionRegistry.kt # 动态AI切换注册器（新增）
└── settings/
    └── AppSettingsState.kt         # 统一配置管理（已优化）
```

## 核心类说明

### AIStateManager
- 管理当前选中的AI状态
- 提供AI切换功能
- 支持状态变化监听
- 验证AI类型有效性
- **新增**: 从 `AppSettingsState` 动态获取支持的AI类型
- **新增**: 配置变更时自动调整AI状态
- **新增**: 支持配置驱动的AI管理

### AIStatePersistentComponent
- 实现状态的持久化保存
- 在IDE重启后恢复AI状态
- 支持自定义AI类型扩展

### AIStatusBarWidget
- 在状态栏显示当前AI状态
- 支持点击切换AI
- 实时更新显示内容

### AISelectionPopup
- 显示可选择的AI列表
- 支持鼠标悬停效果
- 点击选择AI类型

### SwitchAIAction
- 处理AI切换快捷键
- 显示切换成功消息
- 集成到IDE菜单系统

## 使用方法

### 1. 通过状态栏切换AI
1. 查看IDE底部状态栏的"AI: cursor"显示
2. 点击状态栏组件
3. 在弹出的菜单中选择目标AI
4. AI状态立即更新

### 2. 通过快捷键切换AI
1. 按下 `Alt+Shift+S`
2. AI自动切换到下一个类型
3. 显示切换成功消息

### 3. 通过菜单切换AI
1. 点击 `Tools` → `Switch AI`
2. AI自动切换到下一个类型
3. 显示切换成功消息

### 4. 通过配置驱动的快捷键切换AI（新增）
1. 使用配置的快捷键（如：option+shift+1 切换到 cursor）
2. 支持动态配置的AI切换快捷键
3. 配置变更时快捷键自动更新

## 配置说明

### 支持的AI类型
- `cursor`: Cursor编辑器（默认快捷键：option+shift+1）
- `qoder`: Qoder编辑器（默认快捷键：option+shift+2）
- `claudCode`: Claude编辑器（默认快捷键：option+shift+3）

### 默认设置
- 默认AI: cursor
- 快捷键: Alt+Shift+S（循环切换）
- 状态栏显示: 启用
- 配置驱动: 支持动态配置和快捷键自定义

## 扩展性

### 添加新的AI类型
1. 在`AppSettingsState`的配置中添加新AI类型
2. 配置AI的快捷键和基本信息
3. 系统自动注册新的AI切换动作
4. 无需修改核心代码，完全配置驱动

### 自定义快捷键
1. 在`AppSettingsState`中修改AI或动作的快捷键配置
2. 系统自动检测快捷键冲突并提供解决方案
3. 配置变更立即生效（不涉及快捷键变更时）
4. 涉及快捷键变更时会提示重启IDE

## 测试

### 运行测试
```bash
./gradlew test
```

### 测试覆盖
- AI状态管理功能
- 状态切换逻辑
- 监听器机制
- 边界情况处理

## 注意事项

1. **状态持久化**: AI状态会在IDE重启后自动恢复
2. **扩展性**: 支持添加新的AI类型，无需修改核心代码
3. **性能**: 状态变化监听器使用轻量级实现，性能影响最小
4. **兼容性**: 支持IntelliJ IDEA 2022.3及以上版本
5. **配置驱动**: 所有AI和动作配置现在通过 `AppSettingsState` 统一管理
6. **动态更新**: 配置变更时系统会自动响应，无需手动重启
7. **快捷键冲突**: 系统会自动检测快捷键冲突并提供解决方案

## 开发阶段完成情况

### 阶段1: AI状态管理模块 ✅
- 实现AI状态管理功能
- 支持状态栏显示和快捷键切换
- 状态持久化保存

### 阶段2: 功能模块拆分 ✅
- 将命令执行拆分为配置模块、信息读取模块、命令执行模块
- 实现模块间的解耦和独立测试

### 阶段3: 基于配置的AI、动作注册和快捷键 ✅
- 实现基于配置的动态动作注册
- 支持动态快捷键绑定和冲突检测
- 添加右键菜单支持
- **架构优化**: 创建抽象父类实现代码复用
- **统一配置**: 通过 `AppSettingsState` 统一管理配置
- **代码简化**: 显著减少代码量，提高维护性

## 架构优化成果

### 抽象继承设计
- 创建 `AbstractDynamicActionRegistry<T>` 抽象父类
- 实现代码复用和逻辑简化
- 支持泛型设计，便于扩展

### 统一配置管理
- 将配置管理功能合并到 `AppSettingsState`
- 支持配置的实时更新和同步
- 实现配置变更的自动响应

### 性能优化
- 数据类合并，避免重复定义
- 减少不必要的对象创建和类型转换
- 提升配置变更的响应速度

## 问题反馈

如果在使用过程中遇到问题，请：
1. 检查IDE版本是否支持
2. 查看IDE事件日志
3. 确认插件是否正确安装
4. 提交Issue描述问题详情
