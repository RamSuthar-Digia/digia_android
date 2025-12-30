package com.digia.digiaui.config.source

import com.digia.digiaui.config.ConfigProvider
import com.digia.digiaui.config.source.ConfigSource
import com.digia.digiaui.config.model.DUIConfig

/**
 * ConfigSource that loads configuration from network API
 *
 * This is the primary source for Debug, Staging, and Versioned flavors. Makes a POST request to the
 * specified endpoint to fetch the app config.
 *
 * @param provider The ConfigProvider to handle network requests
 * @param endpoint The API endpoint path (e.g., "/config/getAppConfig")
 */
class NetworkConfigSource(private val provider: ConfigProvider, private val endpoint: String) :
        ConfigSource {

    override suspend fun getConfig(): DUIConfig {
        return provider.getAppConfigFromNetwork(endpoint)
    }
}
