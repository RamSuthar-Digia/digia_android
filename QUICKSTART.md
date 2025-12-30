# Digia UI - Quick Start Guide

## Prerequisites

- Android Studio (latest version)
- Android SDK 24 or higher
- Kotlin 2.0.21 or higher
- Gradle 8.13

## 📦 Installation

Add Digia UI SDK to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.digia:digia-ui-compose:latest_version")
}
```

## 🚀 Quick Start

### 1. Initialize DigiaUI

First, you need to initialize the DigiaUI SDK with your project configuration.

#### Using DigiaUIApp (Simple)

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            // Initialize DigiaUI
            val digiaUI = DigiaUI.initialize(
                DigiaUIOptions(
                    accessKey = "YOUR_PROJECT_ACCESS_KEY", // Get from Digia Studio
                    flavor = Flavor.Release(),
                    strategy = NetworkFirstStrategy(timeoutInMs = 2000)
                )
            )

            setContent {
                DigiaUIApp(
                    digiaUI = digiaUI
                ) {
                    // Your app content here
                    DUIFactory().createInitialPage()
                }
            }
        }
    }
}
```

#### Using DigiaUIAppBuilder (Advanced)

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DigiaUIAppBuilder(
                options = DigiaUIOptions(
                    accessKey = "YOUR_PROJECT_ACCESS_KEY",
                    flavor = Flavor.Release(),
                    strategy = NetworkFirstStrategy(timeoutInMs = 2000)
                )
            ) { status ->
                when (status) {
                    is InitializationStatus.Ready -> {
                        // SDK ready - show main app
                        MainScreen()
                    }
                    is InitializationStatus.Loading -> {
                        // Show loading
                        LoadingScreen()
                    }
                    is InitializationStatus.Error -> {
                        // Show error with retry
                        ErrorScreen(status)
                    }
                }
            }
        }
    }
}
```

### 2. Create Your First Page

In Digia Studio, create a page with the ID "home". Then display it:

```kotlin
@Composable
fun MainScreen() {
    // This will render the "home" page from Digia Studio
    DUIFactory().createInitialPage()
}
```

### 3. Navigate Between Pages

```kotlin
@Composable
fun ProductList(products: List<Product>) {
    LazyColumn {
        items(products) { product ->
            Button(onClick = {
                // Navigate to product details page
                // This assumes you have a "product_details" page in Digia Studio
                navController.navigate("product_details") {
                    putString("productId", product.id)
                }
            }) {
                DUIFactory().createComponent(
                    "product_card",
                    mapOf(
                        "title" to product.title,
                        "price" to product.price,
                        "imageUrl" to product.imageUrl
                    )
                )
            }
        }
    }
}
```

### 4. State Management

#### Global State

```kotlin
// Set global state from native code
DUIAppState.setValue("userName", "John Doe")
DUIAppState.setValue("cartCount", 5)

// Listen to state changes
LaunchedEffect(Unit) {
    DUIAppState.listen("cartCount").collect { count ->
        // Update UI when cart count changes
        updateCartBadge(count)
    }
}
```

#### Analytics

```kotlin
// Implement DUIAnalytics
class MyAnalytics : DUIAnalytics {
    override fun trackEvent(eventName: String, properties: Map<String, Any?>) {
        // Send to your analytics provider
        firebaseAnalytics.logEvent(eventName, properties.toBundle())
    }

    override fun trackScreen(screenName: String, properties: Map<String, Any?>) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    // ... other methods
}

