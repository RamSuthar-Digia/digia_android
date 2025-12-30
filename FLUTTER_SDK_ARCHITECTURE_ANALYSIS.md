# Flutter Digia UI SDK - Complete Architecture Analysis

This document provides a comprehensive analysis of the Flutter `digia_ui` SDK architecture to guide the Android Compose implementation.

---

## 1. Directory Structure and File Hierarchy

```
lib/
├── digia_ui.dart                          # Main SDK export file
├── dui_settings.dart                      # Settings configuration
│
├── src/
│   ├── init/                              # Initialization System
│   │   ├── digia_ui.dart                  # Core SDK initialization class
│   │   ├── digia_ui_manager.dart          # Singleton manager for SDK instance
│   │   ├── options.dart                   # DigiaUIOptions configuration
│   │   ├── flavor.dart                    # Flavor system (Debug/Staging/Release/Versioned)
│   │   └── environment.dart               # Environment configuration
│   │
│   ├── app/                               # Application Wrappers
│   │   ├── digia_ui_app.dart             # Main app wrapper (StatefulWidget)
│   │   └── digia_ui_app_builder.dart     # Async initialization builder
│   │
│   ├── framework/                         # Core Framework
│   │   ├── ui_factory.dart               # Central factory for pages/components
│   │   ├── virtual_widget_registry.dart  # Widget type registration
│   │   ├── render_payload.dart           # Context for rendering widgets
│   │   ├── message_bus.dart              # Pub/sub messaging system
│   │   ├── font_factory.dart             # Font creation
│   │   ├── resource_provider.dart        # InheritedWidget for resources
│   │   │
│   │   ├── base/                         # Base Classes
│   │   │   ├── virtual_widget.dart       # Abstract base for all widgets
│   │   │   ├── virtual_leaf_stateless_widget.dart
│   │   │   └── virtual_builder_widget.dart
│   │   │
│   │   ├── widgets/                       # Widget Implementations (~50+ widgets)
│   │   │   ├── text.dart
│   │   │   ├── button.dart
│   │   │   ├── container.dart
│   │   │   ├── scaffold.dart
│   │   │   └── ... (column, row, stack, listView, etc.)
│   │   │
│   │   ├── actions/                       # Action System
│   │   │   ├── action_executor.dart      # Executes action flows
│   │   │   ├── action_factory.dart       # Creates actions from JSON
│   │   │   ├── action_processor_factory.dart # Creates action processors
│   │   │   ├── base/
│   │   │   │   ├── action.dart           # Base action class
│   │   │   │   ├── action_flow.dart      # Sequence of actions
│   │   │   │   └── action_processor.dart # Processes individual actions
│   │   │   ├── navigateToPage/
│   │   │   │   ├── action.dart
│   │   │   │   └── processor.dart
│   │   │   ├── setState/
│   │   │   ├── callRestApi/
│   │   │   ├── showDialog/
│   │   │   ├── showBottomSheet/
│   │   │   ├── showToast/
│   │   │   └── ... (~25 action types)
│   │   │
│   │   ├── page/                         # Page System
│   │   │   ├── page.dart                 # DUIPage widget
│   │   │   ├── page_route.dart           # Custom route for pages
│   │   │   ├── page_controller.dart      # Page lifecycle controller
│   │   │   └── config_provider.dart      # Provides page/component configs
│   │   │
│   │   ├── component/
│   │   │   └── component.dart            # DUIComponent widget
│   │   │
│   │   ├── expr/                         # Expression System
│   │   │   ├── scope_context.dart        # Context for expression evaluation
│   │   │   ├── default_scope_context.dart
│   │   │   └── expression_util.dart      # Expression utilities
│   │   │
│   │   ├── state/                        # State Management
│   │   │   ├── state_context.dart        # State container
│   │   │   ├── state_scope_context.dart  # Scope with state
│   │   │   └── stateful_scope_widget.dart # Stateful widget wrapper
│   │   │
│   │   ├── data_type/                    # Data Type System
│   │   │   ├── variable.dart             # Variable definitions
│   │   │   ├── data_type_creator.dart    # Creates typed data
│   │   │   ├── method_bindings/          # Method binding registry
│   │   │   └── adapted_types/            # Type adapters
│   │   │
│   │   ├── models/                       # Data Models
│   │   │   ├── page_definition.dart      # Page structure
│   │   │   ├── component_definition.dart # Component structure
│   │   │   ├── vw_data.dart              # Virtual widget data
│   │   │   ├── common_props.dart         # Common widget properties
│   │   │   ├── props.dart                # Generic properties
│   │   │   └── types.dart                # Type definitions
│   │   │
│   │   └── widget_props/                 # Typed Widget Properties
│   │       ├── text_props.dart
│   │       ├── button_props.dart
│   │       └── ... (one per widget type)
│   │
│   ├── config/                           # Configuration System
│   │   ├── model.dart                    # DUIConfig model
│   │   ├── resolver.dart                 # ConfigResolver
│   │   ├── factory.dart                  # ConfigStrategyFactory
│   │   ├── provider.dart                 # ConfigProvider interface
│   │   ├── app_state/
│   │   │   ├── global_state.dart         # DUIAppState singleton
│   │   │   ├── reactive_value.dart       # Reactive value wrapper
│   │   │   └── app_state_scope_context.dart
│   │   └── source/                       # Config Sources
│   │       ├── base.dart                 # ConfigSource interface
│   │       ├── network.dart              # NetworkConfigSource
│   │       ├── network_file.dart         # NetworkFileConfigSource
│   │       ├── asset.dart                # AssetConfigSource
│   │       ├── cache.dart                # CachedConfigSource
│   │       ├── delegated.dart            # DelegatedConfigSource
│   │       └── fallback.dart             # FallbackConfigSource
│   │
│   ├── network/                          # Networking System
│   │   ├── network_client.dart           # HTTP client
│   │   ├── netwok_config.dart            # Network configuration
│   │   ├── api_request/
│   │   │   └── api_request.dart          # APIModel for REST calls
│   │   └── core/
│   │       └── types.dart                # HTTP types
│   │
│   ├── analytics/
│   │   └── dui_analytics.dart            # Analytics interface
│   │
│   ├── core/
│   │   ├── action/
│   │   │   └── api_handler.dart          # API call handler
│   │   └── functions/
│   │       └── js_functions.dart         # Custom JS functions
│   │
│   ├── components/                       # Special Components
│   │   ├── dui_icons/                    # Icon system
│   │   └── story/                        # Story feature
│   │
│   ├── utils/                            # Utility Functions
│   │   ├── file_operations.dart
│   │   ├── download_operations.dart
│   │   └── asset_bundle_operations.dart
│   │
│   ├── preferences.dart
│   ├── preferences_store.dart            # SharedPreferences wrapper
│   ├── environment.dart
│   ├── dui_dev_config.dart
│   └── version.dart
```

