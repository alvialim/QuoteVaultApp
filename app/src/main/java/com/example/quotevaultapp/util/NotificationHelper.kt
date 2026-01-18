package com.example.quotevaultapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.quotevaultapp.MainActivity
import com.example.quotevaultapp.R
import com.example.quotevaultapp.domain.model.Quote
import android.util.Log
import android.net.Uri

/**
 * Helper class for managing daily quote notifications
 * Provides methods to create notification channels, show notifications, and handle deep linking
 */
class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "daily_quote_channel"
        private const val CHANNEL_NAME = "Daily Quote"
        private const val CHANNEL_DESCRIPTION = "Get your daily inspirational quote"
        const val NOTIFICATION_ID = 1001
        
        private const val TAG = "NotificationHelper"
    }
    
    /**
     * Create notification channel for Android 8.0+ (API 26+)
     * Channel name: "Daily Quote"
     * Importance: DEFAULT
     * Enable sound and vibration
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }
    
    /**
     * Show daily quote notification
     * Title: "Your Daily Quote"
     * Content: quote text (truncated if too long)
     * Big text style for full quote
     * Author in subtext
     * Tap action: Deep link to open app and show quote detail
     * Small icon: app icon
     * 
     * @param quote Quote to display in notification
     */
    suspend fun showDailyQuoteNotification(quote: Quote) {
        // Create notification channel if not already created
        createNotificationChannel()
        
        // Check if notifications are enabled
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are not enabled for this app")
            return
        }
        
        // Truncate quote text if too long for content text (max ~80 chars)
        val contentText = if (quote.text.length > 80) {
            quote.text.take(77) + "..."
        } else {
            quote.text
        }
        
        // Create deep link intent to open quote detail
        val deepLinkUri = Uri.parse("quotevault://quote/${quote.id}")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Add extras for navigation
            putExtra("quote_id", quote.id)
            putExtra("from_notification", true)
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            pendingIntentFlags
        )
        
        // Build notification with BigTextStyle for expandable content
        // Note: Notification icons should be white/transparent. Use a system icon for now,
        // or create a proper notification icon drawable in res/drawable/
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view) // System icon (replace with custom notification icon)
            .setContentTitle(context.getString(R.string.daily_quote_notification_title))
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(quote.text)
                    .setBigContentTitle("Your Daily Quote")
                    .setSummaryText("— ${quote.author}")
            )
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setOnlyAlertOnce(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        // Show notification
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Daily quote notification shown successfully")
            Log.d(TAG, "Notification details - Quote ID: ${quote.id}, Author: ${quote.author}, Text preview: \"${quote.text.take(50)}...\"")
            Log.d(TAG, "Deep link: quotevault://quote/${quote.id}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to show notification - SecurityException: ${e.message}", e)
            Log.e(TAG, "This may indicate missing POST_NOTIFICATIONS permission or notification channel issues")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification - Unexpected error: ${e.message}", e)
        }
    }
    
    /**
     * Cancel daily quote notification
     */
    fun cancelNotification() {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
            Log.d(TAG, "Daily quote notification cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel notification: ${e.message}", e)
        }
    }
    
    /**
     * Check if notifications are enabled for the app
     * 
     * @return true if notifications are enabled, false otherwise
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}