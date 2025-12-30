package com.digia.digiaui.config.source

import com.digia.digiaui.config.ConfigException
import com.digia.digiaui.config.ConfigProvider
import com.digia.digiaui.config.source.ConfigSource
import com.digia.digiaui.config.model.DUIConfig
import com.digia.digiaui.framework.logging.Logger
import com.google.gson.Gson
import java.io.File

/**
 * ConfigSource that loads configuration from cached file
 *
 * This reads previously downloaded and cached configuration from the app's cache directory. Used in
 * Release builds to provide fast startup with potentially newer config than burned assets.
 *
 * Cache files are stored in: context.cacheDir/config/
 *
 * @param provider The ConfigProvider (used for context)
 * @param fileName The name of the cached config file
 */
class CachedConfigSource(
        private val provider: ConfigProvider,
        private val fileName: String = "appConfig.json"
) : ConfigSource {

    override suspend fun getConfig(): DUIConfig {
        try {
            Logger.log("Loading config from cache: $fileName")

            val context =
                    provider.networkClient.context
                            ?: throw ConfigException.cacheError(
                                    "Context not available for cache access"
                            )

            // Get cache directory
            val cacheDir = File(context.cacheDir, "config")
            val cacheFile = File(cacheDir, fileName)

            if (!cacheFile.exists()) {
                throw ConfigException.cacheError("Cache file not found: $fileName")
            }

            // Read cached file
            val jsonString = cacheFile.readText()

            // Parse the JSON
            val appConfig =
                    Gson().fromJson(jsonString, DUIConfig::class.java)
                            ?: throw ConfigException.invalidData("Failed to parse cached config")

            Logger.log("Successfully loaded config from cache (version: ${appConfig.version})")
            return appConfig
        } catch (e: ConfigException) {
            throw e
        } catch (e: Exception) {
            throw ConfigException.cacheError("Failed to load config from cache: ${e.message}", e)
        }
    }
}
