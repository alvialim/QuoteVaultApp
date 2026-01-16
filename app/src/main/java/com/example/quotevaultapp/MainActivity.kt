package com.example.quotevaultapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.quotevaultapp.domain.repository.AuthRepository
import com.example.quotevaultapp.navigation.QuoteVaultNavGraph
import com.example.quotevaultapp.navigation.Screen
import com.example.quotevaultapp.presentation.theme.QuoteVaultTheme
import com.example.quotevaultapp.util.NotificationHelper
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main Activity for QuoteVaultApp
 * Handles app initialization, navigation, and deep linking
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate() for Android 12+ splash screen API
        // This provides the native Android 12+ splash screen during app initialization
        // Our custom SplashScreen composable will handle auth state checking
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installSplashScreen()
        }
        
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Handle system bars (for devices that don't support edge-to-edge natively)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Set up content
        setContent {
            QuoteVaultTheme {
                // Handle system UI (status bar, navigation bar) with Accompanist
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                
                SideEffect {
                    // Configure status bar - transparent with adaptive icons
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                    
                    // Configure navigation bar - transparent with adaptive icons
                    systemUiController.setNavigationBarColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                    
                    // Configure system bars behavior for edge-to-edge
                    systemUiController.setSystemBarsBehavior(
                        com.google.accompanist.systemuicontroller.SystemBarsBehavior.ADJUST_PADDING
                    )
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Initialize notification channel on first launch
                    val context = LocalContext.current
                    LaunchedEffect(Unit) {
                        NotificationHelper.createNotificationChannel(context)
                    }
                    
                    // Setup navigation with deep link handling
                    QuoteVaultNavGraph(
                        navController = androidx.navigation.compose.rememberNavController(),
                        authRepository = authRepository,
                        startDestination = determineStartDestination(intent)
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        // Handle deep links when activity is already running
        handleDeepLink(intent)
    }
    
    /**
     * Determine start destination based on intent (deep links, notifications, etc.)
     */
    private fun determineStartDestination(intent: Intent?): String {
        // Check for deep link
        if (intent?.data != null) {
            return handleDeepLinkDestination(intent.data)
        }
        
        // Check for notification action
        if (intent?.getStringExtra("navigation_destination") != null) {
            return intent.getStringExtra("navigation_destination") ?: Screen.Splash.route
        }
        
        // Default: Start with splash screen
        return Screen.Splash.route
    }
    
    /**
     * Handle deep link and navigate to appropriate screen
     */
    private fun handleDeepLink(intent: Intent?) {
        if (intent?.data != null) {
            val uri = intent.data
            // Navigation will be handled automatically by NavGraph via navDeepLink
            // This method can be used for additional deep link processing if needed
            uri?.let {
                android.util.Log.d("MainActivity", "Handling deep link: $it")
            }
        }
    }
    
    /**
     * Extract destination from deep link URI
     */
    private fun handleDeepLinkDestination(uri: Uri?): String {
        if (uri == null) return Screen.Splash.route
        
        return when {
            uri.scheme == "quotevaultapp" -> {
                when (uri.host) {
                    "quote" -> {
                        val quoteId = uri.lastPathSegment
                        if (quoteId != null) {
                            Screen.QuoteDetail.createRoute(quoteId)
                        } else {
                            Screen.Splash.route
                        }
                    }
                    "collection" -> {
                        val collectionId = uri.lastPathSegment
                        if (collectionId != null) {
                            Screen.CollectionDetail.createRoute(collectionId)
                        } else {
                            Screen.Splash.route
                        }
                    }
                    "home" -> Screen.Home.route
                    "favorites" -> Screen.Favorites.route
                    else -> Screen.Splash.route
                }
            }
            // Handle standard deep link format
            uri.toString().contains("quotevaultapp://") -> {
                // Parse the deep link and return appropriate route
                Screen.Splash.route // Will be handled by NavGraph's navDeepLink
            }
            else -> Screen.Splash.route
        }
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}
