package com.example.quotevaultapp.util

/**
 * Sealed class representing UI state for ViewModels
 * Used to manage loading, success, error, and idle states in the UI
 * 
 * @param T The type of data displayed on success
 */
sealed class UiState<out T> {
    /**
     * Initial state - no data loaded yet
     */
    object Idle : UiState<Nothing>()
    
    /**
     * Loading state - operation in progress
     */
    object Loading : UiState<Nothing>()
    
    /**
     * Success state with data
     * 
     * @param data The successful data to display
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Error state with message
     * 
     * @param message User-friendly error message
     */
    data class Error(val message: String) : UiState<Nothing>()
    
    /**
     * Returns true if state is Idle
     */
    val isIdle: Boolean
        get() = this is Idle
    
    /**
     * Returns true if state is Loading
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Returns true if state is Success
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Returns true if state is Error
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Gets the data if Success, null otherwise
     */
    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * Gets the error message if Error, null otherwise
     */
    fun getErrorMessageOrNull(): String? = when (this) {
        is Error -> message
        else -> null
    }
}
