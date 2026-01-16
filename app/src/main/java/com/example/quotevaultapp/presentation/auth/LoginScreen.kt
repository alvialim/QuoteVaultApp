package com.example.quotevaultapp.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

/**
 * Login screen for user authentication
 * 
 * @param onLoginSuccess Callback when login is successful
 * @param onNavigateToSignUp Callback to navigate to sign up screen
 * @param onNavigateToForgotPassword Callback to navigate to forgot password screen
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: AuthViewModel? = null
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    
    // Validate form
    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordValid = password.isNotBlank() && password.length >= 6
    val isFormValid = isEmailValid && isPasswordValid
    
    // Handle login result
    LaunchedEffect(Unit) {
        // Observe auth state changes if viewModel is provided
        // This would be handled by the ViewModel in a real implementation
    }
    
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
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Sign in to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
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
                    placeholder = { Text("Enter your password") },
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
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isFormValid && !isLoading) {
                                // Perform login
                                handleLogin(email, password, viewModel) {
                                    isLoading = true
                                    errorMessage = null
                                }
                            } else {
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    isError = password.isNotBlank() && !isPasswordValid,
                    supportingText = if (password.isNotBlank() && !isPasswordValid) {
                        { Text("Password must be at least 6 characters") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Forgot Password Link
                TextButton(
                    onClick = onNavigateToForgotPassword,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                ) {
                    Text("Forgot password?")
                }
                
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
                
                // Login Button
                Button(
                    onClick = {
                        handleLogin(email, password, viewModel) {
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
                            text = "Sign In",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Sign Up Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onNavigateToSignUp) {
                        Text("Sign Up")
                    }
                }
            }
        }
    }
}

/**
 * Handle login logic (placeholder - should be handled by ViewModel)
 */
private fun handleLogin(
    email: String,
    password: String,
    viewModel: AuthViewModel?,
    onLoading: () -> Unit
) {
    // This would be handled by ViewModel in a real implementation
    // For now, this is a placeholder
    onLoading()
}
