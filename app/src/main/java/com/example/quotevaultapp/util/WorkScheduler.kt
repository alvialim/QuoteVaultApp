package com.example.quotevaultapp.util

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.quotevaultapp.workers.DailyQuoteWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.asExecutor
import java.util.Calendar
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utility object for scheduling and managing WorkManager tasks
 * Handles daily quote notification scheduling
 * 
 * Edge Cases Handled:
 * - App killed by system: WorkManager persists work across app restarts
 * - Time zone changes: Calendar.getInstance() uses device timezone automatically
 * - Device time changes: WorkManager recalculates based on system time
 * - Battery optimization: Constraints allow work without charging/battery requirements
 * - Network offline: Constraints set NetworkType.NOT_REQUIRED for offline operation
 */
object WorkScheduler {
    
    private const val TAG = "WorkScheduler"
    
    /**
     * Schedule daily quote notification at specified time
     * 
     * @param context Application context
     * @param hour Hour of day (0-23) when notification should appear
     * @param minute Minute of hour (0-59) when notification should appear
     */
    fun scheduleDailyQuoteNotification(
        context: Context,
        hour: Int,
        minute: Int
    ) {
        try {
            val workManager = WorkManager.getInstance(context)
            
            // Cancel existing work with the same name
            workManager.cancelUniqueWork(DailyQuoteWorker.WORK_NAME)
            
            // Calculate initial delay to target time
            val calendar = Calendar.getInstance()
            val now = System.currentTimeMillis()
            
            // Set target time
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            // If target time has already passed today, schedule for tomorrow
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            val initialDelay = calendar.timeInMillis - now
            
            val targetTimeFormatted = String.format("%02d:%02d", hour, minute)
            val delayHours = TimeUnit.MILLISECONDS.toHours(initialDelay)
            val delayMinutes = TimeUnit.MILLISECONDS.toMinutes(initialDelay) % 60
            
            Log.d(
                TAG,
                "Scheduling daily quote notification for $targetTimeFormatted, " +
                        "initial delay: ${delayHours}h ${delayMinutes}m (${initialDelay}ms)"
            )
            Log.d(TAG, "Target time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(calendar.time)}")
            Log.d(TAG, "Current time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(now))}")
            
            // Create constraints - network not required, battery not low
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Work offline
                .setRequiresBatteryNotLow(false) // Don't require battery level
                .setRequiresCharging(false) // Don't require charging
                .build()
            
            // Create periodic work request with 24-hour interval
            // Minimum interval is 15 minutes, but we'll request 24 hours
            // WorkManager will enforce minimum interval
            val workRequest = PeriodicWorkRequestBuilder<DailyQuoteWorker>(
                24, // Repeat interval
                TimeUnit.HOURS
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag("daily_quote") // Tag for easier identification
                .build()
            
            // Enqueue unique work (replaces existing work with same name)
            workManager.enqueueUniquePeriodicWork(
                DailyQuoteWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE, // Replace existing work
                workRequest
            )
            
            Log.d(TAG, "Daily quote notification scheduled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling daily quote notification: ${e.message}", e)
        }
    }
    
    /**
     * Cancel daily quote notification
     * 
     * @param context Application context
     */
    fun cancelDailyQuoteNotification(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(DailyQuoteWorker.WORK_NAME)
            Log.d(TAG, "Daily quote notification cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling daily quote notification: ${e.message}", e)
        }
    }
    
    /**
     * Check if daily quote notification is scheduled
     * 
     * @param context Application context
     * @return true if work is scheduled, false otherwise
     */
    suspend fun isScheduled(context: Context): Boolean {
        return try {
            val workManager = WorkManager.getInstance(context)
            val workInfosFuture = workManager.getWorkInfosForUniqueWork(DailyQuoteWorker.WORK_NAME)
            
            // Convert ListenableFuture to suspend function
            val workInfos = suspendCancellableCoroutine<List<WorkInfo>> { continuation ->
                workInfosFuture.addListener(
                    {
                        try {
                            continuation.resume(workInfosFuture.get())
                        } catch (e: Exception) {
                            if (e !is CancellationException) {
                                continuation.resumeWithException(e)
                            }
                        }
                    },
                    Dispatchers.IO.asExecutor()
                )
                
                continuation.invokeOnCancellation {
                    workInfosFuture.cancel(true)
                }
            }
            
            val isEnqueued = workInfos.any { workInfo: WorkInfo ->
                workInfo.state == WorkInfo.State.ENQUEUED ||
                        workInfo.state == WorkInfo.State.RUNNING
            }
            Log.d(TAG, "Daily quote notification scheduled: $isEnqueued")
            isEnqueued
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if daily quote notification is scheduled: ${e.message}", e)
            false
        }
    }
}
