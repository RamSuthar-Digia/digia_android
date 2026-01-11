package com.digia.digiaui.framework.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.apiModel
import com.digia.digiaui.framework.base.VirtualCompositeNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.expr.DefaultScopeContext
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.init.DigiaUIManager
import com.digia.digiaui.network.APIModel
import com.digia.digiaui.network.BodyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Future builder widget properties */
data class FutureBuilderProps(
    val initialData: Any? = null,
    val controller: Any? = null, // Expression for async controller
    val future: JsonLike? = null, // Contains futureType and dataSource
    val onSuccess: JsonLike? = null, // Action flow to execute on success
    val onError: JsonLike? = null // Action flow to execute on error
) {
    companion object {
        fun fromJson(json: JsonLike): FutureBuilderProps {
            return FutureBuilderProps(
                initialData = json["initialData"],
                controller = json["controller"],
                future = json["future"] as? JsonLike,
                onSuccess = json["onSuccess"] as? JsonLike,
                onError = json["onError"] as? JsonLike
            )
        }
    }
}

/** Future state enum */
private enum class FutureState {
    LOADING,
    COMPLETED,
    ERROR
}

/**
 * Virtual FutureBuilder widget
 *
 * Handles asynchronous operations (primarily API calls) and manages loading, error, and completed states.
 * Provides futureState and response data to child widgets through scoped context.
 */