**Total Files**: 357 Dart files

---

## 2. Core Architecture Patterns

### 2.1 Server-Driven UI (SDUI) Pattern

The SDK implements a pure SDUI architecture where:
- **Server sends JSON configurations** describing complete UI structure
- **Client interprets and renders** native Flutter widgets dynamically
- **No hardcoded screens** in the SDK itself

### 2.2 Factory Pattern

**DUIFactory** (Singleton) is the central creation point:
```dart
DUIFactory()
  .createPage(pageId, args)          // Creates pages
  .createComponent(componentId, args) // Creates components
  .registerWidget<T>(...)             // Registers custom widgets
```

### 2.3 Virtual Widget Pattern

All widgets are represented as **VirtualWidget** objects before rendering:
- JSON → VWData (virtual widget data)
- VWData → VirtualWidget (via VirtualWidgetRegistry)
- VirtualWidget → Flutter Widget (via render method)

### 2.4 Expression Evaluation Pattern

Uses **digia_expr** package (external dependency) for:
- Data binding: `@{state.value}`
- Template expressions: `${state.count + 1}`
- Conditional evaluation in properties

### 2.5 Action Flow Pattern

Actions are executed in sequences called **ActionFlow**:
```dart
ActionFlow([
  NavigateToPageAction(...),
  SetStateAction(...),
  CallRestApiAction(...)
])
```

### 2.6 Scope-based Context Pattern

All rendering happens within a **ScopeContext**:
- Variables (page args, state, environment vars)
- Enclosing scopes (chained contexts)
- Expression evaluation happens against scopes

---

## 3. Main Classes and Responsibilities

### 3.1 Initialization Layer

#### **DigiaUI** (Core SDK Class)
```dart
class DigiaUI {
  final DigiaUIOptions initConfig;
  final NetworkClient networkClient;
  final DUIConfig dslConfig;
  
  static Future<DigiaUI> initialize(DigiaUIOptions options) async {
    // 1. Initialize preferences
    await PreferencesStore.instance.initialize();
    
    // 2. Create network client with headers
    final headers = await _createDigiaHeaders(options, '');
    final networkClient = NetworkClient(...);
    
    // 3. Load configuration from server/assets
    final config = await ConfigResolver(options.flavor, networkClient).getConfig();
    
    return DigiaUI._(options, networkClient, config);
  }
}
```

**Responsibilities:**
- SDK initialization entry point
- Network client setup with authentication headers
- Configuration loading orchestration
- Platform detection (iOS/Android/Web)

#### **DigiaUIManager** (Singleton)
```dart
class DigiaUIManager {
  static final _instance = DigiaUIManager._();
  DigiaUI? _digiaUI;
  
  void initialize(DigiaUI digiaUI) { _digiaUI = digiaUI; }
  
  String get accessKey => _digiaUI!.initConfig.accessKey;
  NetworkClient get networkClient => _digiaUI!.networkClient;
  Map<String, Variable> get environmentVariables => ...;
}
```

**Responsibilities:**
- Global access point to DigiaUI instance
- Provides access to network client, config, inspector
- Manages environment variables
- JavaScript function access

#### **DigiaUIOptions**
```dart
class DigiaUIOptions {
  final String accessKey;              // Project authentication key
  final Flavor flavor;                 // Debug/Staging/Release/Versioned
  final NetworkConfiguration? networkConfiguration;
  final DeveloperConfig developerConfig;
}
```

#### **Flavor System**
```dart
sealed class Flavor {
  factory Flavor.debug({String? branchName, Environment environment});
  factory Flavor.staging({Environment environment});
  factory Flavor.release({
    required DSLInitStrategy initStrategy,  // NetworkFirst/CacheFirst/LocalFirst
    required String appConfigPath,
    required String functionsPath,
  });
  factory Flavor.versioned({required int version, Environment environment});
}
```

**DSLInitStrategy Options:**
- `NetworkFirstStrategy` - Fetch from network with timeout, fallback to cache
- `CacheFirstStrategy` - Load cache, update in background
- `LocalFirstStrategy` - Only use bundled assets

### 3.2 Application Layer

#### **DigiaUIApp** (StatefulWidget)
```dart
class DigiaUIApp extends StatefulWidget {
  final DigiaUI digiaUI;                  // Initialized SDK instance
  final DUIAnalytics? analytics;          // Analytics handler
  final MessageBus? messageBus;           // Pub/sub messaging
  final ConfigProvider? pageConfigProvider;
  final Map<String, IconData>? icons;     // Custom icon overrides
  final Map<String, ImageProvider>? images;
  final DUIFontFactory? fontFactory;
  final Map<String, Object?>? environmentVariables;
  final Widget Function(BuildContext) builder;
  
  @override
  void initState() {
    DigiaUIManager().initialize(widget.digiaUI);
    DUIAppState().init(widget.digiaUI.dslConfig.appState ?? []);
    DUIFactory().initialize(...);
  }
  
  @override
  Widget build(BuildContext context) {
    return DigiaUIScope(
      analyticsHandler: analytics,
      messageBus: messageBus,
      child: builder(context),
    );
  }
}
```

