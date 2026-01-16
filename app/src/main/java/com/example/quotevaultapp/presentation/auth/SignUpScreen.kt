package com.example.quotevaultapp.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Password strength levels
 */
enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG
}

/**
 * Sign up screen for new user registration
 * 
 * @param onSignUpSuccess Callback when sign up is successful
 * @param onNavigateToLogin Callback to navigate to login screen
 */
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: AuthViewModel? = null
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    
    // Validation
    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val passwordStrength = calculatePasswordStrength(password)
    val isPasswordValid = password.isNotBlank() && password.length >= 6
    val isConfirmPasswordValid = confirmPassword.isNotBlank() && password == confirmPassword
    val isDisplayNameValid = displayName.isNotBlank() || displayName.isEmpty()
    val isFormValid = isEmailValid && isPasswordValid && isConfirmPasswordValid
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Sign up to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Display Name Field (Optional)
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { 
                        displayName = it
                        errorMessage = null
                    },
                    label = { Text("Display Name (Optional)") },
                    placeholder = { Text("Enter your name") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Display name icon"
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email") },
                    placeholder = { Text("Enter your email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email icon"
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = email.isNotBlank() && !isEmailValid,
                    supportingText = if (email.isNotBlank() && !isEmailValid) {
                        { Text("Please enter a valid email address") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    placeholder = { Text("Create a password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password icon"
                        )
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Text(
                                text = if (passwordVisible) "Hide" else "Show",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = password.isNotBlank() && !isPasswordValid,
                    supportingText = if (password.isNotBlank() && !isPasswordValid) {
                        { Text("Password must be at least 6 characters") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Password Strength Indicator
                if (password.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordStrengthIndicator(
                        strength = passwordStrength,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Confirm Password") },
                    placeholder = { Text("Re-enter your password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confirm password icon"
                        )
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                        ) {
                            Text(
                                text = if (confirmPasswordVisible) "Hide" else "Show",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isFormValid && !isLoading) {
                                // Perform sign up
                                handleSignUp(email, password, displayName, viewModel) {
                                    isLoading = true
                                    errorMessage = null
                                }
                            } else {
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    isError = confirmPassword.isNotBlank() && !isConfirmPasswordValid,
                    supportingText = if (confirmPassword.isNotBlank() && !isConfirmPasswordValid) {
                        { Text("Passwords do not match") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Error Message
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Sign Up Button
                Button(
                    onClick = {
                        handleSignUp(email, password, displayName, viewModel) {
                            isLoading = true
                            errorMessage = null
                        }
                    },
                    enabled = isFormValid && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Sign Up",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Login Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onNavigateToLogin) {
                        Text("Sign In")
                    }
                }
            }
        }
    }
}

/**
 * Password strength indicator component
 */
@Composable
private fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    val (progress, color, label) = when (strength) {
        PasswordStrength.WEAK -> Triple(0.33f, MaterialTheme.colorScheme.error, "Weak")
        PasswordStrength.MEDIUM -> Triple(0.66f, androidx.compose.ui.graphics.Color(0xFFFFC107), "Medium")
        PasswordStrength.STRONG -> Triple(1f, MaterialTheme.colorScheme.primary, "Strong")
    }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
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
 * Handle sign up logic (placeholder - should be handled by ViewModel)
 */
private fun handleSignUp(
    email: String,
    password: String,
    displayName: String,
    viewModel: AuthViewModel?,
    onLoading: () -> Unit
) {
    // This would be handled by ViewModel in a real implementation
    // For now, this is a placeholder
    onLoading()
}
