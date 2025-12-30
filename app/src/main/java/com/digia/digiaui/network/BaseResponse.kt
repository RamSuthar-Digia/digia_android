package com.digia.digiaui.network

/// Base response wrapper for API calls.
///
/// [BaseResponse] provides a standardized way to handle API responses,
/// encapsulating success status, data payload, and error information.
/// This allows for consistent error handling and response parsing across
/// different API endpoints.
///
/// Type parameter [T] represents the expected data type when the request succeeds.
data class BaseResponse<T>(
    /// Indicates whether the API request was successful
    val isSuccess: Boolean,

    /// The parsed response data, null if the request failed
    val data: T?,

    /// Error information, null if the request succeeded
    val error: Map<String, Any?>?
) {
    companion object {
        /// Creates a [BaseResponse] from JSON data using a deserialization function.
        ///
        /// This method is used to parse successful API responses into typed objects.
        /// The [fromJsonT] function should handle the conversion from raw JSON
        /// to the expected type T.
        ///
        /// Parameters:
        /// - [json]: Raw JSON response data
        /// - [fromJsonT]: Function to deserialize JSON to type T
        ///
        /// Returns a successful [BaseResponse] with the parsed data.
        fun <T> fromJson(json: Map<String, Any?>, fromJsonT: (Any?) -> T): BaseResponse<T> {
            return BaseResponse(
                isSuccess = true,
                data = fromJsonT(json),
                error = null
            )
        }
    }
}