**Responsibilities:**
- Wraps the entire application
- Initializes DigiaUIManager, DUIAppState, DUIFactory
- Provides DigiaUIScope (InheritedWidget) for analytics/messaging
- Lifecycle management (dispose)

#### **DigiaUIAppBuilder** (Async Wrapper)
```dart
class DigiaUIAppBuilder extends StatefulWidget {
  final DigiaUIOptions options;
  final Widget Function(BuildContext, DigiaUIStatus) builder;
  
  // Handles 3 states:
  // - DigiaUIStatus.loading()
  // - DigiaUIStatus.ready(digiaUI)
  // - DigiaUIStatus.error(error, stackTrace)
}
```

### 3.3 Factory Layer

#### **DUIFactory** (Singleton)
```dart
class DUIFactory {
  late ConfigProvider configProvider;
  late UIResources resources;
  late VirtualWidgetRegistry widgetRegistry;
  late MethodBindingRegistry bindingRegistry;
  late ActionExecutionContext actionExecutionContext;
  
  void initialize({
    ConfigProvider? pageConfigProvider,
    Map<String, IconData>? icons,
    Map<String, ImageProvider>? images,
    DUIFontFactory? fontFactory,
  }) {
    widgetRegistry = DefaultVirtualWidgetRegistry(
      componentBuilder: (id, args, ctx) => createComponent(id, args, ...),
    );
    bindingRegistry = MethodBindingRegistry();
    actionExecutionContext = ActionExecutionContext(...);
    configProvider = pageConfigProvider ?? DUIConfigProvider(dslConfig);
    resources = UIResources(...);
  }
  
  // Main creation methods
  Widget createPage(String pageId, JsonLike? args, {...overrides});
  Widget createComponent(String componentId, JsonLike? args, {...overrides});
  Route<Object> createPageRoute(String pageId, JsonLike? args);
  Widget createInitialPage({...overrides});
  Future<T?> showBottomSheet<T>(...);
  
  // Widget registration
  void registerWidget<T>(String type, fromJson, builder);
  void registerJsonWidget(String type, builder);
  
  // Environment variables
  void setEnvironmentVariable(String name, Object? value);
  void setEnvironmentVariables(Map<String, Object?> vars);
}
```

**Responsibilities:**
- Central widget creation factory
- Widget registry management
- Resource management (icons, images, fonts, colors)
- Custom widget registration
- Environment variable runtime updates

### 3.4 Widget Registry

#### **VirtualWidgetRegistry**
```dart
abstract class VirtualWidgetRegistry {
  static final Map<String, VirtualWidgetBuilder> _defaultBuilders = {
    'digia/text': textBuilder,
    'digia/button': buttonBuilder,
    'digia/container': containerBuilder,
    'digia/scaffold': scaffoldBuilder,
    'digia/column': columnBuilder,
    'digia/row': rowBuilder,
    'digia/stack': stackBuilder,
    'digia/listView': listViewBuilder,
    // ... 100+ built-in widgets
  };
  
  void registerWidget<T>(String type, fromJsonT, builder);
  VirtualWidget createWidget(VWData data, VirtualWidget? parent);
}
```

**Widget Creation Flow:**
1. JSON → VWData (data model)
2. VWData → VirtualWidget (via builder function)
3. VirtualWidget.render() → Flutter Widget

### 3.5 Page and Component Rendering

#### **DUIPage** (StatelessWidget)
```dart
class DUIPage extends StatelessWidget {
  final String pageId;
  final JsonLike? pageArgs;
  final DUIPageDefinition pageDef;
  final VirtualWidgetRegistry registry;
  final UIResources? resources;
  final ScopeContext? scope;
  
  @override
  Widget build(BuildContext context) {
    // 1. Resolve page arguments
    final resolvePageArgs = pageDef.pageArgDefs?.map(...);
    
    // 2. Resolve initial state
    final resolvedState = pageDef.initStateDefs?.map(...);
    
    // 3. Wrap in ResourceProvider (InheritedWidget)
    return ResourceProvider(
      icons: resources?.icons,
      images: resources?.images,
      textStyles: resources?.textStyles,
      colors: resources?.colors,
      child: StatefulScopeWidget(
        initialState: resolvedState,
        childBuilder: (context, state) {
          return _DUIPageContent(...);
        },
      ),
    );
  }
}
```

**Page Rendering Flow:**
1. Resolve page arguments from definition
2. Initialize page state
3. Create scope context (args + state + environment)
4. Render root widget from layout definition
5. Handle lifecycle events (onPageLoad, onBackPress)

#### **DUIComponent** (StatelessWidget)
Similar to DUIPage but for reusable components:
```dart
class DUIComponent extends StatelessWidget {
  final String id;
  final JsonLike? args;
  final DUIComponentDefinition definition;
  final VirtualWidgetRegistry registry;
  // ... similar structure to DUIPage
}
```

### 3.6 Action System

#### **ActionExecutor**
```dart
class ActionExecutor {
  final ActionExecutionContext actionExecutionContext;
  final Widget Function(BuildContext, String, JsonLike?) viewBuilder;
  final Route Function(BuildContext, String, JsonLike?) pageRouteBuilder;
  final MethodBindingRegistry bindingRegistry;
  
  Future<Object?>? execute(
    BuildContext context,
    ActionFlow actionFlow,
    ScopeContext? scopeContext,
    {String? id, String? parentActionId, ObservabilityContext? observabilityContext}
  ) async {
    // For each action in the flow:
    // 1. Check if disabled
    // 2. Get appropriate processor
    // 3. Execute action
    for (final action in actionFlow.actions) {
      final disabled = action.disableActionIf?.evaluate(scopeContext) ?? false;
      if (disabled) continue;
      
      final processor = ActionProcessorFactory(...).getProcessor(action);
      await processor.execute(context, action, scopeContext, ...);
    }
  }
}
```