// Use in initialization
DigiaUI.initialize(
    DigiaUIOptions(
        accessKey = "YOUR_ACCESS_KEY",
        flavor = Flavor.Release(),
        strategy = NetworkFirstStrategy()
    )
)
// Note: Analytics integration will be added in future versions
```

### 5. Custom Components

```kotlin
// Register custom component
DUIFactory().registerWidget(
    "custom_map",
    { json -> MapProps.fromJson(json) },
    { props ->
        val mapProps = props as MapProps
        GoogleMap(
            initialCameraPosition = CameraPosition(
                target = LatLng(mapProps.latitude, mapProps.longitude),
                zoom = mapProps.zoom
            )
        )
    }
)
```

## 📱 Complete Example

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DigiaUIAppBuilder(
                options = DigiaUIOptions(
                    accessKey = "your_access_key_here",
                    flavor = Flavor.Release(),
                    strategy = NetworkFirstStrategy(timeoutInMs = 2000)
                )
            ) { status ->
                when (status) {
                    is InitializationStatus.Ready -> {
                        AppNavigation()
                    }
                    is InitializationStatus.Loading -> {
                        LoadingScreen()
                    }
                    is InitializationStatus.Error -> {
                        ErrorScreen(status)
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            DUIFactory().createInitialPage()
        }
        composable("product_details/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            DUIFactory().createPage("product_details", mapOf("productId" to productId))
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Loading latest content...")
    }
}

@Composable
fun ErrorScreen(status: InitializationStatus.Error) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Failed to load content", style = MaterialTheme.typography.h5)
        Text("Error: ${status.error}", modifier = Modifier.padding(vertical = 16.dp))

        if (status.hasCache) {
            Button(onClick = { status.useCachedVersion() }) {
                Text("Use Offline Version")
            }
        }
    }
}
```

## 🔧 Configuration

### DigiaUIOptions

- `accessKey`: Your Digia Studio project access key
- `flavor`: `Flavor.Debug()` or `Flavor.Release()`
- `strategy`: `NetworkFirstStrategy()` or `CacheFirstStrategy()`

### Initialization Strategies

- **NetworkFirstStrategy**: Always fetch latest content first, fallback to cache
- **CacheFirstStrategy**: Use cached content for instant startup, update in background

## 📚 Next Steps

1. Create your first project in [Digia Studio](https://app.digia.tech/)
2. Design pages and components visually
3. Use the SDK to render them in your Android app
4. Implement custom components for advanced functionality
5. Set up analytics and state management

## 🆘 Support

- 📚 [Documentation](https://docs.digia.tech/)
- 💬 [Community](https://discord.gg/szgbr63a)
- 🐛 [Issue Tracker](https://github.com/Digia-Technology-Private-Limited/digia_ui/issues)
- 📧 [Contact Support](mailto:admin@digia.tech)
      "route": "home",
      "layout": {
        "type": "Column",
        "children": [
          {
            "type": "Text",
            "properties": {
              "text": "Hello Digia UI!"
            }
          }
        ]
      }
    }
  },
  "rest": {
    "defaultHeaders": {
      "Content-Type": "application/json"
    }
  }
}
```

### 4. Initialize in MainActivity

Digia UI Compose offers two initialization patterns:

#### Option A: Simple Initialization with DigiaUIApp

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val digiaUIManager = DigiaUIManager.getInstance(
            context = this,
            environment = Environment.debug(
                apiBaseUrl = "https://your-api.com"
            ),
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

#### Option B: Advanced Initialization with DigiaUIAppBuilder

For more control over loading and error states:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DigiaUIAppBuilder(
                configSource = AssetConfigSource(...),
                environment = Environment.debug(),
                analytics = ExampleAnalytics()
            ) { status ->
                when (status) {
                    is InitializationStatus.Loading -> LoadingScreen()
                    is InitializationStatus.Ready -> DigiaUIContent(status.viewModel)
                    is InitializationStatus.Error -> ErrorScreen(status.message, status.hasCache)
                }
            }
        }
    }
}
```

#### Option C: Direct DigiaUI Usage

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Option 1: Use from network
        val digiaUIManager = DigiaUIManager.getInstance(
            context = this,
            environment = Environment.debug(
                apiBaseUrl = "https://your-api.com"
            ),
            flavor = Flavor.Debug(branchName = "main")
        )

        // Option 2: Use from assets
        // See example in AssetConfigSource usage below

        setContent {
            DigiaUI(
                configSource = digiaUIManager.configSource,
                environment = digiaUIManager.environment
            )
        }
    }
}
```

## Configuration Strategies

### 1. Network Configuration (Debug/Staging)

```kotlin
val flavor = Flavor.Debug(
    branchName = "feature-branch",
    environment = Environment.DEVELOPMENT
)
```

### 2. Asset Configuration (Production)

```kotlin
val resolver = AppConfigResolver(
    flavor = Flavor.Release(
        initStrategy = CacheFirstStrategy(),
        appConfigPath = "config/app_config.json",
        functionsPath = "functions/app_functions.js"
    ),
    networkClient = networkClient,
    context = context
)

