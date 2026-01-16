package com.example.quotevaultapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.model.User
import com.example.quotevaultapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class representing the sign up state
 */
sealed class SignUpState {
    data object Idle : SignUpState()
    data object Loading : SignUpState()
    data class Success(val user: User) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

/**
 * Password strength levels
 */
enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG
}

/**
 * ViewModel for Sign Up screen
 * Manages sign up state, form inputs, and registration logic
 */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // Display name input state
    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()
    
    // Email input state
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    // Password input state
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    // Confirm password input state
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()
    
    // Sign up state
    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState.asStateFlow()
    
    // Email validation state
    val isEmailValid: Boolean
        get() = _email.value.isNotBlank() && 
                android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()
    
    // Password validation state
    val isPasswordValid: Boolean
        get() = _password.value.isNotBlank() && _password.value.length >= 6
    
    // Confirm password validation state
    val isConfirmPasswordValid: Boolean
        get() = _confirmPassword.value.isNotBlank() && 
                _password.value == _confirmPassword.value
    
    // Form validation state
    val isFormValid: Boolean
        get() = isEmailValid && isPasswordValid && isConfirmPasswordValid
    
    // Password strength
    val passwordStrength: PasswordStrength
        get() = calculatePasswordStrength(_password.value)
    
    // Display name validation (optional, so always valid if empty or non-empty)
    val isDisplayNameValid: Boolean = true
    
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
    
    // Confirm password error message
    val confirmPasswordError: String?
        get() = when {
            _confirmPassword.value.isBlank() -> null
            _password.value != _confirmPassword.value -> 
                "Passwords do not match"
            else -> null
        }
    
    /**
     * Handle display name input change
     */
    fun onDisplayNameChange(value: String) {
        _displayName.value = value
        if (_signUpState.value is SignUpState.Error) {
            _signUpState.value = SignUpState.Idle
        }
    }
    
    /**
     * Handle email input change
     */
    fun onEmailChange(value: String) {
        _email.value = value
        if (_signUpState.value is SignUpState.Error) {
            _signUpState.value = SignUpState.Idle
        }
    }
    
    /**
     * Handle password input change
     */
    fun onPasswordChange(value: String) {
        _password.value = value
        // Clear confirm password if password changes
        if (_confirmPassword.value.isNotBlank() && _password.value != _confirmPassword.value) {
            // Trigger re-validation by updating confirm password
            _confirmPassword.value = _confirmPassword.value
        }
        if (_signUpState.value is SignUpState.Error) {
            _signUpState.value = SignUpState.Idle
        }
    }
    
    /**
     * Handle confirm password input change
     */
    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = value
        if (_signUpState.value is SignUpState.Error) {
            _signUpState.value = SignUpState.Idle
        }
    }
    
    /**
     * Handle sign up button click
     * Validates inputs and attempts to register the user
     */
    fun onSignUpClick() {
        if (!isFormValid) {
            val errorMessages = mutableListOf<String>()
            
            if (!isEmailValid) {
                errorMessages.add("Please enter a valid email address")
            }
            if (!isPasswordValid) {
                errorMessages.add("Password must be at least 6 characters")
            }
            if (!isConfirmPasswordValid) {
                errorMessages.add("Passwords do not match")
            }
            
            _signUpState.value = SignUpState.Error(
                errorMessages.joinToString("\n")
            )
            return
        }
        
        _signUpState.value = SignUpState.Loading
        
        viewModelScope.launch {
            val result = authRepository.signUp(
                email = _email.value.trim(),
                password = _password.value
            )
            
            when (result) {
                is Result.Success -> {
                    val user = result.data
                    // Update profile with display name if provided
                    if (_displayName.value.isNotBlank()) {
                        val updateResult = authRepository.updateProfile(
                            displayName = _displayName.value.trim(),
                            avatarUrl = null
                        )
                        when (updateResult) {
                            is Result.Success -> {
                                _signUpState.value = SignUpState.Success(updateResult.data)
                            }
                            is Result.Error -> {
                                // Sign up succeeded but profile update failed
                                // Still consider it success since user is created
                                _signUpState.value = SignUpState.Success(user)
                            }
                        }
                    } else {
                        _signUpState.value = SignUpState.Success(user)
                    }
                }
                is Result.Error -> {
                    val errorMessage = result.exception.message 
                        ?: "An error occurred during sign up. Please try again."
                    _signUpState.value = SignUpState.Error(errorMessage)
                }
            }
        }
    }
    
    /**
     * Calculate password strength based on length and complexity
     */
    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.length < 6) return PasswordStrength.WEAK
        if (password.length < 10) return PasswordStrength.MEDIUM
        
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        
        val complexity = listOf(hasUpper, hasLower, hasDigit, hasSpecial).count { it }
        
        return when {
            complexity >= 3 && password.length >= 10 -> PasswordStrength.STRONG
            complexity >= 2 && password.length >= 8 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        if (_signUpState.value is SignUpState.Error) {
            _signUpState.value = SignUpState.Idle
        }
    }
    
    /**
     * Reset sign up state to Idle
     */
    fun resetState() {
        _signUpState.value = SignUpState.Idle
    }
}
