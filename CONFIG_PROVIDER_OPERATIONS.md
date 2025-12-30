# ConfigProvider Operations - Added Missing Components

## Overview
Added the missing utility operations to `ConfigProvider` interface to match the Flutter SDK pattern.

## Files Created

### 1. FileOperations.kt
**Location**: `/app/src/main/java/com/digia/digiaui/utils/FileOperations.kt`

**Purpose**: Handle file system operations (read, write, exists, delete)

**Interface**:
```kotlin
abstract class FileOperations {
    abstract suspend fun readAsString(path: String): String?
    abstract suspend fun writeAsString(path: String, contents: String)
    abstract suspend fun exists(path: String): Boolean
    abstract suspend fun delete(path: String)
}
```

**Implementation**: `FileOperationsImpl(context: Context)`
- Uses Android's internal storage (`context.filesDir`)
- All operations are suspend functions for coroutine support
- Handles file creation, parent directory creation, and error cases

### 2. DownloadOperations.kt
**Location**: `/app/src/main/java/com/digia/digiaui/utils/DownloadOperations.kt`

**Purpose**: Download files from network to local storage

**Interface**:
```kotlin
abstract class DownloadOperations {
    abstract suspend fun downloadFile(
        url: String,
        fileName: String,
        retry: Int = 0
    ): Response<String>?
}
```

**Implementation**: `DownloadOperationsImpl(context: Context)`
- Downloads files using standard HTTP (java.net.URL)
- Saves to app's internal storage
- Supports retry logic with exponential backoff
- Returns Response object with HTTP status codes and request details
- **Note**: Does not use `withContext(Dispatchers.IO)` - caller must ensure appropriate threading

## Files Modified

### 1. ConfigProvider.kt
**Added Properties**:
```kotlin
/** Gets the asset bundle operations for reading bundled assets */
val bundleOps: AssetBundleOperations

/** Gets the file operations for reading/writing local files */
val fileOps: FileOperations

/** Gets the file downloader for downloading files from network */
val downloadOps: DownloadOperations
```

### 2. ConfigResolver.kt
**Changes**:
- Added `context: Context` parameter to constructor
- Implemented `bundleOps`, `fileOps`, `downloadOps` as lazy properties
- Each property is initialized with its respective implementation class

**Updated Constructor**:
```kotlin
class ConfigResolver(
    private val flavor: Flavor,
    override val networkClient: NetworkClient,
    private val context: Context
) : ConfigProvider
```

**Property Implementations**:
```kotlin
override val bundleOps: AssetBundleOperations by lazy {
    AssetBundleOperationsImpl(context.assets)
}

override val fileOps: FileOperations by lazy {
    FileOperationsImpl(context)
}

override val downloadOps: DownloadOperations by lazy {
    DownloadOperationsImpl(context)
}
```

### 3. DUIConfigProvider.kt
**Changes**:
- Added `context: Context` parameter to constructor
- Implemented `bundleOps`, `fileOps`, `downloadOps` as lazy properties
- Same pattern as ConfigResolver

**Updated Constructor**:
```kotlin
class DUIConfigProvider(
    private val config: DUIConfig,
    override val networkClient: NetworkClient,
    private val context: Context
) : ConfigProvider
```

### 4. DigiaUI.kt
**Changes**:
- Updated ConfigResolver instantiation to pass context

**Before**:
```kotlin
val configResolver = ConfigResolver(
    flavor = options.flavor,
    networkClient = networkClient
)
```

**After**:
```kotlin
val configResolver = ConfigResolver(
    flavor = options.flavor,
    networkClient = networkClient,
    context = options.context
)
```

### 5. DUIFactory.kt
**Changes**:
- Updated DUIConfigProvider instantiation to pass context

**Before**:
```kotlin
configProvider = DUIConfigProvider(config, digiaUIInstance.networkClient)
```

**After**:
```kotlin
configProvider = DUIConfigProvider(
    config,
    digiaUIInstance.networkClient,
    digiaUIInstance.initConfig.context
)
```

## Usage Examples

### Reading Asset Files
```kotlin
val configProvider: ConfigProvider = // ...
val jsonString = configProvider.bundleOps.readString("config.json")
```

### File Operations
```kotlin
// Write to file
configProvider.fileOps.writeAsString("cache/config.json", jsonString)

// Read from file
val cached = configProvider.fileOps.readAsString("cache/config.json")

// Check if exists
if (configProvider.fileOps.exists("cache/config.json")) {
    // File exists
}

// Delete file
configProvider.fileOps.delete("cache/config.json")
```

### Download Files
```kotlin
// Download with retry - caller must use appropriate dispatcher
lifecycleScope.launch(Dispatchers.IO) { // ✅ Use IO dispatcher
    val response = configProvider.downloadOps.downloadFile(
        url = "https://example.com/config.json",
        fileName = "remote_config.json",
        retry = 3
    )

    if (response?.isSuccessful == true) {
        // Download completed successfully
        val filePath = response.data // File path is in data field
        println("Downloaded to: $filePath")
    } else {
        // Handle error
        val statusCode = response?.statusCode
        val statusMessage = response?.statusMessage
        println("Download failed: $statusCode - $statusMessage")
    }
}
```

## Architecture Pattern

This follows the Flutter SDK pattern where `ConfigProvider` acts as a facade providing access to all utility operations needed for configuration loading:

```
ConfigProvider (Interface)
├── getAppConfigFromNetwork() - Network operations
├── initFunctions() - Function initialization
├── addBranchName() - Header management
├── addVersionHeader() - Header management
├── networkClient - HTTP client
├── bundleOps - Asset reading
├── fileOps - File system operations
└── downloadOps - File downloading
```

Each implementation (ConfigResolver, DUIConfigProvider) provides these operations through lazy initialization, ensuring they're only created when needed.

## Benefits

1. **Consistency with Flutter**: Matches the Flutter SDK API exactly
2. **Lazy Initialization**: Operations are only created when accessed
3. **Testability**: Can be mocked for unit tests
4. **Separation of Concerns**: Each operation type has its own class
5. **Coroutine Support**: All file/network operations are suspend functions

## Testing

To verify the implementations work:

1. **Asset Reading**:
   ```kotlin
   val text = configProvider.bundleOps.readString("app_config.json")
   ```

2. **File Writing/Reading**:
   ```kotlin
   configProvider.fileOps.writeAsString("test.txt", "Hello")
   val content = configProvider.fileOps.readAsString("test.txt")
   ```

3. **File Download**:
   ```kotlin
   val response = configProvider.downloadOps.downloadFile(
       url = "https://example.com/file.json",
       fileName = "downloaded.json",
       retry = 2
   )
   
   if (response?.isSuccessful == true) {
       println("Downloaded to: ${response.data}")
   } else {
       println("Download failed: ${response?.statusCode} - ${response?.statusMessage}")
   }
   ```

## Status

✅ All operations implemented
✅ All files updated
✅ No compilation errors
✅ Matches Flutter SDK pattern
✅ Ready for use in config sources

The ConfigProvider interface is now complete with all the utility operations needed for configuration loading from various sources!
