package com.example.quotevaultapp.util

import android.content.Context
import android.util.Log
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.quotevaultapp.data.remote.supabase.SupabaseQuoteRepository
import com.example.quotevaultapp.domain.repository.QuoteRepository
import com.example.quotevaultapp.workers.DailyQuoteWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utility object for testing notifications and getting scheduling information
 * Useful for debugging and development
 */
object NotificationTester {
    
    private const val TAG = "NotificationTester"
    
    /**
     * Show a notification immediately for testing purposes
     * Fetches the current quote of the day and displays it
     * 
     * @param context Application context
     */
    suspend fun testImmediateNotification(context: Context) {
        try {
            Log.d(TAG, "Testing immediate notification...")
            val quoteRepository: QuoteRepository = SupabaseQuoteRepository()
            
            when (val result = quoteRepository.getQuoteOfTheDay()) {
                is com.example.quotevaultapp.domain.model.Result.Success -> {
                    val notificationHelper = NotificationHelper(context)
                    notificationHelper.showDailyQuoteNotification(result.data)
                    Log.d(TAG, "Test notification shown successfully for quote: ${result.data.id}")
                }
                is com.example.quotevaultapp.domain.model.Result.Error -> {
                    Log.e(TAG, "Failed to fetch quote for test notification: ${result.exception.message}", result.exception)
                }
                is com.example.quotevaultapp.domain.model.Result.Loading -> {
                    Log.d(TAG, "Loading quote of the day for test notification...")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing test notification: ${e.message}", e)
        }
    }
    
    /**
     * Get human-readable next scheduled notification time
     * Calculates next notification time based on configured hour/minute
     * Returns formatted string with next notification time or "Not scheduled"
     * 
     * @param context Application context
     * @param hour Hour of day (0-23) when notification should appear
     * @param minute Minute of hour (0-59) when notification should appear
     * @return Human-readable string with next notification time or "Not scheduled"
     */
    fun getNextScheduledTime(
        context: Context,
        hour: Int,
        minute: Int
    ): String {
        return try {
            // Check if work is scheduled (synchronous check)
            val workManager = WorkManager.getInstance(context)
            // Note: We'll assume work is scheduled if enabled (check is done in ViewModel)
            
            
            // Calculate next notification time
            val calendar = Calendar.getInstance()
            val now = System.currentTimeMillis()
            
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            // If target time has already passed today, schedule for tomorrow
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            
            val timeString = timeFormat.format(calendar.time)
            val dateString = dateFormat.format(calendar.time)
            
            val nowCal = Calendar.getInstance()
            if (calendar.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)) {
                // Same day
                return "Today at $timeString"
            } else {
                return "$dateString at $timeString"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting next scheduled time: ${e.message}", e)
            "Error: ${e.message}"
        }
    }
    
    /**
     * Get WorkInfo state as human-readable string for debugging
     * 
     * @param context Application context
     * @return String describing current work state
     */
    suspend fun getWorkState(context: Context): String {
        return try {
            val workManager = WorkManager.getInstance(context)
            val workInfosFuture = workManager.getWorkInfosForUniqueWork(DailyQuoteWorker.WORK_NAME)
            
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
            
            val workInfo = workInfos.firstOrNull()
            if (workInfo != null) {
                "State: ${workInfo.state}, Run Attempt Count: ${workInfo.runAttemptCount}"
            } else {
                "No work scheduled"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting work state: ${e.message}", e)
            "Error: ${e.message}"
        }
    }
}