class VWFutureBuilder(
    refName: String? = null,
    commonProps: CommonProps? = null,
    props: FutureBuilderProps,
    parent: VirtualNode? = null,
    slots: Map<String, List<VirtualNode>>? = null,
    parentProps: Props? = null
) :
    VirtualCompositeNode<FutureBuilderProps>(
        props = props,
        commonProps = commonProps,
        parentProps = parentProps,
        parent = parent,
        refName = refName,
        slots = slots
    ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Return empty if no child template
        if (child == null) {
            Empty()
            return
        }

        // Get API model configuration (must be called from Composable)
        val futureConfig = props.future
        val dataSource = futureConfig?.get("dataSource") as? JsonLike
        val apiModelId = dataSource?.get("id") as? String
        val apiModel = apiModelId?.let { payload.apiModel(it) }

        // State management for async operation
        var futureState by remember { mutableStateOf(FutureState.LOADING) }
        var responseData by remember { mutableStateOf<Any?>(props.initialData) }
        var errorData by remember { mutableStateOf<Any?>(null) }

        // Execute async operation
        LaunchedEffect(Unit) {
            try {
                // Validate future configuration
                if (futureConfig == null) {
                    futureState = FutureState.ERROR
                    errorData = mapOf("message" to "No future configuration provided")
                    payload.executeAction(props.onError)
                    return@LaunchedEffect
                }

                val futureType = futureConfig["futureType"] as? String

                when (futureType) {
                    "api" -> {
                        // Handle API call
                        val result = executeApiCall(payload, apiModel, dataSource)
                        if (result.isSuccess) {
                            futureState = FutureState.COMPLETED
                            responseData = result.data
                            payload.executeAction(props.onSuccess, "onSuccess")
                        } else {
                            futureState = FutureState.ERROR
                            errorData = result.error
                            payload.executeAction(props.onError, "onError")
                        }
                    }
                    else -> {
                        futureState = FutureState.ERROR
                        errorData = mapOf("message" to "Unsupported future type: $futureType")
                        payload.executeAction(props.onError, "onError")
                    }
                }
            } catch (e: Exception) {
                futureState = FutureState.ERROR
                errorData = mapOf(
                    "message" to (e.message ?: "Unknown error"),
                    "exception" to e.toString()
                )
                payload.executeAction(props.onError, "onError")
            }
        }

        // Create scoped context with state information
        val scopedPayload = payload.copyWithChainedContext(
            createExprContext(futureState, responseData, errorData)
        )

        // Render child with scoped context
        child?.ToWidget(scopedPayload)
    }

    /**
     * Execute API call using NetworkClient
     */
    private suspend fun executeApiCall(
        payload: RenderPayload,
        apiModel: APIModel?,
        dataSource: JsonLike?
    ): ApiResult {
        return withContext(Dispatchers.IO) {
            try {
                if (dataSource == null) {
                    return@withContext ApiResult(
                        isSuccess = false,
                        data = null,
                        error = mapOf("message" to "No dataSource provided")
                    )
                }

                // Validate API model
                if (apiModel == null) {
                    val apiModelId = dataSource["id"] as? String
                    return@withContext ApiResult(
                        isSuccess = false,
                        data = null,
                        error = mapOf("message" to "API model not found: $apiModelId")
                    )
                }

                // Get NetworkClient from DigiaUIManager
                val networkClient = DigiaUIManager.getInstance().networkClient

                // Evaluate variables in the API model
                val evaluatedUrl = evaluateVariables(apiModel.url, payload, apiModel)
                val evaluatedHeaders = apiModel.headers?.mapValues { (_, value) ->
                    evaluateVariables(value.toString(), payload, apiModel)
                }
                val evaluatedBody = apiModel.body?.mapValues { (_, value) ->
                    when (value) {
                        is String -> evaluateVariables(value, payload, apiModel)
                        else -> value
                    }
                }

                // Execute the network request
                val response = networkClient.requestProject(
                    bodyType = apiModel.bodyType ?: BodyType.JSON,
                    url = evaluatedUrl,
                    method = apiModel.method,
                    additionalHeaders = evaluatedHeaders,
                    data = evaluatedBody,
                    apiName = apiModel.name
                )

                // Parse response
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val data = if (!responseBody.isNullOrEmpty()) {
                        try {
                            com.google.gson.Gson().fromJson(
                                responseBody,
                                Map::class.java
                            ) as? Map<String, Any>
                        } catch (e: Exception) {
                            mapOf("raw" to responseBody)
                        }
                    } else {
                        emptyMap<String, Any>()
                    }

                    ApiResult(
                        isSuccess = true,
                        data = data,
                        error = null
                    )
                } else {
                    ApiResult(
                        isSuccess = false,
                        data = null,
                        error = mapOf(
                            "message" to "HTTP ${response.code}: ${response.message}",
                            "statusCode" to response.code,
                            "body" to (response.body?.string() ?: "")
                        )
                    )
                }
            } catch (e: Exception) {
                ApiResult(
                    isSuccess = false,
                    data = null,
                    error = mapOf(
                        "message" to (e.message ?: "Unknown error"),
                        "exception" to e.toString()
                    )
                )
            }
        }
    }

    /**
     * Evaluate variables in a string (replace {variableName} with actual values)
     */
    private fun evaluateVariables(
        template: String,
        payload: RenderPayload,
        apiModel: APIModel
    ): String {
        var result = template
        val pattern = "\\{([^}]+)\\}".toRegex()
        
        pattern.findAll(template).forEach { matchResult ->
            val variableName = matchResult.groupValues[1]
            val variable = apiModel.variables?.get(variableName)
            
            if (variable != null) {
                val value = payload.eval<Any>(variable.defaultValue)
                result = result.replace("{$variableName}", value?.toString() ?: "")
            }
        }
        
        return result
    }

    /**
     * Create expression context with future state and response data
     */
    private fun createExprContext(
        state: FutureState,
        responseData: Any?,
        errorData: Any?
    ): DefaultScopeContext {
        val stateString = when (state) {
            FutureState.LOADING -> "loading"
            FutureState.COMPLETED -> "completed"
            FutureState.ERROR -> "error"
        }

        val variables = mutableMapOf<String, Any?>(
            "futureState" to stateString,
            "response" to responseData,
            "error" to errorData
        )

        // Add named reference if refName is provided
        refName?.let { name ->
            variables[name] = mapOf(
                "futureState" to stateString,
                "response" to responseData,
                "error" to errorData
            )
        }

        return DefaultScopeContext(variables = variables)
    }

    /**
     * Internal data class to hold API result
     */
    private data class ApiResult(
        val isSuccess: Boolean,
        val data: Any?,
        val error: Map<String, Any>?
    )
}

/** Builder function for FutureBuilder widget */
fun futureBuilderBuilder(
    data: VWNodeData,
    parent: VirtualNode?,
    registry: VirtualWidgetRegistry
): VirtualNode {
    val childrenData = data.childGroups?.mapValues { (_, childrenData) ->
        childrenData.map { data -> registry.createWidget(data, parent) }
    }

    return VWFutureBuilder(
        refName = data.refName,
        commonProps = data.commonProps,
        parent = parent,
        parentProps = data.parentProps,
        props = FutureBuilderProps.fromJson(data.props.value),
        slots = childrenData
    )
}