val configSource = AssetConfigSource(
    provider = resolver,
    appConfigPath = "app_config_example.json",
    functionsPath = "functions.js"
)
```

### 3. Cached Configuration

```kotlin
val configSource = CachedConfigSource(
    provider = resolver,
    cachedFilePath = "appConfig.json"
)
```

### 4. Fallback Configuration

```kotlin
val configSource = FallbackConfigSource(
    primary = NetworkAppConfigSource(resolver, "/config/getAppConfig"),
    fallback = listOf(
        CachedConfigSource(resolver, "appConfig.json"),
        AssetConfigSource(resolver, "app_config_example.json", "functions.js")
    )
)
```

## Custom Components

### Register Custom Component

```kotlin
@Composable
fun MyCustomWidget(props: Map<String, Any?>) {
    val title = props["title"] as? String ?: ""
    val count = props["count"] as? Int ?: 0
    
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text("Count: $count")
        }
    }
}

// Register in Application or MainActivity onCreate
val componentRegistry = ComponentRegistry.getInstance()
componentRegistry.register("MyCustomWidget", object : ComponentFactory {
    @Composable
    override fun create(props: Map<String, Any?>) {
        MyCustomWidget(props)
    }
})
```

### Use in Configuration

```json
{
  "type": "MyCustomWidget",
  "properties": {
    "title": "Hello",
    "count": 42
  }
}
```

## State Management

### Global State

```kotlin
// In ViewModel or composable
viewModel.stateManager.setState("user", userData)
val user = viewModel.stateManager.getState<User>("user")
```

### Persistent State

```kotlin
// Save
viewModel.preferencesStore.save("theme", "dark")

// Read
val theme = viewModel.preferencesStore.getString("theme", "light")

// Observe
viewModel.preferencesStore.observeKey("theme", "light")
    .collect { theme ->
        // Handle theme change
    }
```

## API Calls

### Using ApiClient

```kotlin
val apiClient = viewModel.apiClient

// GET request
val response = apiClient?.get(
    endpoint = "/api/users",
    queryParams = mapOf("page" to "1")
)

// POST request
val response = apiClient?.post(
    endpoint = "/api/users",
    body = mapOf("name" to "John", "email" to "john@example.com")
)

// Handle response
when (response) {
    is ApiResponse.Success -> {
        val data = response.data
        // Process data
    }
    is ApiResponse.Error -> {
        val error = response.message
        // Handle error
    }
}
```

### Using Data Sources

```kotlin
val dataSource = viewModel.dataSourceManager?.getDataSource("userApi")
val endpoint = dataSource?.getEndpoint("getUsers")
```

## Analytics

```kotlin
// Track screen view
viewModel.analyticsHandler.trackScreenView("home")

// Track custom event
viewModel.analyticsHandler.trackEvent(
    AnalyticsEvent.Custom(
        name = "user_action",
        params = mapOf(
            "action" to "button_click",
            "screen" to "home"
        )
    )
)

// Add custom analytics provider
class MyAnalyticsProvider : AnalyticsProvider {
    override fun logEvent(event: AnalyticsEvent) {
        // Send to your analytics service
    }
    
