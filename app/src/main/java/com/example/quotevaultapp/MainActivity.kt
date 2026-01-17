package com.example.quotevaultapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.quotevaultapp.data.remote.supabase.SupabaseAuthRepository
import com.example.quotevaultapp.navigation.QuoteVaultNavGraph
import com.example.quotevaultapp.presentation.theme.QuoteVaultTheme

/**
 * Main Activity for QuoteVaultApp
 * Simple version without dependency injection
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create repository instance directly (without DI)
        val authRepository = SupabaseAuthRepository()
        
        setContent {
            QuoteVaultTheme {
                QuoteVaultNavGraph(
                    navController = rememberNavController(),
                    authRepository = authRepository
                )
            }
        }
    }
}
