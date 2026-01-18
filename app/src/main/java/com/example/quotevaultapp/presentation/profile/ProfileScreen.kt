package com.example.quotevaultapp.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quotevaultapp.domain.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * Profile screen showing user information and settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authRepository: AuthRepository? = null,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isLoggingOut by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Settings button
            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoggingOut
            ) {
                Text("Settings")
            }
            
            // Logout button
            OutlinedButton(
                onClick = {
                    if (authRepository != null) {
                        isLoggingOut = true
                        scope.launch {
                            try {
                                // Actually sign out from Supabase
                                authRepository.signOut()
                                android.util.Log.d("ProfileScreen", "User signed out successfully")
                                // Navigate to login after sign out
                                onNavigateToLogin()
                            } catch (e: Exception) {
                                android.util.Log.e("ProfileScreen", "Error signing out: ${e.message}", e)
                                isLoggingOut = false
                            }
                        }
                    } else {
                        // Fallback: just navigate if no auth repository
                        onNavigateToLogin()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoggingOut
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Logout")
                }
            }
        }
    }
}