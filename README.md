# Digia UI - Android Compose

Digia UI SDK is the Android Compose-based rendering engine for [Digia Studio](https://app.digia.tech/), a low-code mobile application platform. Built on the Server-Driven UI (SDUI) architecture, this SDK dynamically renders native Jetpack Compose widgets based on configurations received from the server, enabling real-time UI updates without requiring app releases or store approvals.

## 📁 File Hierarchy & Structure

```
com/digia/digiaui/
├── 📄 Core SDK Files
│   ├── DigiaUI.kt                    # Main SDK class with initialize() method
│   ├── DigiaUIApp.kt                 # App wrappers (DigiaUIApp, DigiaUIAppBuilder)
│   ├── DigiaUIOptions.kt             # Configuration options (accessKey, flavor, strategy)
│   ├── Flavor.kt                     # Debug/Release flavor enum
│   ├── InitializationStrategy.kt     # NetworkFirstStrategy, CacheFirstStrategy
│   ├── InitializationStatus.kt       # Ready/Loading/Error status for builder
│   └── DigiaUIInitializationViewModel.kt # ViewModel for initialization status
│
├── 🏭 Factory & Rendering
│   ├── DUIFactory.kt                 # Factory for creating pages/components from config
│   ├── actions/ActionSystem.kt       # Action system (showToast, etc.)
│   └── widgets/WidgetSystem.kt       # Widget system (Text, Scaffold, etc.)
│
├── 📊 State Management
│   ├── DUIAppState.kt                # Global state management with streams
│   └── state/PersistentStateManager.kt # Persistent state using DataStore
│
├── 📡 Networking
│   └── network/DigiaApiClient.kt     # HTTP client for Digia Studio API
│
├── 🎨 Theming
│   └── theme/DigiaTheme.kt           # Material 3 theme integration
│
├── ⚙️ Configuration System
│   ├── config/AppConfig.kt           # Configuration models (AppConfig, PageDefinition, etc.)
│   └── config/source/ConfigSources.kt # Config sources (Network, Asset, Cache, Fallback)
│
├── 🔧 Core Systems
│   └── core/DigiaRenderer.kt         # Core rendering engine
│
├── 📊 Analytics
│   └── DUIAnalytics.kt               # Analytics interface
│
├── 🛠️ Utilities
│   └── utils/DigiaUtils.kt           # Logging, JSON parsing, device detection
│
└── 📱 Example Usage
    └── MainActivity.kt               # Complete example implementation
```

## 🚀 Overview

### What is Server-Driven UI?

Server-Driven UI (SDUI) is an architectural pattern where the server controls the presentation layer of your application by sending UI configurations that the client interprets and renders. This approach offers several key advantages:

• 🚀 **Instant Updates** - Deploy UI changes immediately without app store review cycles
• 🧪 **A/B Testing** - Run experiments and personalize experiences without client-side release cycles
• 🔧 **Bug Fixes** - Fix UI issues in production without releasing a new app version
• 📱 **Cross-Platform Consistency** - Ensure uniform experiences across Android, iOS, and Mobile Web from a single configuration

### The Digia Ecosystem

Digia UI SDK is part of the Digia Studio ecosystem, where:

1. **Digia Studio** - A visual low-code tool where users drag and drop widgets to create mobile applications
2. **Server Configurations** - The studio generates structured configurations describing UI layout, data bindings, and business logic
3. **Digia UI SDK** - This Android Compose SDK interprets the server configurations to render fully functional native mobile apps across Android platforms

### Key Features

• 🎨 **Server-Driven UI** - Render Jetpack Compose widgets from server-side configurations
• 🔄 **Instant Updates** - Push UI and logic changes instantly without app store approvals
• 🔗 **Expression Binding** - Powerful data binding system for dynamic content
• 🎯 **Pre-built Actions** - Navigation, state management, API calls, and more
• 📱 **Native Performance** - Rendering handled directly by Jetpack Compose for optimal performance
• 🧩 **Custom Widgets** - Register your own widgets to extend functionality
• 🌐 **Android Native** - Single codebase for Android using Jetpack Compose

## 📦 Installation

Add Digia UI SDK to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.digia:digia-ui-compose:latest_version")
}
```

## 🏁 Getting Started

### Initialization of DigiaUI SDK

DigiaUI SDK offers two initialization strategies to suit different application needs:

#### NetworkFirst Strategy

• Prioritizes fresh content - Always fetches the latest DSL configuration from the network first
• Fast performance - DSL is hosted on CDN with average load times under 100ms for large projects
• Recommended for production - Ensures users always see the most up-to-date UI
• Best for - Apps where having the latest content is critical
• Timeout fallback - Optionally set a timeout; if exceeded, falls back to cache or burned DSL config

#### CacheFirst Strategy

• Instant startup - Uses cached DSL configuration for immediate rendering
• Fallback to network - Fetches updates in the background for next session
• Offline capable - Works even without network connectivity
• Best for - Apps prioritizing fast startup times or offline functionality

### Implementation Options

DigiaUI SDK offers two implementation options for different needs.

#### Option 1: Using DigiaUIApp

Use this approach when DigiaUI needs to be initialized before rendering the first frame.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize DigiaUI synchronously
        lifecycleScope.launch {
            val digiaUI = DigiaUI.initialize(
                DigiaUIOptions(
                    accessKey = 'YOUR_PROJECT_ACCESS_KEY',
                    flavor = Flavor.Release(),
                    strategy = NetworkFirstStrategy(timeoutInMs = 2000)
                )
            )

            setContent {
                DigiaUIApp(
                    digiaUI = digiaUI,
                    builder = { context ->
                        MaterialApp(
                            home = DUIFactory().createInitialPage()
                        )
                    }
                )
            }
        }
    }
}
```

