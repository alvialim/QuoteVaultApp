package com.example.quotevaultapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quotevaultapp.data.remote.supabase.SupabaseAuthRepository
import com.example.quotevaultapp.navigation.QuoteVaultNavGraph
import com.example.quotevaultapp.navigation.Screen
import com.example.quotevaultapp.presentation.components.NotificationPermissionDialog
import com.example.quotevaultapp.presentation.theme.QuoteVaultTheme
import com.example.quotevaultapp.util.NotificationPermissionHelper

/**
 * Main Activity for QuoteVaultApp
 * Simple version without dependency injection
 * Handles deep linking from notifications, widgets, and external apps
 */
class MainActivity : ComponentActivity() {
    
    private var navController: NavController? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create repository instance directly (without DI)
        val authRepository = SupabaseAuthRepository()
        
        setContent {
            QuoteVaultTheme {
                val context = LocalContext.current
                var showPermissionDialog by remember { mutableStateOf(false) }
                
                // Check notification permission on app launch (Android 13+)
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = NotificationPermissionHelper.hasNotificationPermission(context)
                        if (!hasPermission) {
                            Log.d(TAG, "Notification permission not granted, showing permission dialog")
                            showPermissionDialog = true
                        }
                    }
                }
                
                // Create NavController and store reference for deep link handling
                val navControllerState = rememberNavController()
                navController = navControllerState
                
                QuoteVaultNavGraph(
                    navController = navControllerState,
                    authRepository = authRepository,
                    startDestination = getDeepLinkDestination(intent) ?: Screen.Splash.route
                )
                
                // Show permission dialog on app launch if needed
                if (showPermissionDialog) {
                    NotificationPermissionDialog(
                        onGranted = {
                            Log.d(TAG, "Notification permission granted on app launch")
                            showPermissionDialog = false
                        },
                        onDenied = {
                            Log.d(TAG, "Notification permission denied on app launch")
                            showPermissionDialog = false
                        }
                    )
                }
            }
        }
        
        // Handle initial intent (deep link on app launch)
        handleDeepLink(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Update intent for when app is already running
        setIntent(intent)
        intent?.let { handleDeepLink(it) }
    }
    
    /**
     * Handle deep link intent and navigate accordingly
     */
    private fun handleDeepLink(intent: Intent?) {
        val navController = this.navController ?: return
        
        val data: Uri? = intent?.data
        if (data != null) {
            try {
                val handled = navController.handleDeepLink(intent)
                if (handled) {
                    Log.d(TAG, "Deep link handled: ${data}")
                } else {
                    Log.w(TAG, "Deep link not handled: ${data}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling deep link: ${e.message}", e)
            }
        }
    }
    
    /**
     * Get start destination from deep link intent
     * Returns null to use default if no deep link
     */
    private fun getDeepLinkDestination(intent: Intent?): String? {
        val data: Uri? = intent?.data
        return if (data != null) {
            // Return null to let NavGraph use default logic
            // Deep links are handled by NavController.handleDeepLink()
            null
        } else {
            null
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
