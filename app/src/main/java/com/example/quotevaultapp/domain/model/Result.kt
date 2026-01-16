package com.example.quotevaultapp.domain.model

/**
 * Sealed class representing the result of an operation
 * Can be either Success with data or Error with exception
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    
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
     * Gets the data if Success, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    /**
     * Gets the exception if Error, null otherwise
     */
    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Error -> exception
    }
}
