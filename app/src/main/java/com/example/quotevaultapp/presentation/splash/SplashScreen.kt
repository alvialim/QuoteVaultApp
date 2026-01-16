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
        
        // Check auth state
        val currentUser = authRepository.getCurrentUser()
        
        when (currentUser) {
            is com.example.quotevaultapp.domain.model.Result.Success -> {
                if (currentUser.data != null) {
                    onNavigateToHome()
                } else {
                    onNavigateToLogin()
                }
            }
            is com.example.quotevaultapp.domain.model.Result.Error -> {
                // On error, navigate to login
                onNavigateToLogin()
            }
            else -> {
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
            
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.height(24.dp)
            )
            
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