#### **Action Types** (25+ built-in actions)
- **navigateToPage** - Navigate to a page
- **navigateBack** - Go back
- **navigateBackUntil** - Pop until route
- **setState** - Update local state
- **set_app_state** - Update global state
- **callRestApi** - Make HTTP requests
- **showDialog** - Display dialog
- **showBottomSheet** - Show bottom sheet
- **showToast** - Show toast message
- **controlDrawer** - Open/close drawer
- **controlNavBar** - Control navigation bar
- **openUrl** - Open external URL
- **share** - Share content
- **copyToClipBoard** - Copy to clipboard
- **filePicker** - Pick files
- **imagePicker** - Pick images
- **upload** - Upload files
- **delay** - Wait for duration
- **postMessage** - Send message via MessageBus
- **execute_callback** - Execute callback
- **event** - Trigger event
- **rebuild_state** - Force rebuild

#### **Action Definition Example**
```dart
class NavigateToPageAction extends Action {
  final ExprOr<JsonLike>? pageData;  // Can be static or expression
  final bool waitForResult;
  final bool shouldRemovePreviousScreensInStack;
  final ExprOr<String>? routeNametoRemoveUntil;
  final ActionFlow? onResult;  // Actions to execute with result
  
  factory NavigateToPageAction.fromJson(Map<String, Object?> json);
}
```

---

## 4. Widget Registration and Rendering

### 4.1 Registration Process

**Built-in Widget Registration** (in VirtualWidgetRegistry):
```dart
static final Map<String, VirtualWidgetBuilder> _defaultBuilders = {
  'digia/text': textBuilder,
  'digia/button': buttonBuilder,
  // ... 100+ widgets
};

// Builder function signature:
VirtualWidget textBuilder(
  VWNodeData data,
  VirtualWidget? parent,
  VirtualWidgetRegistry registry,
) {
  return VWText(
    props: TextProps.fromJson(data.props),
    commonProps: data.commonProps,
    parentProps: data.parentProps,
    parent: parent,
    refName: data.refName,
  );
}
```

**Custom Widget Registration**:
```dart
// Type-safe registration
DUIFactory().registerWidget<CustomProps>(
  'custom/myWidget',
  CustomProps.fromJson,
  (props, childGroups) => VWCustomWidget(props, childGroups),
);

// JSON-based registration
DUIFactory().registerJsonWidget(
  'custom/simpleWidget',
  (props, childGroups) => VWSimpleWidget(props['text']),
);
```

### 4.2 Widget Rendering Flow

```
JSON Configuration
    ↓
VWData.fromJson()  ← Parses JSON into data model
    ↓
VirtualWidgetRegistry.createWidget()  ← Looks up builder
    ↓
widgetBuilder(data, parent, registry)  ← Creates VirtualWidget
    ↓
VirtualWidget.toWidget(RenderPayload)  ← Converts to Flutter Widget
    ↓
VirtualWidget.render(RenderPayload)  ← Renders actual widget
    ↓
Flutter Widget Tree
```

### 4.3 Virtual Widget Base Classes

```dart
abstract class VirtualWidget {
  final String? refName;
  final WeakReference<VirtualWidget>? _parent;
  Props? parentProps;
  
  VirtualWidget? get parent => _parent?.target;
  
  Widget render(RenderPayload payload);  // Abstract method
  Widget toWidget(RenderPayload payload) => render(payload);
}

// For leaf widgets (no children)
abstract class VirtualLeafStatelessWidget<T> extends VirtualWidget {
  final T props;
  final CommonProps? commonProps;
  
  @override
  Widget render(RenderPayload payload);
}

// For widgets with children
abstract class VirtualStatelessWidget<T> extends VirtualWidget {
  final T props;
  final Map<String, List<VirtualWidget>>? childGroups;
  
  @override
  Widget render(RenderPayload payload);
}
```

### 4.4 Example Widget Implementation

```dart
class VWText extends VirtualLeafStatelessWidget<TextProps> {
  VWText({
    required super.props,
    required super.commonProps,
    super.parentProps,
    super.parent,
    super.refName,
  });
  
  @override
  Widget render(RenderPayload payload) {
    // Evaluate expressions
    final text = payload.evalExpr(props.text);
    final maxLines = payload.evalExpr(props.maxLines);
    final alignment = To.textAlign(payload.evalExpr(props.alignment));
    
    // Get styled text style
    final style = payload.getTextStyle(props.textStyle);
    
    // Return Flutter widget
    return Text(
      text.toString(),
      style: style,
      maxLines: maxLines,
      overflow: To.textOverflow(payload.evalExpr(props.overflow)),
      textAlign: alignment,
    );
  }
}
```

### 4.5 RenderPayload

**RenderPayload** provides context for rendering:
```dart
class RenderPayload {
  final BuildContext buildContext;
  final ScopeContext scopeContext;
  final List<String> widgetHierarchy;
  final String? currentEntityId;
  
  // Helper methods:
  Color? getColor(String key);
  APIModel? getApiModel(String id);
  TextStyle? getTextStyle(JsonLike? json);
  Future<Object?>? executeAction(ActionFlow? actionFlow, {...});
  
  // Expression evaluation:
  T? eval<T>(ExprOr<T>? exprOr) {
    if (exprOr == null) return null;
    return exprOr.evaluate(scopeContext);
  }
}
```

---

## 5. Action Registration and Execution

### 5.1 Action Registration (Built-in)