#### Option 2: Using DigiaUIAppBuilder

For advanced use cases where you need more granular control over the initialization process. You can choose whether or not to wait for DigiaUI to be ready. This is especially useful when your app starts with a few native Flutter pages before transitioning to DigiaUI-powered screens.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DigiaUIAppBuilder(
                options = DigiaUIOptions(
                    accessKey = 'YOUR_PROJECT_ACCESS_KEY',
                    flavor = Flavor.Release(),
                    strategy = NetworkFirstStrategy(timeoutInMs = 2000)
                ),
                builder = { status ->
                    when (status) {
                        is InitializationStatus.Ready -> {
                            // SDK is ready, show main app
                            MaterialApp(
                                home = DUIFactory().createInitialPage()
                            )
                        }
                        is InitializationStatus.Loading -> {
                            // Show loading indicator
                            LoadingScreen()
                        }
                        is InitializationStatus.Error -> {
                            // Show error UI with fallback
                            ErrorScreen(status)
                        }
                    }
                }
            )
        }
    }
}
```

## 🛠️ Usage Patterns

Digia UI SDK supports two integration patterns:

### 1. Full App Mode

Build your whole application in Digia Studio and use the SDK to render it.

```kotlin
MaterialApp(
    home = DUIFactory().createInitialPage(),
    onGenerateRoute = { settings ->
        DUIFactory().createPageRoute(
            settings.name!!,
            settings.arguments as Map<String, dynamic>?
        )
    }
)
```

### 2. Hybrid Mode

Migrate pages incrementally by mixing native Compose screens with Digia UI pages:

```kotlin
// Navigate to a Digia UI page from native Compose
navController.navigate("checkout_page") {
    // Pass arguments to the Digia UI page
    putString("cartId", cartId)
    putString("userId", userId)
}
```

## 📄 Creating Pages

Pages are complete, full-screen UI definitions that include lifecycle hooks and built-in state management.

```kotlin
// Create a page with arguments
val checkoutPage = DUIFactory().createPage(
    'checkout_page',
    mapOf(
        'cartId' to '12345',
        'totalAmount' to 99.99
    )
)

// Navigate to a page
navController.navigate("product_details") {
    putString("productId", product.id)
}
```

## 🧩 Creating Components

Components are modular UI elements that you can reuse throughout your app.

```kotlin
@Composable
fun ProductListPage(products: List<Product>) {
    LazyColumn {
        items(products) { product ->
            DUIFactory().createComponent(
                'product_list_item',
                mapOf(
                    'id' to product.id,
                    'title' to product.title,
                    'price' to product.price,
                    'imageUrl' to product.imageUrl,
                    'rating' to product.rating,
                    'isOnSale' to product.isOnSale,
                    'discount' to product.discount,
                    'onTap' to { /* navigate to product */ },
                    'onAddToCart' to { /* add to cart */ }
                )
            )
        }
    }
}
```

## 🗂️ State Management

Digia UI provides a comprehensive state management system with four levels:

### 1. Global State (App State)

Shared across the entire app and can optionally persist between sessions.

```kotlin
// In your native code
class CartManager {
    fun addToCart(product: Product) {
        cart.add(product)

        // Sync with Digia UI
        DUIAppState.setValue('cartCount', cart.size)
        DUIAppState.setValue('cartTotal', cart.totalAmount)
        DUIAppState.setValue('cartItems', cart.items.map { it.toJson() })
    }
}

