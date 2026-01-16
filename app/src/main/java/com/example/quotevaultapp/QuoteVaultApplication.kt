package com.example.quotevaultapp

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.quotevaultapp.util.NotificationHelper
import com.example.quotevaultapp.util.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for QuoteVaultApp
 * Required for Hilt dependency injection
 */
@HiltAndroidApp
class QuoteVaultApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workManagerConfiguration: Configuration
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager with Hilt configuration
        WorkManager.initialize(this, workManagerConfiguration)
        
        // Create notification channel for daily quotes
        NotificationHelper.createNotificationChannel(this)
        
        // Schedule daily quote notification on app launch
        // Default time: 9:00 AM
        // This will be updated when user changes notification time in settings
        WorkScheduler.scheduleDailyQuoteNotification(
            context = this,
            hour = 9,
            minute = 0
        )
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return workManagerConfiguration
    }
}
