package com.example.quotevaultapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for QuoteVaultApp
 * Required for Hilt dependency injection
 */
@HiltAndroidApp
class QuoteVaultApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Application initialization if needed
    }
}
