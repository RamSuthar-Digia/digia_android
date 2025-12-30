package com.digia.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digia.digiaui.config.ConfigResolver
import com.digia.digiaui.init.Flavor
import com.digia.digiaui.network.NetworkClient
import com.digia.digiaui.network.NetworkConfiguration
import com.digia.digiaui.utils.DeveloperConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val status = remember { mutableStateOf("Idle") }

                    // Update these two values for your environment
                    val baseUrl = "https://app.digia.tech/api/v1"
                    val accessKey = "CHANGE_ME"

                    fun createClient(): NetworkClient {
                        val digiaHeaders = NetworkClient.getDefaultDigiaHeaders(
                            packageVersion = "0.0.1",
                            accessKey = accessKey,
                            platform = "Android",
                            uuid = null,
                            packageName = packageName,
                            appVersion = "1.0",
                            appBuildNumber = "1",
                            environment = "production",
                            buildSignature = ""
                        )

                        val projectNetworkConfig = NetworkConfiguration.withDefaults()

                        return NetworkClient(
                            baseUrl = baseUrl,
                            digiaHeaders = digiaHeaders,
                            projectNetworkConfiguration = projectNetworkConfig,
                            developerConfig = DeveloperConfig()
                        )
                    }

                    suspend fun fetchConfig() {
                        status.value = "Fetching..."
                        try {
                            val client = createClient()
                            val resolver = ConfigResolver(
                                _flavorInfo = Flavor.DASHBOARD,
                                _networkClient = client,
                                context = this@MainActivity
                            )
                            // Example endpoint path - update to match your backend
                            val json = resolver.getAppConfigFromNetwork("/config/getDUIConfig")
                            status.value = "OK: keys=${json?.keys?.joinToString()}"
                        } catch (t: Throwable) {
                            status.value = "FAIL: ${t.message}"
                        }
                    }

                    Column(Modifier.padding(16.dp)) {
                        Text("Digia UI Template")
                        Text("Status: ${status.value}")
                        Button(onClick = {
                            // Launch in composition scope
                            androidx.compose.runtime.LaunchedEffect(Unit) {}
                        }) {
                            Text("Tap: fetch config (see status)")
                        }
                    }

                    // Auto-run once
                    LaunchedEffect(Unit) {
                        fetchConfig()
                    }
                }
            }
        }
    }
}