Actions are registered in **ActionProcessorFactory**:
```dart
class ActionProcessorFactory {
  ActionProcessor getProcessor(Action action) {
    return switch (action) {
      NavigateToPageAction() => NavigateToPageProcessor(...),
      SetStateAction() => SetStateProcessor(...),
      CallRestApiAction() => CallRestApiProcessor(...),
      ShowDialogAction() => ShowDialogProcessor(...),
      ShowBottomSheetAction() => ShowBottomSheetProcessor(...),
      ShowToastAction() => ShowToastProcessor(...),
      // ... all action types
    };
  }
}
```

### 5.2 Action Execution Flow

```
User Event (tap, load, etc.)
    ↓
Widget has ActionFlow property
    ↓
executeAction() called on RenderPayload
    ↓
ActionExecutor.execute(context, actionFlow, scopeContext)
    ↓
For each action in ActionFlow:
  1. Check disableActionIf condition
  2. Notify observers (if inspector enabled)
  3. Get ActionProcessor from factory
  4. processor.execute(context, action, scopeContext)
  5. Action completes or fails
    ↓
All actions executed sequentially
```

### 5.3 Action Processor Pattern

```dart
abstract class ActionProcessor<T extends Action> {
  Future<void> execute(
    BuildContext context,
    T action,
    ScopeContext? scopeContext,
    {required String id, String? parentActionId, ObservabilityContext? ctx}
  );
}

// Example: NavigateToPageProcessor
class NavigateToPageProcessor extends ActionProcessor<NavigateToPageAction> {
  @override
  Future<void> execute(...) async {
    // 1. Evaluate expressions
    final pageData = action.pageData?.evaluate(scopeContext);
    final pageId = pageData['pageId'];
    final args = pageData['args'];
    
    // 2. Create route
    final route = _dependencies.pageRouteBuilder(context, pageId, args);
    
    // 3. Navigate
    if (action.waitForResult) {
      final result = await Navigator.push(context, route);
      // Execute onResult actions with result
      if (action.onResult != null) {
        _dependencies.executeActionFlow(..., scopeContext.extend(result));
      }
    } else {
      Navigator.push(context, route);
    }
    
    // 4. Notify completion
    _actionExecutionContext.notifySuccess(...);
  }
}
```

### 5.4 setState Action Example

```dart
class SetStateAction extends Action {
  final String stateContextName;  // 'page', 'component', or custom
  final List<StateUpdate> updates;
  final ExprOr<bool>? rebuild;
}

class StateUpdate {
  final String stateName;
  final ExprOr<Object>? newValue;
}

// Processor:
class SetStateProcessor extends ActionProcessor<SetStateAction> {
  @override
  Future<void> execute(...) async {
    final stateContext = _findStateContext(context, action.stateContextName);
    
    for (final update in action.updates) {
      final newValue = update.newValue?.evaluate(scopeContext);
      stateContext.setState(update.stateName, newValue);
    }
    
    final shouldRebuild = action.rebuild?.evaluate(scopeContext) ?? true;
    if (shouldRebuild) {
      stateContext.rebuild();
    }
  }
}
```

### 5.5 API Call Action

```dart
class CallRestApiAction extends Action {
  final String apiId;  // References API model in config
  final ExprOr<Map<String, Object>>? queryParams;
  final ExprOr<Map<String, Object>>? pathParams;
  final ExprOr<Object>? body;
  final ActionFlow? onSuccess;
  final ActionFlow? onError;
}

// Processor makes HTTP call and executes onSuccess/onError flows
```

---

## 6. Initialization Flow and Configuration Loading

### 6.1 Complete Initialization Flow

```
App Start
    ↓
DigiaUIAppBuilder(options: DigiaUIOptions(...))
    ↓
_initialize() in initState()
    ↓
DigiaUI.initialize(options)
    ├─→ PreferencesStore.instance.initialize()  // SharedPreferences
    ├─→ Create NetworkClient with headers:
    │   ├─ SDK version
    │   ├─ Platform (iOS/Android/Web)
    │   ├─ Package info (name, version, build)
    │   ├─ Access key
    │   └─ Environment name
    ├─→ ConfigResolver(flavor, networkClient).getConfig()
    │   ├─→ ConfigStrategyFactory.createStrategy(flavor, provider)
    │   │   Returns: NetworkConfigSource / AssetConfigSource / etc.
    │   ├─→ strategy.getConfig()  // Returns DUIConfig
    │   └─→ Load JS functions if specified
    └─→ Return DigiaUI instance
    ↓
DigiaUIAppBuilder state updates to ready
    ↓
DigiaUIApp wraps builder result
    ├─→ DigiaUIManager().initialize(digiaUI)
    ├─→ DUIAppState().init(dslConfig.appState)
    └─→ DUIFactory().initialize(...)
        ├─→ widgetRegistry = DefaultVirtualWidgetRegistry(...)
        ├─→ bindingRegistry = MethodBindingRegistry()
        ├─→ actionExecutionContext = ActionExecutionContext(...)
        ├─→ configProvider = DUIConfigProvider(dslConfig)
        └─→ resources = UIResources(icons, images, fonts, colors)
    ↓
DigiaUIScope provides context
    ↓
builder(context) creates app UI
    ↓
DUIFactory().createInitialPage()
```

### 6.2 Configuration Loading Strategies

#### **Debug Flavor** - Network-first from specific branch
```dart
Flavor.debug(branchName: 'feature-xyz')
    ↓
NetworkConfigSource('/config/getAppConfig')
    ↓
POST request with branchName in body
    ↓
Returns latest DSL from server
```

#### **Staging Flavor** - Network from staging environment
```dart
Flavor.staging()
    ↓
NetworkConfigSource('/config/getAppConfigStaging')
```