// Listen to state changes
DUIAppState.listen('cartCount').collect { count ->
    // Update native UI
}
```

### 2. Page State

Scoped to individual pages and cleared when page is disposed.

### 3. Component State

Local state for reusable components.

### 4. Local State

Widget-level state for UI interactions within a specific widget tree.

## 🎨 Custom Widget Registration

Extend Digia UI with your own custom widgets:

```kotlin
// Define your custom widget
@Composable
fun CustomMapWidget(props: MapProps) {
    GoogleMap(
        initialCameraPosition = CameraPosition(
            target = LatLng(props.latitude, props.longitude),
            zoom = props.zoom
        )
    )
}

// Register during initialization
DUIFactory().registerWidget(
    'custom/map',
    { json -> MapProps.fromJson(json) },
    { props -> CustomMapWidget(props as MapProps) }
)
```

## ## Latest Version

Digia UI SDK is under active development, and we release updates regularly. To take advantage of the latest features and improvements, it's important to keep your project updated.

## Quick Start

### 1. Add Dependency

Choose one of the following methods:

#### A. From Maven Local (for development/testing)

```bash
# In this repo, publish to local Maven
./gradlew :digia-ui:publishToMavenLocal
```

In your app's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()  // Add this
        google()
        mavenCentral()
    }
}
```

#### B. From GitHub Packages (recommended for teams)

See [PUBLISHING.md](PUBLISHING.md) for GitHub Packages setup.

#### C. Composite Build (active development)

```kotlin
// In your app's settings.gradle.kts
includeBuild("/path/to/digia_ui_compose")
```

### 2. Sync Gradle

After adding the dependency, click "Sync Now" in Android Studio or run:

```bash
./gradlew --refresh-dependencies
```

### 3. Initialize SDK

