package com.example.quotevaultapp.presentation.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quotevaultapp.domain.repository.AuthRepository
import kotlinx.coroutines.delay

/**
 * Splash screen that checks authentication state and navigates accordingly
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    authRepository: AuthRepository
) {
    LaunchedEffect(Unit) {
        // Show splash for minimum time (1 second)
        delay(1000)
        
        // Check auth state - this checks Supabase session
        android.util.Log.d("SplashScreen", "Checking auth state on app launch")
        val currentUserResult = authRepository.getCurrentUser()
        
        when (currentUserResult) {
            is com.example.quotevaultapp.domain.model.Result.Success -> {
                val user = currentUserResult.data
                android.util.Log.d("SplashScreen", "Auth check result: user=${user?.email ?: "null"}")
                if (user != null) {
                    android.util.Log.d("SplashScreen", "User is logged in, navigating to Home")
                    onNavigateToHome()
                } else {
                    android.util.Log.d("SplashScreen", "No user found, navigating to Login")
                    onNavigateToLogin()
                }
            }
            is com.example.quotevaultapp.domain.model.Result.Error -> {
                // On error, navigate to login
                android.util.Log.w("SplashScreen", "Error checking auth state: ${currentUserResult.exception.message}, navigating to Login")
                onNavigateToLogin()
            }
            else -> {
                android.util.Log.w("SplashScreen", "Unknown auth state result, navigating to Login")
                onNavigateToLogin()
            }
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "QuoteVault",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 36.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(
                modifier = Modifier.height(24.dp)
            )
            
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