#### **Versioned Flavor** - Specific version from server
```dart
Flavor.versioned(version: 42)
    ↓
Add version header
    ↓
NetworkConfigSource('/config/getAppConfigForVersion')
```

#### **Release Flavor** - Complex strategy with fallbacks

**NetworkFirstStrategy:**
```
1. Load bundled asset config (appConfigPath)
2. Try to load cached config (appConfig.json)
3. Use whichever is newer (by version number)
4. Attempt network fetch with timeout
5. If network succeeds, use that (and cache it)
6. Otherwise use bundled/cached
```

**CacheFirstStrategy:**
```
1. Load bundled asset config
2. Try to load cached config
3. Use whichever is newer
4. Return immediately (fast startup)
5. Fetch from network in background for next launch
```

**LocalFirstStrategy:**
```
1. Load only from bundled assets
2. No network calls
3. Fastest and most reliable
```

### 6.3 Configuration Model (DUIConfig)

```dart
class DUIConfig {
  final Map<String, dynamic> _themeConfig;  // Colors, fonts
  final Map<String, Object?> pages;         // All page definitions
  final Map<String, Object?>? components;   // All component definitions
  final Map<String, dynamic> restConfig;    // API configurations
  final String initialRoute;                // Starting page ID
  final String? functionsFilePath;          // Custom JS functions
  final List? appState;                     // Global state definitions
  final bool? versionUpdated;
  final int? version;
  final Map<String, dynamic>? _environment; // Environment variables
  JSFunctions? jsFunctions;
  
  // Getters:
  Map<String, Object?> get colorTokens;     // Light theme colors
  Map<String, Object?> get darkColorTokens; // Dark theme colors
  Map<String, Object?> get fontTokens;      // Font definitions
  
  // Methods:
  void setEnvVariable(String varName, Object? value);
  String? getColorValue(String colorToken);
  Map<String, dynamic>? getDefaultHeaders();
  Map<String, Variable> getEnvironmentVariables();
  APIModel getApiDataSource(String id);
}
```

**JSON Structure:**
```json
{
  "theme": {
    "colors": {
      "light": { "primary": "#FF5722", "secondary": "#2196F3" },
      "dark": { "primary": "#FF7043", "secondary": "#42A5F5" }
    },
    "fonts": {
      "heading": { "fontFamily": "Roboto", "fontSize": 24, "fontWeight": "bold" },
      "body": { "fontFamily": "Roboto", "fontSize": 16 }
    }
  },
  "pages": {
    "home_page": { /* page definition */ },
    "profile_page": { /* page definition */ }
  },
  "components": {
    "product_card": { /* component definition */ },
    "user_avatar": { /* component definition */ }
  },
  "rest": {
    "defaultHeaders": { "Content-Type": "application/json" },
    "resources": {
      "userApi": { /* API model */ }
    }
  },
  "appSettings": {
    "initialRoute": "home_page"
  },
  "appState": [
    { "key": "isLoggedIn", "initialValue": false, "shouldPersist": true },
    { "key": "userId", "initialValue": null }
  ],
  "environment": {
    "variables": {
      "baseUrl": { "type": "String", "defaultValue": "https://api.example.com" }
    }
  },
  "version": 1,
  "functionsFilePath": "functions.js"
}
```

---

## 7. Key Interfaces and Their Purposes

### 7.1 ConfigProvider Interface
```dart
abstract class ConfigProvider {
  Future<JsonLike?> getAppConfigFromNetwork(String path);
  Future<void> initFunctions({String? remotePath, String? localPath, int? version});
  void addVersionHeader(int version);
  void addBranchName(String? branchName);
  
  AssetBundleOperations get bundleOps;
  FileOperations get fileOps;
  FileDownloader get downloadOps;
}
```
**Purpose:** Abstract interface for loading configuration from various sources

### 7.2 ConfigSource Interface
```dart
abstract class ConfigSource {
  Future<DUIConfig> getConfig();
}
```
**Purpose:** Strategy pattern for different config loading strategies

**Implementations:**
- **NetworkConfigSource** - Load from HTTP endpoint
- **NetworkFileConfigSource** - Load file from HTTP (with caching)
- **AssetConfigSource** - Load from bundled assets
- **CachedConfigSource** - Load from local file cache
- **DelegatedConfigSource** - Delegates to async function
- **FallbackConfigSource** - Try primary, fallback to secondary

### 7.3 ScopeContext (Expression Context)
```dart
abstract class ScopeContext extends ExprContext {
  ScopeContext copyAndExtend({required Map<String, Object?> newVariables});
}

class DefaultScopeContext extends ScopeContext {
  final Map<String, Object?> variables;
  final ScopeContext? enclosing;  // Parent scope
  
  @override
  Object? resolve(String name) {
    return variables[name] ?? enclosing?.resolve(name);
  }
}
```
**Purpose:** Provides variable resolution for expression evaluation with scope chaining

### 7.4 StateContext
```dart
abstract class StateContext {
  Map<String, Object?> get state;
  void setState(String key, Object? value);
  void rebuild();
  T? getState<T>(String key);
}
```
**Purpose:** Manages component/page-level reactive state

### 7.5 MessageBus
```dart
abstract class MessageBus {
  Stream<T> on<T>();
  void emit<T>(T message);
  void dispose();
}
```
**Purpose:** Pub/sub messaging between components

### 7.6 DUIAnalytics
```dart
abstract class DUIAnalytics {
  void logEvent(String eventName, Map<String, Object?>? parameters);
  void logScreenView(String screenName);
  void setUserId(String? userId);
  void setUserProperty(String name, String? value);
}
```
**Purpose:** Analytics abstraction for tracking events

### 7.7 DUIFontFactory
```dart
abstract class DUIFontFactory {
  TextStyle? createTextStyle(String fontFamily, {
    double? fontSize,
    FontWeight? fontWeight,
    FontStyle? fontStyle,
    Color? color,
  });
}
```
**Purpose:** Custom font loading and text style creation