Follow the [Getting Started](#-getting-started) guide to initialize the SDK in your application.

## Staying Updated

To ensure your project always uses the latest version of Digia UI SDK, follow these guidelines:

### Auto-sync Latest Version

#### Option 1: Using Gradle Version Catalogs (Recommended)

1. Create/update `gradle/libs.versions.toml`:

```toml
[versions]
digiaui = "1.0.0"

[libraries]
digiaui = { module = "com.digia:digia-ui", version.ref = "digiaui" }
```

2. Use in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.digiaui)
}
```

3. To update, just change the version in `libs.versions.toml` and sync!

#### Option 2: Using Composite Build (Development)

Always gets the latest code automatically:

```kotlin
// settings.gradle.kts
includeBuild("/path/to/digia_ui_compose")
```

No version management needed - Gradle syncs changes automatically!

#### Option 3: Dependabot (Automated PRs)

Add `.github/dependabot.yml` to your project:

```yaml
version: 2
updates:
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "daily"
```

Dependabot will create PRs when new versions are available.

### Check for Updates

To manually check for updates, you can use the following command:

```bash
./gradlew dependencyUpdates
```

Or use Android Studio: `Tools → Gradle → Refresh Gradle Dependencies`

## Documentation

For detailed documentation, API references, and guides, visit the [Digia UI Documentation](https://docs.digia.tech/).

## Contributing

We welcome contributions! Please read our [Contributing Guide](CONTRIBUTING.md) for details on how to get involved.

## Support

For support, please contact our [support team](mailto:support@digia.tech) or visit our [community forums](https://community.digia.tech/).

## License

This project is licensed under the Business Source License 1.1 (BSL 1.1) - see the [LICENSE](LICENSE) file for details.
- **ComposeStateManager**: Reactive state for Compose

#### 7. **Theme System** (`theme/`)
- **ThemeProvider**: Creates Material3 theme from config
- **DigiaTheme**: Composable theme wrapper

#### 8. **API & Networking** (`api/`, `network/`)
- **ApiClient**: REST API client with support for GET, POST, PUT, DELETE
- **NetworkClient**: Low-level HTTP client
- **DataSourceManager**: Manages API data sources from config
- **ApiDataSource**: Represents an API data source

#### 9. **Event & Action System** (`event/`)
- **EventHandler**: Event handling and listener management
- **ActionExecutor**: Executes actions from config (navigate, setState, apiCall, etc.)
- **Event**: Event data structure
- **ActionContext**: Context for action execution

#### 10. **Analytics System** (`analytics/`)
- **AnalyticsHandler**: Multi-provider analytics
- **AnalyticsProvider**: Interface for analytics providers
- **AnalyticsEvent**: Predefined event types

#### 11. **Message Bus** (`message/`)
- **MessageBus**: Pub/sub messaging system using Kotlin Flow
- **Message**: Message interface
- **Command**: Command interface

#### 12. **Expression Evaluation** (`expression/`)
- **ExpressionEvaluator**: Evaluates expressions and bindings from config
- Supports: `${}` template expressions, `@{}` bindings, arithmetic, comparisons

#### 13. **Validation System** (`validation/`)
- **ValidationManager**: Field validation
- **ValidationRule**: Validation rule interface
- Built-in rules: Required, Email, MinLength, MaxLength, Pattern, Range, Custom

#### 14. **Logging System** (`logging/`)
- **Logger**: Android Log wrapper with levels
- **LogLevel**: DEBUG, INFO, WARN, ERROR

#### 15. **Resource Provider** (`resource/`)
- **ResourceProvider**: Access to Android resources (strings, colors, drawables)

#### 16. **Utilities** (`utils/`)
- **FileOperations**: File read/write operations
- **DownloadOperations**: File download with coroutines
- **AssetBundleOperations**: Asset loading

#### 17. **Core System** (`core/`)
- **DigiaUI**: Main composable entry point
- **DigiaUIViewModel**: Core ViewModel managing entire app lifecycle

## Usage

### App Initialization Patterns

Digia UI Compose provides two main initialization patterns, equivalent to Flutter's `DigiaUIApp` and `DigiaUIAppBuilder`:

#### Option 1: DigiaUIApp (Simple Initialization)

Use this when DigiaUI needs to be initialized before rendering the first frame:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val digiaUIManager = DigiaUIManager.getInstance(
            context = this,
            environment = Environment.debug(),
            flavor = Flavor.Debug(branchName = "main"),
            analytics = ExampleAnalytics()
        )

        setContent {
            DigiaUIApp(digiaUIManager = digiaUIManager) {
                DigiaUI(
                    configSource = digiaUIManager.configSource,
                    environment = digiaUIManager.environment,
                    analytics = digiaUIManager.analytics
                )
            }
        }
    }
}
```

#### Option 2: DigiaUIAppBuilder (Advanced Control)

For granular control over initialization with custom loading/error states:

```kotlin
DigiaUIAppBuilder(
    configSource = configSource,
    environment = Environment.debug(),
    analytics = ExampleAnalytics()
) { status ->
    when (status) {
        is InitializationStatus.Loading -> {
            // Show loading screen
            LoadingScreen()
        }
        is InitializationStatus.Ready -> {
            // DigiaUI is ready
            DigiaUIContent(status.viewModel)
        }
        is InitializationStatus.Error -> {
            // Show error screen
            ErrorScreen(status.message) { /* retry */ }
        }
    }
}
```

