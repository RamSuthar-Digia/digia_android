package com.digia.digiaui.config.source

import com.digia.digiaui.config.ConfigException
import com.digia.digiaui.config.ConfigProvider
import com.digia.digiaui.config.source.ConfigSource
import com.digia.digiaui.config.model.DUIConfig
import com.digia.digiaui.framework.logging.Logger
import com.google.gson.Gson
import java.io.File
import kotlinx.coroutines.withTimeout

/**
 * ConfigSource that loads configuration from network with timeout and caching
 *
 * This is used in Release builds to fetch the latest config from the network with a timeout
 * constraint. If successful, it also caches the result for future use by CachedConfigSource.
 *
 * Differences from NetworkDUIConfigSource:
 * - Has configurable timeout
 * - Automatically caches successful responses
 * - Used primarily in release builds
 *
 * @param provider The ConfigProvider to handle network requests
 * @param endpoint The API endpoint path
 * @param timeout Timeout in milliseconds (default 5 seconds)
 */
class NetworkFileConfigSource(
        private val provider: ConfigProvider,
        private val endpoint: String,
        private val timeout: Long = 5000L
) : ConfigSource {

    override suspend fun getConfig(): DUIConfig {
        try {
            Logger.log("Fetching config from network (timeout: ${timeout}ms)")

            // Fetch with timeout
            val appConfig = withTimeout(timeout) { provider.getDUIConfigFromNetwork(endpoint) }

            // Cache the result
            cacheConfig(appConfig)

            Logger.log("Successfully fetched and cached config from network")
            return appConfig
        } catch (e: Exception) {
            throw ConfigException.networkError(
                    "Failed to fetch config from network: ${e.message}",
                    e
            )
        }
    }

    /** Caches the config to disk for later use */
    private fun cacheConfig(config: DUIConfig) {
        try {
            val context = provider.networkClient.context ?: return

            // Create cache directory
            val cacheDir = File(context.cacheDir, "config")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            // Write to cache file
            val cacheFile = File(cacheDir, "appConfig.json")
            val jsonString = Gson().toJson(config)
            cacheFile.writeText(jsonString)

            Logger.log("Config cached successfully")
        } catch (e: Exception) {
            Logger.log("Failed to cache config: ${e.message}")
            // Don't throw - caching is optional
        }
    }
}
