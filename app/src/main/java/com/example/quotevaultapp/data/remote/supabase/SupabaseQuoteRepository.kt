package com.example.quotevaultapp.data.remote.supabase

import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import com.example.quotevaultapp.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.util.Calendar

/**
 * Supabase implementation of QuoteRepository
 * Handles quote operations using Supabase Postgrest
 */
class SupabaseQuoteRepository : QuoteRepository {
    
    private val supabaseClient: SupabaseClient by lazy {
        require(BuildConfig.SUPABASE_URL.isNotEmpty()) {
            "SUPABASE_URL must be configured in local.properties"
        }
        require(BuildConfig.SUPABASE_ANON_KEY.isNotEmpty()) {
            "SUPABASE_ANON_KEY must be configured in local.properties"
        }
        
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(io.github.jan.supabase.gotrue.Auth)
            install(io.github.jan.supabase.postgrest.Postgrest)
            install(io.github.jan.supabase.realtime.Realtime)
        }
    }
    
    private val favoritesFlow = MutableStateFlow<List<Quote>>(emptyList())
    
    init {
        // Load initial favorites
        loadFavorites()
    }
    
    override suspend fun getQuotes(page: Int, pageSize: Int): Result<List<Quote>> {
        return try {
            val offset = (page - 1) * pageSize
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            
            val quoteEntities = supabaseClient.postgrest.from("quotes")
                .select {
                    order("created_at", ascending = false)
                    limit(pageSize)
                    range(offset, offset + pageSize - 1)
                }
                .decodeList<QuoteEntity>()
            
            val quotes = quoteEntities.map { entity ->
                val isFavorite = userId?.let { checkIfFavorite(it, entity.id) } ?: false
                mapToDomainQuote(entity, isFavorite)
            }
            
            Result.Success(quotes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getQuotesByCategory(
        category: QuoteCategory,
        page: Int
    ): Result<List<Quote>> {
        return try {
            val pageSize = 20
            val offset = (page - 1) * pageSize
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            
            val quoteEntities = supabaseClient.postgrest.from("quotes")
                .select {
                    filter {
                        eq("category", category.name)
                    }
                    order("created_at", ascending = false)
                    limit(pageSize)
                    range(offset, offset + pageSize - 1)
                }
                .decodeList<QuoteEntity>()
            
            val quotes = quoteEntities.map { entity ->
                val isFavorite = userId?.let { checkIfFavorite(it, entity.id) } ?: false
                mapToDomainQuote(entity, isFavorite)
            }
            
            Result.Success(quotes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun searchQuotes(query: String): Result<List<Quote>> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            
            val quoteEntities = supabaseClient.postgrest.from("quotes")
                .select {
                    filter {
                        or {
                            ilike("text", "%$query%")
                            ilike("author", "%$query%")
                        }
                    }
                    order("created_at", ascending = false)
                    limit(50)
                }
                .decodeList<QuoteEntity>()
            
            val quotes = quoteEntities.map { entity ->
                val isFavorite = userId?.let { checkIfFavorite(it, entity.id) } ?: false
                mapToDomainQuote(entity, isFavorite)
            }
            
            Result.Success(quotes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun searchByAuthor(author: String): Result<List<Quote>> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            
            val quoteEntities = supabaseClient.postgrest.from("quotes")
                .select {
                    filter {
                        ilike("author", "%$author%")
                    }
                    order("created_at", ascending = false)
                    limit(50)
                }
                .decodeList<QuoteEntity>()
            
            val quotes = quoteEntities.map { entity ->
                val isFavorite = userId?.let { checkIfFavorite(it, entity.id) } ?: false
                mapToDomainQuote(entity, isFavorite)
            }
            
            Result.Success(quotes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getQuoteOfTheDay(): Result<Quote> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            
            // Use day of year to get a consistent quote for the day
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            
            // Get all quotes and use modulo to select one consistently per day
            // For better performance, we could cache the total count
            val allQuotes = supabaseClient.postgrest.from("quotes")
                .select {
                    order("created_at", ascending = true)
                }
                .decodeList<QuoteEntity>()
            
            if (allQuotes.isEmpty()) {
                return Result.Error(Exception("No quotes available"))
            }
            
            // Use modulo to get a consistent quote index for the day
            val quoteIndex = dayOfYear % allQuotes.size
            val entity = allQuotes[quoteIndex]
            
            val isFavorite = userId?.let { checkIfFavorite(it, entity.id) } ?: false
            val quote = mapToDomainQuote(entity, isFavorite)
            
            Result.Success(quote)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun addToFavorites(quoteId: String): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.Error(Exception("User not authenticated"))
            
            supabaseClient.postgrest.from("user_favorites")
                .insert(
                    FavoriteEntity(
                        user_id = userId,
                        quote_id = quoteId
                    )
                )
            
            // Reload favorites
            loadFavorites()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun removeFromFavorites(quoteId: String): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.Error(Exception("User not authenticated"))
            
            supabaseClient.postgrest.from("user_favorites")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("quote_id", quoteId)
                    }
                }
            
            // Reload favorites
            loadFavorites()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun getFavorites(): Flow<List<Quote>> {
        return favoritesFlow.asStateFlow()
    }
    
    /**
     * Load user's favorites from database
     */
    private suspend fun loadFavorites() {
        try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: run {
                favoritesFlow.value = emptyList()
                return
            }
            
            // Get favorite quote IDs
            val favoriteIds = supabaseClient.postgrest.from("user_favorites")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<FavoriteEntity>()
                .map { it.quote_id }
            
            if (favoriteIds.isEmpty()) {
                favoritesFlow.value = emptyList()
                return
            }
            
            // Get quote details for favorite IDs
            val quoteEntities = supabaseClient.postgrest.from("quotes")
                .select {
                    filter {
                        `in`("id", favoriteIds)
                    }
                    order("created_at", ascending = false)
                }
                .decodeList<QuoteEntity>()
            
            val favorites = quoteEntities.map { entity ->
                mapToDomainQuote(entity, isFavorite = true)
            }
            
            favoritesFlow.value = favorites
        } catch (e: Exception) {
            favoritesFlow.value = emptyList()
        }
    }
    
    /**
     * Check if a quote is in user's favorites
     */
    private suspend fun checkIfFavorite(userId: String, quoteId: String): Boolean {
        return try {
            val favorites = supabaseClient.postgrest.from("user_favorites")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("quote_id", quoteId)
                    }
                    limit(1)
                }
                .decodeList<FavoriteEntity>()
            
            favorites.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Map database entity to domain model
     */
    private fun mapToDomainQuote(entity: QuoteEntity, isFavorite: Boolean): Quote {
        return Quote(
            id = entity.id,
            text = entity.text,
            author = entity.author,
            category = QuoteCategory.valueOf(entity.category),
            createdAt = parseTimestamp(entity.created_at),
            isFavorite = isFavorite
        )
    }
    
    /**
     * Parse ISO 8601 timestamp string to milliseconds
     */
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            java.time.Instant.parse(timestamp).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    /**
     * Database entity for quotes table
     */
    @Serializable
    private data class QuoteEntity(
        val id: String,
        val text: String,
        val author: String,
        val category: String,
        val created_at: String
    )
    
    /**
     * Database entity for user_favorites table
     */
    @Serializable
    private data class FavoriteEntity(
        val user_id: String,
        val quote_id: String
    )
    
}
