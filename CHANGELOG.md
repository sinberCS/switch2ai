# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.1] - 2024-08-30

### Fixed
- 修复了ConfigModel中AI命令格式问题，移除了多余引号
- 修复了Claude Code和iflow命令模板中的语法错误

### Changed
- 版本号从0.1.0更新到0.1.1
- 更新了README中的下载链接和版本信息
- 更新了plugin.xml中的版本记录

### Technical
- 优化了AI命令的变量替换逻辑
- 改进了命令模板的格式一致性

## [0.1.0] - 2024-08-29

### Added
- 🚀 支持多种AI代理（Cursor、Qoder、Claude Code）
- 🤖 实现支持AI选择的提示词输入弹窗
- ⚡ 快捷命令支持（$test、$refactor、$explain等）
- 🔧 动态命令注册和管理系统
- 📝 全面的配置界面，包含三个选项卡
- 🔍 增强的变量支持：$filePath、$projectPath、$line、$column、$selectedText、$prompt

### Features
- 即时AI编辑器切换
- 精确保持光标位置（行号和列号）
- 支持多种AI代理：Cursor、Qoder、Claude Code等
- 支持AI选择的快速提示词输入弹窗
- 支持快捷命令如$test、$refactor、$explain
- 便捷的快捷键（可自定义）
- 多种访问方式：快捷键、右键菜单、工具菜单
- 支持变量的完全可配置自定义命令
- 动态命令注册和管理

### Technical
- 基于IntelliJ Platform的插件架构
- Kotlin语言开发
- 模块化设计，分离关注点
- 配置驱动的行为
- 抽象继承实现代码复用
