package com.example.quotevaultapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.AuthRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Sealed class representing the forgot password state
 */
sealed class ForgotPasswordState {
    data object Idle : ForgotPasswordState()
    data object Loading : ForgotPasswordState()
    data class Success(val email: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

/**
 * ViewModel for Forgot Password screen
 * Manages email input and password reset functionality
 */
class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = SupabaseAuthRepository()
) : ViewModel() {
    
    // Email input state
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    // Forgot password state
    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()
    
    // Email validation state
    val isEmailValid: Boolean
        get() = _email.value.isNotBlank() && 
                android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()
    
    // Email error message
    val emailError: String?
        get() = when {
            _email.value.isBlank() -> null
            !android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches() -> 
                "Please enter a valid email address"
            else -> null
        }
    
    /**
     * Handle email input change
     */
    fun onEmailChange(value: String) {
        _email.value = value
        // Clear error state when user starts typing
        if (_forgotPasswordState.value is ForgotPasswordState.Error) {
            _forgotPasswordState.value = ForgotPasswordState.Idle
        }
    }
    
    /**
     * Handle send reset link button click
     * Validates email and sends password reset email
     */
    fun onSendResetLinkClick() {
        if (!isEmailValid) {
            _forgotPasswordState.value = ForgotPasswordState.Error(
                "Please enter a valid email address"
            )
            return
        }
        
        _forgotPasswordState.value = ForgotPasswordState.Loading
        
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(
                email = _email.value.trim()
            )
            
            when (result) {
                is Result.Success -> {
                    _forgotPasswordState.value = ForgotPasswordState.Success(_email.value.trim())
                }
                is Result.Error -> {
                    val errorMessage = result.exception.message 
                        ?: "An error occurred while sending the reset link. Please try again."
                    _forgotPasswordState.value = ForgotPasswordState.Error(errorMessage)
                }
            }
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        if (_forgotPasswordState.value is ForgotPasswordState.Error) {
            _forgotPasswordState.value = ForgotPasswordState.Idle
        }
    }
    
    /**
     * Reset state to Idle
     */
    fun resetState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
        _email.value = ""
    }
}
