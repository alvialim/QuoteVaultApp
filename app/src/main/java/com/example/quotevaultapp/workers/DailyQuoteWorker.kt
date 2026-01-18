package com.example.quotevaultapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.quotevaultapp.data.remote.supabase.SupabaseQuoteRepository
import com.example.quotevaultapp.domain.model.Result as DomainResult
import com.example.quotevaultapp.domain.repository.QuoteRepository
import com.example.quotevaultapp.util.NotificationHelper

/**
 * WorkManager worker that fetches quote of the day and shows notification
 * Runs daily at the user's preferred time
 */
class DailyQuoteWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val quoteRepository: QuoteRepository = SupabaseQuoteRepository()
    private val notificationHelper: NotificationHelper = NotificationHelper(context)
    
    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            Log.d(TAG, "DailyQuoteWorker: Starting work to fetch quote of the day")
            
            // Fetch quote of the day
            val result = quoteRepository.getQuoteOfTheDay()
            
            when (result) {
                is DomainResult.Success -> {
                    val quote = result.data
                    Log.d(TAG, "DailyQuoteWorker: Successfully fetched quote: ${quote.id}")
                    
                    // Show notification with quote
                    notificationHelper.showDailyQuoteNotification(quote)
                    
                    Log.d(TAG, "DailyQuoteWorker: Notification shown successfully")
                    ListenableWorker.Result.success()
                }
                is DomainResult.Error -> {
                    // Log error but don't fail - try again next day
                    Log.e(
                        TAG,
                        "Failed to fetch quote of the day: ${result.exception.message}",
                        result.exception
                    )
                    
                    // Retry if transient error, otherwise success to avoid infinite retries
                    // Use retry() for transient errors, success() for permanent errors
                    ListenableWorker.Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error in DailyQuoteWorker: ${e.message}",
                e
            )
            
            // Retry on exception (might be network issue)
            ListenableWorker.Result.retry()
        }
    }
    
    companion object {
        /**
         * Unique work name for daily quote worker
         */
        const val WORK_NAME = "daily_quote_work"
        
        private const val TAG = "DailyQuoteWorker"
    }
}