### Basic Setup

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val digiaUIManager = DigiaUIManager.getInstance(
            context = this,
            environment = Environment.debug(
                apiBaseUrl = "https://dev-api.digiastudio.com"
            ),
            flavor = Flavor.Debug(
                branchName = "main"
            )
        )

        setContent {
            DigiaUI(
                configSource = digiaUIManager.configSource,
                environment = digiaUIManager.environment
            )
        }
    }
}
```

### Configuration Example

```json
{
  "appSettings": {
    "initialRoute": "home"
  },
  "theme": {
    "colors": {
      "light": {
        "primary": "#6200EE",
        "onPrimary": "#FFFFFF",
        "background": "#FFFFFF",
        "surface": "#FFFFFF"
      },
      "dark": {
        "primary": "#BB86FC",
        "onPrimary": "#000000",
        "background": "#121212",
        "surface": "#121212"
      }
    }
  },
  "pages": {
    "home": {
      "title": "Home",
      "route": "home",
      "appBar": {
        "title": "Welcome",
        "showBackButton": false
      },
      "layout": {
        "type": "Column",
        "children": [
          {
            "type": "Text",
            "properties": {
              "text": "Welcome to Digia UI!"
            }
          },
          {
            "type": "Button",
            "properties": {
              "text": "Get Started"
            },
            "events": {
              "onClick": [
                {
                  "type": "navigate",
                  "route": "details"
                }
              ]
            }
          }
        ]
      }
    }
  },
  "rest": {
    "defaultHeaders": {
      "Content-Type": "application/json"
    },
    "resources": {
      "userApi": {
        "baseUrl": "https://api.example.com",
        "endpoints": {
          "getUser": {
            "path": "/users/{id}",
            "method": "GET"
          }
        }
      }
    }
  }
}
```

### Custom Components

Register custom components:

```kotlin
val componentRegistry = ComponentRegistry.getInstance()

componentRegistry.register("CustomWidget") { props ->
    // Your composable implementation
    CustomWidget(
        title = props["title"] as? String ?: "",
        value = props["value"] as? Int ?: 0
    )
}
```

### State Management

```kotlin
// Global state
viewModel.stateManager.setState("user", userData)
val user = viewModel.stateManager.getState<User>("user")

// Persistent state
viewModel.preferencesStore.save("theme", "dark")
val theme = viewModel.preferencesStore.getString("theme")
```

### API Calls

```kotlin
val apiClient = viewModel.apiClient
val response = apiClient?.get("/api/users")

when (response) {
    is ApiResponse.Success -> {
        val data = response.data
        // Handle success
    }
    is ApiResponse.Error -> {
        val error = response.message
        // Handle error
    }
}
```

### Analytics

```kotlin
viewModel.analyticsHandler.trackEvent(
    AnalyticsEvent.Custom(
        name = "user_action",
        params = mapOf("action" to "button_click")
    )
)
```

## Features

✅ **Server-Driven UI**: Build entire apps from JSON configuration  
✅ **Dynamic Theming**: Material3 themes from configuration  
✅ **Navigation**: Multi-page apps with routing  
✅ **State Management**: Global and persistent state  
✅ **API Integration**: REST API client with data source management  
✅ **Event System**: Event handling and action execution  
✅ **Analytics**: Multi-provider analytics support  
✅ **Validation**: Form validation with built-in rules  
✅ **Expression Evaluation**: Dynamic bindings and expressions  
✅ **Component Registry**: Custom component support  
✅ **Message Bus**: Pub/sub messaging  
✅ **Logging**: Structured logging system  
✅ **Multiple Flavors**: Debug, Staging, Release, Versioned  
✅ **Caching**: Config and resource caching  

## Not Implemented (As Requested)

❌ Widget rendering (beyond basic placeholders)  
❌ Action handlers implementation  
❌ Data type methods  

## Dependencies

```gradle
// Navigation
implementation("androidx.navigation:navigation-compose:2.7.6")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// JSON
implementation("com.google.code.gson:gson:2.10.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Preferences DataStore
implementation("androidx.datastore:datastore-preferences:1.0.0")

// ViewModel & LiveData
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// Image Loading
implementation("io.coil-kt:coil-compose:2.5.0")
```

## Project Structure

```
com.digia.digiaui/
├── analytics/          # Analytics system
├── api/                # API client and data sources
├── component/          # Component registry
├── config/             # Configuration system
│   └── source/         # Config source implementations
├── core/               # Core UI and ViewModel
├── event/              # Event and action handling
├── expression/         # Expression evaluator
├── init/               # Initialization and managers
├── logging/            # Logging system
├── message/            # Message bus
├── navigation/         # Navigation system
├── network/            # Network client
├── page/               # Page management and rendering
├── resource/           # Resource provider
├── state/              # State management
├── theme/              # Theme provider
├── ui/theme/           # UI theme definitions
├── utils/              # Utility classes
└── validation/         # Validation system
```

## License

This is a demonstration implementation for the Digia UI system.
