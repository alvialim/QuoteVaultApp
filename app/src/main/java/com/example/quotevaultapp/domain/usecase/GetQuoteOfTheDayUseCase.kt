package com.example.quotevaultapp.domain.usecase

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseQuoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Calendar
import android.util.Log

/**
 * DataStore extension property for quote of the day cache
 */
private val Context.quoteOfTheDayDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "quote_of_the_day_cache",
    scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
)

/**
 * Use case for getting quote of the day
 * Implements caching logic using DataStore to avoid fetching multiple times per day
 */
class GetQuoteOfTheDayUseCase(
    private val context: Context,
    private val quoteRepository: QuoteRepository = SupabaseQuoteRepository()
) {
    
    companion object {
        private const val TAG = "GetQuoteOfTheDayUseCase"
        
        // Preference keys for caching
        private val CACHED_QUOTE_KEY = stringPreferencesKey("cached_quote")
        private val CACHED_DATE_KEY = longPreferencesKey("cached_date")
        
        // JSON serializer for Quote caching
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }
    }
    
    /**
     * Get quote of the day with intelligent caching
     * - Returns cached quote if available for today
     * - Fetches new quote if cache is invalid or missing
     * - Handles errors gracefully with fallback to cache
     * 
     * @return Result containing today's quote or error
     */
    suspend operator fun invoke(): Result<Quote> {
        return try {
            val todayDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR).toLong()
            
            // Check cache first
            val cachedQuote = getCachedQuote()
            val cachedDate = getCachedDate()
            
            if (cachedQuote != null && cachedDate == todayDayOfYear) {
                Log.d(TAG, "Returning cached quote of the day for day $todayDayOfYear")
                return Result.Success(cachedQuote)
            }
            
            Log.d(TAG, "Cache miss or expired, fetching new quote of the day...")
            
            // Fetch from repository
            val result = quoteRepository.getQuoteOfTheDay()
            
            when (result) {
                is Result.Success -> {
                    // Cache the quote for today
                    cacheQuote(result.data, todayDayOfYear)
                    Log.d(TAG, "Successfully fetched and cached quote of the day")
                    result
                }
                is Result.Error -> {
                    Log.e(TAG, "Error fetching quote of the day: ${result.exception.message}")
                    
                    // If we have a stale cache, return it as fallback
                    if (cachedQuote != null) {
                        Log.w(TAG, "Using stale cached quote due to fetch error")
                        return Result.Success(cachedQuote)
                    }
                    
                    // No cache available, return error
                    result
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in getQuoteOfTheDay: ${e.message}", e)
            
            // Try to return cached quote as last resort
            val cachedQuote = getCachedQuote()
            if (cachedQuote != null) {
                Log.w(TAG, "Using cached quote as fallback due to exception")
                return Result.Success(cachedQuote)
            }
            
            Result.Error(e)
        }
    }
    
    /**
     * Get cached quote from DataStore
     */
    private suspend fun getCachedQuote(): Quote? {
        return try {
            val cachedJson = context.quoteOfTheDayDataStore.data
                .map { it[CACHED_QUOTE_KEY] }
                .first()
            
            cachedJson?.let { jsonString ->
                try {
                    // Deserialize Quote from JSON
                    json.decodeFromString<CachedQuote>(jsonString).toQuote()
                } catch (e: Exception) {
                    Log.e(TAG, "Error deserializing cached quote: ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading cached quote: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get cached date from DataStore
     */
    private suspend fun getCachedDate(): Long? {
        return try {
            context.quoteOfTheDayDataStore.data
                .map { it[CACHED_DATE_KEY] }
                .first()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading cached date: ${e.message}", e)
            null
        }
    }
    
    /**
     * Cache quote in DataStore with timestamp
     */
    private suspend fun cacheQuote(quote: Quote, dayOfYear: Long) {
        try {
            context.quoteOfTheDayDataStore.edit { preferences ->
                // Serialize Quote to JSON
                val cachedQuote = CachedQuote.fromQuote(quote)
                val jsonString = json.encodeToString(CachedQuote.serializer(), cachedQuote)
                
                preferences[CACHED_QUOTE_KEY] = jsonString
                preferences[CACHED_DATE_KEY] = dayOfYear
            }
            Log.d(TAG, "Quote cached successfully for day $dayOfYear")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching quote: ${e.message}", e)
            // Don't throw - caching failure shouldn't break the use case
        }
    }
    
    /**
     * Clear the cache (useful for testing or manual refresh)
     */
    suspend fun clearCache() {
        try {
            context.quoteOfTheDayDataStore.edit { preferences ->
                preferences.remove(CACHED_QUOTE_KEY)
                preferences.remove(CACHED_DATE_KEY)
            }
            Log.d(TAG, "Quote of the day cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache: ${e.message}", e)
        }
    }
}

/**
 * Serializable representation of Quote for DataStore caching
 */
@Serializable
private data class CachedQuote(
    val id: String,
    val text: String,
    val author: String,
    val category: String,
    val createdAt: Long,
    val isFavorite: Boolean = false
) {
    fun toQuote(): Quote {
        return Quote(
            id = id,
            text = text,
            author = author,
            category = com.example.quotevaultapp.domain.model.QuoteCategory.valueOf(category),
            createdAt = createdAt,
            isFavorite = isFavorite
        )
    }
    
    companion object {
        fun fromQuote(quote: Quote): CachedQuote {
            return CachedQuote(
                id = quote.id,
                text = quote.text,
                author = quote.author,
                category = quote.category.name,
                createdAt = quote.createdAt,
                isFavorite = quote.isFavorite
            )
        }
    }
}