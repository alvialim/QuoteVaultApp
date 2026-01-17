package com.example.quotevaultapp.workers

/**
 * DailyQuoteWorker - TEMPORARILY DISABLED (requires Hilt)
 * 
 * This worker is disabled because it uses @HiltWorker annotation.
 * To re-enable:
 * 1. Re-add Hilt dependencies
 * 2. Uncomment the code below
 * 3. Update QuoteVaultApplication to use HiltWorkerFactory
 */

/*
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseQuoteRepository
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
    
    override suspend fun doWork(): Result {
        return try {
            // Fetch quote of the day
            val result = quoteRepository.getQuoteOfTheDay()
            
            when (result) {
                is com.example.quotevaultapp.domain.model.Result.Success -> {
                    val quote = result.data
                    
                    // Show notification with quote
                    NotificationHelper.showDailyQuoteNotification(
                        context = applicationContext,
                        quote = quote
                    )
                    
                    Result.success()
                }
                is com.example.quotevaultapp.domain.model.Result.Error -> {
                    // Log error but don't fail - try again next day
                    android.util.Log.e(
                        "DailyQuoteWorker",
                        "Failed to fetch quote of the day: ${result.exception.message}",
                        result.exception
                    )
                    
                    // Retry if transient error, otherwise success to avoid infinite retries
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "DailyQuoteWorker",
                "Error in DailyQuoteWorker: ${e.message}",
                e
            )
            
            // Retry on exception
            Result.retry()
        }
    }
    
    companion object {
        /**
         * Unique work name for daily quote worker
         */
        const val WORK_NAME = "daily_quote_work"
    }
}
*/