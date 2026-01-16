package com.example.quotevaultapp.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.quotevaultapp.workers.DailyQuoteWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Helper class for scheduling daily quote notifications
 */
object WorkScheduler {
    
    /**
     * Default time for daily quote (9:00 AM)
     */
    private const val DEFAULT_HOUR = 9
    private const val DEFAULT_MINUTE = 0
    
    /**
     * Schedule daily quote notification at specified time
     * 
     * @param context Application context
     * @param hour Hour of day (0-23)
     * @param minute Minute of hour (0-59)
     */
    fun scheduleDailyQuoteNotification(
        context: Context,
        hour: Int = DEFAULT_HOUR,
        minute: Int = DEFAULT_MINUTE
    ) {
        val workManager = WorkManager.getInstance(context)
        
        // Calculate initial delay to next scheduled time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()
        
        // Constraints: Requires network (for fetching quote)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Create periodic work request (runs once per day)
        val dailyQuoteWork = PeriodicWorkRequestBuilder<DailyQuoteWorker>(
            1, TimeUnit.DAYS // Repeat interval
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()
        
        // Enqueue unique work (replaces existing if already scheduled)
        workManager.enqueueUniquePeriodicWork(
            DailyQuoteWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyQuoteWork
        )
    }
    
    /**
     * Cancel daily quote notifications
     */
    fun cancelDailyQuoteNotification(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(DailyQuoteWorker.WORK_NAME)
    }
    
    /**
     * Check if daily quote notification is scheduled
     */
    suspend fun isScheduled(context: Context): Boolean {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(DailyQuoteWorker.WORK_NAME)
        
        return workInfos.get().any { workInfo ->
            workInfo.state == androidx.work.WorkInfo.State.ENQUEUED ||
            workInfo.state == androidx.work.WorkInfo.State.RUNNING
        }
    }
}
