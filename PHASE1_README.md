# AI State Management Module - Phase 1 Development Complete

## Overview
This module implements AI state management functionality, supporting switching between different AI tools (such as Cursor, Qoder, etc.) in the IDE. After subsequent phase optimizations, it now supports configuration-driven AI management with better extensibility and maintainability.

## Features

### 1. AI State Management
- Support for dynamically configured AI types (default: cursor, qoder, claudeCode)
- Extensible support for more AI types
- State persistence
- State change listener mechanism
- Automatic AI state adjustment when configuration changes

### 2. Status Bar Display
- Display current AI state in IDE bottom status bar
- Click status bar to pop up AI selection menu
- Real-time AI state display updates

### 3. Shortcut Support
- `Alt+Shift+S`: Switch to next AI
- Support for cycling through AI types
- Support for dynamically configured AI switching shortcuts (e.g., option+shift+1/2/3)
- Automatic shortcut conflict detection and resolution

### 4. User Interface
- Status bar component displaying current AI
- Popup menu for AI selection
- Switch success prompt messages

## Code Structure

```
src/main/kotlin/com/github/switch2ai/
├── state/
│   ├── AIStateManager.kt           # AI State Manager (optimized)
│   └── AIStatePersistentComponent.kt # State Persistence Component
├── ui/
│   ├── AIStatusBarWidget.kt        # Status Bar Component
│   └── AISelectionPopup.kt         # AI Selection Popup
├── actions/
│   ├── SwitchAIAction.kt           # AI Switch Action
│   ├── AbstractDynamicActionRegistry.kt # Abstract Dynamic Registry (new)
│   ├── DynamicActionRegistry.kt    # Dynamic Action Registry (refactored)
│   └── DynamicSwitchAIActionRegistry.kt # Dynamic AI Switch Registry (new)
└── settings/
    └── AppSettingsState.kt         # Unified Configuration Management (optimized)
```

## Core Class Description

### AIStateManager
- Manages currently selected AI state
- Provides AI switching functionality
- Supports state change listening
- Validates AI type validity
- **New**: Dynamically obtains supported AI types from `AppSettingsState`
- **New**: Automatically adjusts AI state when configuration changes
- **New**: Supports configuration-driven AI management

### AIStatePersistentComponent
- Implements state persistence
- Restores AI state after IDE restart
- Supports custom AI type extension

### AIStatusBarWidget
- Displays current AI state in status bar
- Supports click to switch AI
- Real-time display content updates

### AISelectionPopup
- Displays selectable AI list
- Supports mouse hover effects
- Click to select AI type

### SwitchAIAction
- Handles AI switching shortcuts
- Displays switch success messages
- Integrates with IDE menu system

## Usage

### 1. Switch AI via Status Bar
1. View "AI: cursor" display in IDE bottom status bar
2. Click status bar component
3. Select target AI from popup menu
4. AI state updates immediately

### 2. Switch AI via Shortcut
1. Press `Alt+Shift+S`
2. AI automatically switches to next type
3. Display switch success message

### 3. Switch AI via Menu
1. Click `Tools` → `Switch AI`
2. AI automatically switches to next type
3. Display switch success message

### 4. Switch AI via Configuration-Driven Shortcuts (New)
1. Use configured shortcuts (e.g., option+shift+1 to switch to cursor)
2. Support for dynamically configured AI switching shortcuts
3. Shortcuts automatically update when configuration changes

## Configuration

### Supported AI Types
- `cursor`: Cursor editor (default shortcut: option+shift+1)
- `qoder`: Qoder editor (default shortcut: option+shift+2)
- `claudeCode`: Claude editor (default shortcut: option+shift+3)

### Default Settings
- Default AI: cursor
- Shortcut: Alt+Shift+S (cycle switch)
- Status bar display: enabled
- Configuration driven: supports dynamic configuration and shortcut customization

## Extensibility

### Adding New AI Types
1. Add new AI type in `AppSettingsState` configuration
2. Configure AI shortcuts and basic information
3. System automatically registers new AI switch actions
4. No core code modification required, fully configuration-driven

### Custom Shortcuts
1. Modify AI or action shortcut configuration in `AppSettingsState`
2. System automatically detects shortcut conflicts and provides solutions
3. Configuration changes take effect immediately (when shortcuts don't change)
4. IDE restart prompt when shortcuts change

## Testing

### Run Tests
```bash
./gradlew test
```

### Test Coverage
- AI state management functionality
- State switching logic
- Listener mechanism
- Edge case handling

## Notes

1. **State Persistence**: AI state automatically restores after IDE restart
2. **Extensibility**: Supports adding new AI types without core code modification
3. **Performance**: State change listeners use lightweight implementation with minimal performance impact
4. **Compatibility**: Supports IntelliJ IDEA 2022.3 and above
5. **Configuration Driven**: All AI and action configurations now managed through `AppSettingsState`
6. **Dynamic Updates**: System automatically responds to configuration changes without manual restart
7. **Shortcut Conflicts**: System automatically detects shortcut conflicts and provides solutions

## Development Phase Completion Status

### Phase 1: AI State Management Module ✅
- Implement AI state management functionality
- Support status bar display and shortcut switching
- State persistence

### Phase 2: Function Module Separation ✅
- Separate command execution into configuration module, information reading module, command execution module
- Implement module decoupling and independent testing

### Phase 3: Configuration-Based AI, Action Registration and Shortcuts ✅
- Implement configuration-based dynamic action registration
- Support dynamic shortcut binding and conflict detection
- Add right-click menu support
- **Architecture Optimization**: Create abstract parent class for code reuse
- **Unified Configuration**: Unified configuration management through `AppSettingsState`
- **Code Simplification**: Significantly reduce code volume, improve maintainability

## Architecture Optimization Results

### Abstract Inheritance Design
- Create `AbstractDynamicActionRegistry<T>` abstract parent class
- Implement code reuse and logic simplification
- Support generic design for easy extension

### Unified Configuration Management
- Merge configuration management functionality into `AppSettingsState`
- Support real-time configuration updates and synchronization
- Implement automatic response to configuration changes

### Performance Optimization
- Merge data classes to avoid duplicate definitions
- Reduce unnecessary object creation and type conversion
- Improve configuration change response speed

## Issue Reporting

If you encounter problems during use, please:
1. Check if IDE version is supported
2. View IDE event logs
3. Confirm plugin is correctly installed
4. Submit Issue describing problem details
