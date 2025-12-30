package com.digia.digiaui.init

import com.digia.digiaui.config.model.DUIConfig
import com.digia.digiaui.framework.logging.Logger
import com.digia.digiaui.network.NetworkClient

/**
 * Singleton manager for accessing the initialized Digia UI instance.
 *
 * [DigiaUIManager] provides global access to the DigiaUI instance and its core components. This
 * follows the Flutter implementation pattern where DigiaUIManager acts as a central access point
 * for SDK resources.
 *
 * Key responsibilities:
 * - Global access point to DigiaUI instance
 * - Provides access to network client, config, and other core components
 * - Manages SDK lifecycle (initialization and cleanup)
 *
 * Example usage:
 * ```kotlin
 * // Initialize
 * val digiaUI = DigiaUI.initialize(options)
 * DigiaUIManager.initialize(digiaUI)
 *
 * // Access anywhere in the app
 * val accessKey = DigiaUIManager.getInstance().accessKey
 * val networkClient = DigiaUIManager.getInstance().networkClient
 * ```
 */
class DigiaUIManager private constructor() {

    private var _digiaUI: DigiaUI? = null

    /** The current DigiaUI instance, if initialized */
    val safeInstance: DigiaUI?
        get() = _digiaUI

    /** Gets the DigiaUI instance, throwing if not initialized */
    private val instance: DigiaUI
        get() =
                _digiaUI
                        ?: throw IllegalStateException(
                                "DigiaUI not initialized. Call DigiaUIManager.initialize() first."
                        )

    /** The access key used for authentication */
    val accessKey: String
        get() = instance.initConfig.accessKey

    /** The network client for API communications */
    val networkClient: NetworkClient
        get() = instance.networkClient

    /** The application configuration */
    val config: DUIConfig
        get() = instance.dslConfig

    /** Environment variables from the configuration */
    val environmentVariables: Map<String, Any>
        get() = config.getEnvironmentVariables()

    companion object {
        @Volatile private var INSTANCE: DigiaUIManager? = null

        /** Gets the singleton instance of DigiaUIManager */
        fun getInstance(): DigiaUIManager {
            return INSTANCE
                    ?: synchronized(this) { INSTANCE ?: DigiaUIManager().also { INSTANCE = it } }
        }

        /**
         * Initializes the manager with a DigiaUI instance
         *
         * @param digiaUI The initialized DigiaUI instance
         */
        fun initialize(digiaUI: DigiaUI) {
            Logger.log("DigiaUIManager initialized")
            getInstance()._digiaUI = digiaUI
        }

        /** Destroys the manager and cleans up resources */
        fun destroy() {
            Logger.log("DigiaUIManager destroyed")
            getInstance()._digiaUI = null
        }
    }
}