---

## 8. Data Models for Configuration

### 8.1 Page Definition
```dart
class DUIPageDefinition {
  final String pageId;
  final Map<String, Variable>? pageArgDefs;      // Input parameters
  final Map<String, Variable>? initStateDefs;    // Initial state
  final ({VWData? root})? layout;                // Widget tree root
  final ActionFlow? onPageLoad;                  // Lifecycle action
  final ActionFlow? onBackPress;                 // Back button handler
}
```

**JSON Structure:**
```json
{
  "pageId": "home_page",
  "inputArgs": {
    "userId": { "type": "String", "defaultValue": null }
  },
  "variables": {
    "counter": { "type": "int", "defaultValue": 0 },
    "isLoading": { "type": "bool", "defaultValue": false }
  },
  "layout": {
    "root": {
      "type": "digia/scaffold",
      "props": { /* ... */ },
      "children": [ /* ... */ ]
    }
  },
  "actions": {
    "onPageLoadAction": {
      "actions": [ /* ... */ ]
    },
    "onBackPress": {
      "actions": [ /* ... */ ]
    }
  }
}
```

### 8.2 Component Definition
```dart
class DUIComponentDefinition {
  final String componentId;
  final Map<String, Variable>? argDefs;       // Input parameters
  final Map<String, Variable>? initStateDefs; // Initial state
  final ({VWData? root})? layout;             // Widget tree root
}
```

### 8.3 VWData (Virtual Widget Data)
```dart
sealed class VWData {
  final String? refName;  // For referencing in expressions
}

class VWNodeData extends VWData {
  final String type;                                  // 'digia/text', etc.
  final JsonLike props;                               // Widget properties
  final CommonProps? commonProps;                     // Padding, visibility, etc.
  final Props? parentProps;                           // Parent-specific props
  final Map<String, List<VWData>>? childGroups;      // Named child groups
  final VWRepeatData? repeat;                         // For loops
}

class VWComponentData extends VWData {
  final String id;                                    // Component ID
  final Map<String, ExprOr<Object>?>? args;          // Arguments to pass
  final CommonProps? commonProps;
  final Props? parentProps;
}

class VWStateData extends VWData {
  final Map<String, Variable> initStateDefs;         // Scoped state
  final Map<String, List<VWData>>? childGroups;      // Children
  final Props parentProps;
}
```

### 8.4 Variable (Typed Data Definition)
```dart
class Variable {
  final String name;
  final DataType type;           // String, int, bool, double, List, Map, etc.
  final Object? defaultValue;
  final bool isNullable;
  final String? description;
  
  // For complex types:
  final DataType? itemType;      // List item type
  final DataType? keyType;       // Map key type
  final DataType? valueType;     // Map value type
}

enum DataType {
  string, integer, boolean, double, list, map,
  dateTime, duration, color, icon, image,
  custom  // For user-defined types
}
```

### 8.5 CommonProps (Shared Widget Properties)
```dart
class CommonProps {
  final ExprOr<EdgeInsets>? padding;
  final ExprOr<EdgeInsets>? margin;
  final ExprOr<double>? width;
  final ExprOr<double>? height;
  final ExprOr<Alignment>? alignment;
  final ExprOr<BoxDecoration>? decoration;
  final ExprOr<bool>? visible;
  final ExprOr<double>? opacity;
  final ActionFlow? onTap;
  final ActionFlow? onLongPress;
  final ActionFlow? onDoubleTap;
}
```

### 8.6 ExprOr<T> (Expression or Value)
```dart
sealed class ExprOr<T> {
  T? evaluate(ScopeContext? context);
  
  factory ExprOr.value(T value) = StaticValue<T>;
  factory ExprOr.expr(String expression) = ExprValue<T>;
}

class StaticValue<T> extends ExprOr<T> {
  final T value;
  @override
  T? evaluate(ScopeContext? context) => value;
}

class ExprValue<T> extends ExprOr<T> {
  final String expression;  // e.g., "@{state.counter + 1}"
  
  @override
  T? evaluate(ScopeContext? context) {
    // Parse and evaluate expression using digia_expr package
    return evaluator.eval(expression, context);
  }
}
```

### 8.7 ActionFlow
```dart
class ActionFlow {
  final List<Action> actions;
  
  factory ActionFlow.fromJson(dynamic json) {
    if (json is Map) {
      return ActionFlow([Action.fromJson(json)]);
    } else if (json is List) {
      return ActionFlow(json.map((e) => Action.fromJson(e)).toList());
    }
  }
}

abstract class Action {
  final ExprOr<bool>? disableActionIf;  // Conditional execution
  ActionType get actionType;
  Map<String, dynamic> toJson();
  
  factory Action.fromJson(Map<String, Object?> json) {
    final type = ActionType.fromString(json['type']);
    return switch (type) {
      ActionType.navigateToPage => NavigateToPageAction.fromJson(json),
      ActionType.setState => SetStateAction.fromJson(json),
      // ... all action types
    };
  }
}
```

### 8.8 APIModel (REST Configuration)
```dart
class APIModel {
  final String id;
  final String baseUrl;
  final String path;
  final HttpMethod method;  // GET, POST, PUT, DELETE, PATCH
  final Map<String, String>? headers;
  final Map<String, String>? queryParams;
  final Map<String, String>? pathParams;
  final Object? body;
  final Duration timeout;
  final bool requiresAuth;
  final ResponseType responseType;  // JSON, TEXT, BYTES
}
```

