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

/**
 * Notification channel ID for daily quotes
 */
private const val CHANNEL_ID = "daily_quote_channel"
private const val CHANNEL_NAME = "Daily Quotes"
private const val CHANNEL_DESCRIPTION = "Notifications for your daily inspirational quotes"

/**
 * Notification ID for daily quote notifications
 */
private const val NOTIFICATION_ID = 1

/**
 * Helper class for managing daily quote notifications
 */
object NotificationHelper {
    
    /**
     * Create notification channel for Android 8.0+ (API 26+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Show daily quote notification
     * 
     * @param context Application context
     * @param quote Quote of the day to display in notification
     */
    fun showDailyQuoteNotification(context: Context, quote: Quote) {
        // Create notification channel if not already created
        createNotificationChannel(context)
        
        // Create intent for when notification is tapped (deep link to app)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add extra data if needed for navigation
            putExtra("quote_id", quote.id)
            putExtra("from_notification", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view) // Replace with your app icon
            .setContentTitle("Quote of the Day")
            .setContentText(quote.text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(quote.text)
                    .setBigContentTitle(quote.author)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()
        
        // Show notification
        with(NotificationManagerCompat.from(context)) {
            if (areNotificationsEnabled()) {
                notify(NOTIFICATION_ID, notification)
            }
        }
    }
    
    /**
     * Check if notifications are enabled for the app
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    
    /**
     * Cancel daily quote notification
     */
    fun cancelDailyQuoteNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