    override fun setUserProperty(property: String, value: String) {
        // Set user property
    }
    
    override fun setUserId(userId: String?) {
        // Set user ID
    }
    
    override fun reset() {
        // Reset analytics
    }
}

viewModel.analyticsHandler.addProvider(MyAnalyticsProvider())
```

## Validation

```kotlin
val validationManager = viewModel.validationManager

val result = validationManager.validate(
    value = "john@example.com",
    rules = listOf(
        RequiredRule(),
        EmailRule()
    )
)

if (!result.isValid) {
    // Show errors
    result.errors.forEach { error ->
        println(error)
    }
}
```

## Navigation

```kotlin
// In your composable
val navigationManager = rememberNavigationManager()

// Navigate to page
navigationManager.navigateTo("details", mapOf("id" to "123"))

// Navigate and replace
navigationManager.navigateToWithReplace("home")

// Go back
navigationManager.pop()

// Get current route
val currentRoute = navigationManager.getCurrentRoute()
```

## Logging

```kotlin
val logger = Logger.getLogger("MyTag")

logger.debug("Debug message")
logger.info("Info message")
logger.warn("Warning message")
logger.error("Error message", throwable)

// Configure logging
logger.setEnabled(true)
logger.setMinLevel(LogLevel.DEBUG)
```

## Message Bus

```kotlin
// Publish message
viewModel.messageBus.publishSync(
    StandardMessage(
        type = "user_login",
        payload = userData
    )
)

// Subscribe to messages
LaunchedEffect(Unit) {
    viewModel.messageBus.events.collect { message ->
        when (message.type) {
            "user_login" -> {
                // Handle login
            }
        }
    }
}
```

## Expression Evaluation

```kotlin
val evaluator = viewModel.expressionEvaluator

// Evaluate expression
val result = evaluator.evaluate(
    expression = "\${user.name}",
    context = mapOf("user" to mapOf("name" to "John"))
)

// Interpolate template
val text = evaluator.interpolate(
    template = "Hello \${user.name}, you have \${count} messages",
    context = mapOf(
        "user" to mapOf("name" to "John"),
        "count" to 5
    )
)
```

## Testing

### Unit Test Example

```kotlin
@Test
fun testStateManager() {
    val stateManager = StateManager()
    stateManager.setState("key", "value")
    assertEquals("value", stateManager.getState<String>("key"))
}
```

### Integration Test Example

```kotlin
@Test
fun testConfigLoading() = runTest {
    val configSource = AssetConfigSource(provider, "test_config.json", "")
    val config = configSource.getConfig()
    assertNotNull(config)
    assertEquals("home", config.initialRoute)
}
```

## Troubleshooting

### Configuration Not Loading

1. Check network connectivity
2. Verify API endpoint
3. Check logs for errors
4. Try fallback configuration

### Navigation Not Working

1. Verify route exists in config
2. Check initialRoute in appSettings
3. Ensure NavHost is properly configured

### State Not Persisting

1. Check DataStore permissions
2. Verify context is application context
3. Check for serialization errors

### Theme Not Applying

1. Verify color tokens in config
2. Check color format (hex strings)
3. Ensure DigiaTheme wraps content

## Production Checklist

- [ ] Replace debug API URLs with production URLs
- [ ] Use Release flavor
- [ ] Enable ProGuard/R8
- [ ] Disable debug logging
- [ ] Configure analytics providers
- [ ] Test all pages and flows
- [ ] Handle error states
- [ ] Add loading states
- [ ] Test offline functionality
- [ ] Optimize configuration size
- [ ] Enable caching
- [ ] Set up monitoring

## Resources

- See `README.md` for full documentation
- See `IMPLEMENTATION_SUMMARY.md` for architecture details
- See `app/src/main/assets/app_config_example.json` for config example

## Support

For issues or questions:
1. Check the documentation
2. Review example configurations
3. Check logs for error messages
4. Verify all dependencies are installed