### 8.9 ReactiveValue (Global State)
```dart
class ReactiveValue<T> {
  final StreamController<T> controller;
  T _value;
  
  T get value => _value;
  
  bool update(T newValue) {
    if (_value == newValue) return false;
    _value = newValue;
    controller.add(newValue);
    return true;
  }
  
  Stream<T> get stream => controller.stream;
}

class PersistedReactiveValue<T> extends ReactiveValue<T> {
  final SharedPreferences prefs;
  final String key;
  final String Function(T) serialize;
  final T Function(String) deserialize;
  
  @override
  bool update(T newValue) {
    super.update(newValue);
    prefs.setString(key, serialize(newValue));
    return true;
  }
}
```

---

## 9. Key Implementation Patterns

### 9.1 Expression Binding Pattern
```dart
// In JSON:
{
  "type": "digia/text",
  "props": {
    "text": "@{state.userName}",           // Data binding
    "visible": "@{state.isLoggedIn}",      // Conditional visibility
    "textStyle": {
      "fontSize": "@{state.fontSize + 2}" // Expression in nested object
    }
  }
}

// At runtime:
final text = payload.evalExpr(props.text);  // Evaluates against ScopeContext
```

### 9.2 Scope Chaining Pattern
```dart
// Scope hierarchy:
AppStateScopeContext (global state + stdlib functions)
    ↓
PageScopeContext (page args)
    ↓
StateScopeContext (page state)
    ↓
LocalScopeContext (loop variable, etc.)

// Variable resolution cascades up the chain
```

### 9.3 Resource Override Pattern
```dart
// Global resources
DUIFactory().initialize(icons: {...}, images: {...}, textStyles: {...});

// Page-level overrides
DUIFactory().createPage(
  'checkout',
  args,
  overrideColorTokens: {'primary': Colors.blue},  // Only for this page
);
```

### 9.4 Widget Hierarchy Pattern
```dart
VirtualWidget maintains parent reference:
  Container
    ↓ (parent)
  Column
    ↓ (parent)
  Row
    ↓ (parent)
  Text

// Used for context propagation and observability
```

### 9.5 Observability Pattern
```dart
class ObservabilityContext {
  final List<String> widgetHierarchy;  // ['pageId', 'componentId']
  final String? currentEntityId;        // 'pageId' or 'componentId'
  final String? triggerType;            // 'onTap', 'onLoad', etc.
}

// Passed through rendering and action execution for debugging/inspection
```

---

## 10. Key Differences from Android Implementation

### 10.1 Platform-Specific Patterns

**Flutter:**
- StatefulWidget/StatelessWidget pattern
- InheritedWidget for dependency injection
- BuildContext for tree navigation
- Navigator for routing

**Android Compose:**
- Composable functions
- CompositionLocal for dependency injection
- Composition for tree structure
- NavController for routing

### 10.2 State Management

**Flutter:**
- ChangeNotifier
- StreamController
- setState()

**Android Compose:**
- MutableState
- StateFlow
- remember/rememberSaveable

### 10.3 Async Operations

**Flutter:**
- Future/async-await
- Stream
- FutureBuilder/StreamBuilder

**Android Compose:**
- Coroutines/suspend
- Flow
- LaunchedEffect/rememberCoroutineScope

---

## 11. Summary of Core Concepts

### Initialization:
1. **DigiaUI.initialize()** - Loads config, creates network client
2. **DigiaUIManager** - Global singleton access point
3. **DUIFactory** - Widget creation factory
4. **DUIAppState** - Global reactive state
5. **Flavor system** - Environment-specific config loading

### Rendering:
1. **JSON → VWData** - Parse configuration
2. **VWData → VirtualWidget** - Create virtual representation
3. **VirtualWidget → Flutter Widget** - Render native widgets
4. **RenderPayload** - Provides context (BuildContext, ScopeContext, resources)
5. **Expression evaluation** - Dynamic bindings via digia_expr

### Actions:
1. **ActionFlow** - Sequence of actions
2. **ActionExecutor** - Orchestrates execution
3. **ActionProcessor** - Executes individual action types
4. **25+ built-in actions** - Navigation, state, API, UI, etc.

### State:
1. **Page/Component state** - Local reactive state
2. **Global state** - DUIAppState singleton
3. **Environment variables** - Runtime configuration
4. **ScopeContext** - Chained variable resolution

### Configuration:
1. **DUIConfig** - Complete app configuration model
2. **ConfigSource** - Strategy for loading configs
3. **Multiple strategies** - Network/Cache/Asset/Delegated
4. **Theme, pages, components, APIs, state** - All defined in JSON

---

## 12. Implementation Checklist for Android Compose

- [ ] **Initialization System**: DigiaUI, DigiaUIManager, Options, Flavors
- [ ] **Factory System**: DUIFactory with widget/component creation
- [ ] **Widget Registry**: Registration and lookup of widget builders
- [ ] **Virtual Widget System**: VirtualWidget → Composable conversion
- [ ] **Configuration Loading**: Network/Asset/Cache strategies
- [ ] **Expression Evaluation**: Integrate or create expression parser
- [ ] **Scope System**: ScopeContext with chaining
- [ ] **State Management**: Reactive state at page/component/global levels
- [ ] **Action System**: ActionExecutor, ActionProcessor, 25+ actions
- [ ] **Page Rendering**: DUIPage with lifecycle
- [ ] **Component Rendering**: DUIComponent with nesting
- [ ] **Resource System**: Icons, Images, Fonts, Colors with overrides
- [ ] **Network Client**: HTTP with headers and auth
- [ ] **Theme System**: Color/Font tokens
- [ ] **Navigation**: Page routes and navigation actions
- [ ] **API Integration**: REST client with APIModel
- [ ] **Analytics Integration**: DUIAnalytics interface
- [ ] **Message Bus**: Pub/sub system
- [ ] **Observability**: Inspector integration
- [ ] **Persistence**: State persistence with DataStore
- [ ] **Custom Widgets**: Registration API
- [ ] **Custom Actions**: Registration API

This completes the comprehensive architecture analysis of the Flutter digia_ui SDK!
