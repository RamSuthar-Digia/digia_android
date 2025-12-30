package com.digia.digiaui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digia.digiaui.app.DigiaUIApp
import com.digia.digiaui.app.DigiaUIAppBuilder
import com.digia.digiaui.app.DigiaUIStatus
import com.digia.digiaui.core.DigiaUIViewModel
import com.digia.digiaui.framework.DUIFactory
import com.digia.digiaui.init.DigiaUI
import com.digia.digiaui.init.DigiaUIOptions
import com.digia.digiaui.init.Environment
import com.digia.digiaui.init.Flavor

/**
 * Main activity demonstrating DigiaUI SDK usage
 *
 * This example shows two approaches:
 * 1. Using DigiaUIAppBuilder - for async initialization with loading states
 * 2. Using DigiaUIApp - for pre-initialized SDK (shown in commented code)
 */


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Option 1: Using DigiaUIAppBuilder (handles initialization internally)
                    DigiaUIAppBuilderExample()

                    // Option 2: Manual initialization (uncomment to use)
                    // ManualInitializationExample()
                }
            }
        }
    }

    /**
     * Example using DigiaUIAppBuilder - recommended for most use cases
     * Handles initialization and loading states automatically
     */
    @Composable
    private fun DigiaUIAppBuilderExample() {
        DigiaUIAppBuilder(
            options = DigiaUIOptions(
                context = applicationContext,
                accessKey = "demo-access-key", // Replace with your actual access key
                environment = Environment.Development,
                flavor = Flavor.Debug(branchName = "main")
            ),
            builder = { status ->
                when (status) {
                    is DigiaUIStatus.Ready -> {
                        // SDK is ready - render the app
                        MainAppContent()
                    }
                    is DigiaUIStatus.Loading -> {
                        // Show loading indicator
                        LoadingScreen("Initializing DigiaUI SDK...")
                    }
                    is DigiaUIStatus.Error -> {
                        // Show error with retry option
                        ErrorScreen(status.message) {
                            // Retry logic would go here
                        }
                    }
                }
            }
        )
    }

    /**
     * Example of manual initialization - gives more control
     */
    @Composable
    private fun ManualInitializationExample() {
        var digiaUI by remember { mutableStateOf<DigiaUI?>(null) }
        var error by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            try {
                val initialized = DigiaUI.initialize(
                    DigiaUIOptions(
                        context = applicationContext,
                        accessKey = "demo-access-key",
                        environment = Environment.Development,
                        flavor = Flavor.Debug(branchName = "main")
                    )
                )
                digiaUI = initialized
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }

        when {
            isLoading -> LoadingScreen("Initializing DigiaUI SDK...")
            error != null -> ErrorScreen(error!!) { /* Retry */ }
            digiaUI != null -> {
                val viewModel = remember {
                    DigiaUIViewModel(
                        context = applicationContext,
                        config = digiaUI!!.config,
                        environment = digiaUI!!.initConfig.environment,
                        analytics = null
                    )
                }
                DigiaUIApp(
                    digiaUI = digiaUI!!,
                    viewModel = viewModel
                ) {
                    MainAppContent()
                }
            }
        }
    }

    /**
     * Main app content - renders pages from DUIFactory
     */
    @Composable
    private fun MainAppContent() {
        // Render the initial page from configuration
        DUIFactory.getInstance().CreateInitialPage()
    }

    /**
     * Loading screen shown during initialization
     */
    @Composable
    private fun LoadingScreen(message: String = "Loading...") {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    /**
     * Error screen shown when initialization fails
     */
    @Composable
    private fun ErrorScreen(message: String, onRetry: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
