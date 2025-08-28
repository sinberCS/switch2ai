# 1. 项目目标
我要写一个Intellij idea的插件，它的作用是将idea通过快捷键切换、执行不同AI的命令，其工作流程一是通过快捷键切换AI，二是使用快捷键(使用当前选择的AI)执行对应功能。我需要你给我生成一个给AI的开发计划，注意：不要增加多余的内容。

# 2. 核心项目需求 
## 2.1 核心功能
1. 能够通过yaml配置文件(或者其他对于插件方便的方式)指定AI + 命令，会主要定义下面两个信息
   1. AI信息，默认设置为 cursor、qoder、claudCode
   2. 快捷键对应，每一条信息包含 名称、描述、快捷键 及 每个AI对应的命令
```yaml
aiList:
   cursor: "option+shift+1"
   qoder: "option+shift+2"
   claudCode: "option+shift+3"

actions:
    switch2ai:
        description: "open file in ai with exactly the line and column"
        keyMap: "option+shift+o"
        cursor: "open -a cursor cursor://file${filePath}:${line}:${column}"
        qoder: "open -a qoder qoder://file${filePath}:${line}:${column}"
        claudCode: ""
    generateUnitTest:
        description: "generate unit test (only generate selection part if have)"
        keyMap: "option+shift+t"
        cursor: ""
        claudCode: ""
```
2. 当前使用的AI名字显示在idea下面框中 (和git显示所在分支那显示在一块)，能够通过下面的框选择、切换AI。也能够通过option+shift+1(或者234)切换到指定的ai
3. 通过快捷键，或者右键能够执行配置文件中的命令

## 2.2 代码模块
1. **配置模块**，主要是
   1. 读取配置
   2. 可右键手动更新idea的快捷键

2. **状态管理模块**
   1. 在idea下方显示正在使用的ai模型
   2. 可根据idea下方的ui或者快捷键切换当前的ai

3. **信息读取模块**，在执行命令前的时候获取执行命令所需的内容，目前包括
   1. 当前所处的文件绝对路径 ${filePath}
   2. 当前光标所在的${line}和${column}
   3. 鼠标所选的高亮选块 ${selection}

4. **命令执行模块**
   1. 获取命令：获取当前的AI名字以及执行的命令，根据配置文件找到对应的命令字符串
   2. 命令拼装：根据信息读取模块的信息替换对应的${}内容
   3. 执行命令：执行命令并记录执行的命令以及执行的结果

# 3. 疑问解答 (Q&A)

## Q1: IDEA插件能否支持右键手动更新快捷键？
**A:** 

## Q2: 插件执行结果在哪里查看？
**A:** 插件执行结果可以通过以下方式查看：
- **Event Log窗口**: 显示插件的日志信息，包括命令执行状态
- **IDE日志文件**: 在Help > Diagnostic Tools > Debug Log Settings中配置
- **自定义通知**: 使用Notification API显示成功/失败消息
- **状态栏显示**: 在IDE底部状态栏显示当前AI状态和最后执行结果

## Q3: 如何实现AI状态显示在状态栏？
**A:** 可以通过以下方式实现：
- 使用`StatusBarWidget`接口创建自定义状态栏组件
- 在状态栏中显示当前选中的AI名称
- 点击状态栏组件可以弹出AI选择菜单
- 支持快捷键切换AI状态

# 4. 开发步骤

## 阶段1: AI状态管理模块

### 具体内容
1. **实现AI状态管理**
   - 创建AI状态管理类
   - 添加状态内存持有
   - 目前仅考虑Cusor、Qoder两种状态
   - 需考虑可拓展性、相关配置需独立

2. **idea状态栏显示**
   - 实现状态栏组件
   - 显示当前AI状态
   - 添加AI选择菜单

3. **实现快捷键切换AI**
   - 注册AI切换快捷键
   - 实现快捷键处理逻辑
   - 添加切换确认和反馈

---

## 阶段2: 功能不变，拆分模块 ✅
### 具体内容
1. 暂不考虑AI状态管理模块，固定为default
2. 将当前的命令执行，拆分为配置模块、信息读取模块以及命令执行模块

### 已完成的模块拆分
1. **配置模块 (ConfigurationManager)**
   - 管理AI配置信息 (AIConfig)
   - 管理动作配置信息 (ActionConfig)
   - 提供默认配置和配置加载功能
   - 支持获取指定AI和动作的命令

2. **信息读取模块 (InformationReader)**
   - 从ActionEvent中读取命令执行上下文
   - 获取文件路径、光标位置、选中文本等信息
   - 支持变量替换功能
   - 验证命令上下文的有效性

3. **命令执行模块 (CommandExecutor)**
   - 执行系统命令
   - 解析命令字符串为数组
   - 记录执行结果和错误信息
   - 提供UI反馈（成功/失败消息）

4. **命令处理器 (CommandProcessor)**
   - 整合上述三个模块
   - 提供统一的命令处理接口
   - 支持获取可用动作和AI列表
   - 验证动作和AI组合的有效性

### 重构的动作类
- `ExecuteCustomCommandAction`: 使用新的模块化架构

### 模块化优势
- **职责分离**: 每个模块负责特定的功能
- **可维护性**: 代码结构清晰，易于理解和修改
- **可扩展性**: 新增功能只需修改相应模块
- **可测试性**: 各模块可以独立测试
- **代码复用**: 模块间可以灵活组合使用


---

## 阶段3: 基于配置的AI、动作注册和快捷键 ✅
### 目标
实现插件的基于配置的，AI、动作注册和快捷键绑定

### 具体步骤
1. **基于配置的动作定义** ✅
   - 以可持久化的方式进行AI、命令的分别定义
   - 用户可在setting中进行修改配置，应用后会持久化(现有的配置为默认值)
   
2. **启动时，基于配置更新** ✅
   - plugin启动时候，读取最新配置
   - 根据最新配置，更新快捷键定义 并 处理快捷键冲突(这里需要备注说明如何处理)
   - 根据最新配置，在编辑器右键菜单中添加动作
   
3. **更新配置化操作处理** ✅
   - 客户修改，如果不涉及快捷键修改，相关command改动即可生效
   - 客户修改，如果涉及快捷键修改，弹框提示需要重启ide

### 架构优化成果 ✅
- **抽象父类设计**: 创建 `AbstractDynamicActionRegistry<T>` 抽象父类，实现代码复用
- **代码简化**: `DynamicActionRegistry` 从 380 行减少到约 200 行
- **代码简化**: `DynamicSwitchAIActionRegistry` 从 236 行减少到约 100 行
- **统一配置管理**: 将 `ConfigurationManager` 功能合并到 `AppSettingsState` 中
- **数据类合并**: 使用统一的 `AIConfigData` 和 `ActionConfigData` 类型

### 验证点 ✅
- 动作能正确显示在菜单中
- 快捷键能正常触发动作
- 右键菜单功能正常
- 配置变更自动同步到所有组件
- 代码复用和逻辑简化效果显著
