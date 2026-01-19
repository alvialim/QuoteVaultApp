package com.example.quotevaultapp.domain.model

/**
 * Sealed class representing the result of an operation
 * Supports Success, Error, and Loading states for comprehensive error handling
 * 
 * @param T The type of data returned on success
 */
sealed class Result<out T> {
    /**
     * Successful result with data
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * Error result with exception and optional user-friendly message
     * 
     * @param exception The exception that occurred
     * @param message Optional user-friendly error message (defaults to null)
     */
    data class Error(
        val exception: Exception,
        val message: String? = null
    ) : Result<Nothing>()
    
    /**
     * Loading state to indicate operation is in progress
     */
    object Loading : Result<Nothing>()
    
    /**
     * Returns true if the result is Success
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Returns true if the result is Error
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Returns true if the result is Loading
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Gets the data if Success, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
        is Loading -> null
    }
    
    /**
     * Gets the exception if Error, null otherwise
     */
    fun exceptionOrNull(): Exception? = when (this) {
        is Success -> null
        is Error -> exception
        is Loading -> null
    }
    
    /**
     * Gets the error message if Error, null otherwise
     */
    fun errorMessageOrNull(): String? = when (this) {
        is Success -> null
        is Error -> message ?: exception.message
        is Loading -> null
    }
}
