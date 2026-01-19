package com.example.quotevaultapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.model.User
import com.example.quotevaultapp.domain.repository.AuthRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sealed class representing the login state
 */
sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

/**
 * ViewModel for Login screen
 * Manages login state, email/password inputs, and authentication logic
 */
class LoginViewModel(
    private val authRepository: AuthRepository = SupabaseAuthRepository()
) : ViewModel() {
    
    // Email input state
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    // Password input state
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    // Login state
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    // Email validation state
    val isEmailValid: Boolean
        get() = _email.value.isNotBlank() && 
                android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()
    
    // Password validation state
    val isPasswordValid: Boolean
        get() = _password.value.isNotBlank() && _password.value.length >= 6
    
    // Form validation state
    val isFormValid: Boolean
        get() = isEmailValid && isPasswordValid
    
    // Email error message
    val emailError: String?
        get() = when {
            _email.value.isBlank() -> null
            !android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches() -> 
                "Please enter a valid email address"
            else -> null
        }
    
    // Password error message
    val passwordError: String?
        get() = when {
            _password.value.isBlank() -> null
            _password.value.length < 6 -> 
                "Password must be at least 6 characters"
            else -> null
        }
    
    /**
     * Handle email input change
     */
    fun onEmailChange(value: String) {
        _email.value = value
        // Clear error state when user starts typing
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Idle
        }
    }
    
    /**
     * Handle password input change
     */
    fun onPasswordChange(value: String) {
        _password.value = value
        // Clear error state when user starts typing
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Idle
        }
    }
    
    /**
     * Handle login button click
     * Validates inputs and attempts to sign in the user
     */
    fun onLoginClick() {
        if (!isFormValid) {
            _loginState.value = LoginState.Error("Please fill in all fields correctly")
            return
        }
        
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            val result = authRepository.signIn(
                email = _email.value.trim(),
                password = _password.value
            )
            
            when (result) {
                is Result.Success -> {
                    _loginState.value = LoginState.Success(result.data)
                }
                is Result.Error -> {
                    val errorMessage = result.exception.message 
                        ?: "An error occurred during login. Please try again."
                    _loginState.value = LoginState.Error(errorMessage)
                }
                is Result.Loading -> {
                    _loginState.value = LoginState.Loading
                }
            }
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Idle
        }
    }
    
    /**
     * Reset login state to Idle
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
