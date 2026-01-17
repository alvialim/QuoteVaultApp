package com.example.quotevaultapp

import android.app.Application
import com.example.quotevaultapp.util.NotificationHelper

/**
 * Application class for QuoteVaultApp
 * Simple Application class without dependency injection
 */
class QuoteVaultApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Create notification channel for daily quotes
        val notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()
        
        // Note: WorkManager scheduling and other initialization
        // will be handled elsewhere when needed
    }
}
