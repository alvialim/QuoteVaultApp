package com.example.quotevaultapp.data.remote.supabase

import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.realtime
import com.example.quotevaultapp.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
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
    
    // Track current user ID for auth state changes
    private var currentUserId: String? = null
    
    // Cache for quote of the day (in-memory cache for current day)
    private var cachedQuoteOfTheDay: Quote? = null
    private var cachedQuoteOfTheDayDate: Int = -1 // Day of year
    
    init {
        // Load initial favorites in a coroutine scope
        CoroutineScope(Dispatchers.IO).launch {
            loadFavorites()
        }
        
        // Observe auth state changes to reload favorites on login/logout
        observeAuthState()
    }
    
    /**
     * Observe authentication state changes and reload favorites accordingly
     * This ensures favorites are synced when user logs in on different devices
     */
    private fun observeAuthState() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                val newUserId = currentUser?.id
                
                // If user ID changed (login/logout), reload favorites
                if (newUserId != currentUserId) {
                    android.util.Log.d("SupabaseQuoteRepository", "Auth state changed: ${if (newUserId != null) "Logged in" else "Logged out"} (User ID: $newUserId)")
                    
                    if (newUserId != null) {
                        // User logged in - reload favorites from cloud
                        android.util.Log.d("SupabaseQuoteRepository", "User logged in, loading favorites from cloud...")
                        loadFavorites()
                    } else {
                        // User logged out - clear favorites
                        android.util.Log.d("SupabaseQuoteRepository", "User logged out, clearing favorites...")
                        favoritesFlow.value = emptyList()
                    }
                    
                    currentUserId = newUserId
                }
                
                // Check auth state every 2 seconds
                kotlinx.coroutines.delay(2000)
            }
        }
    }
    
    override suspend fun getQuotes(page: Int, pageSize: Int): Result<List<Quote>> {
        return try {
            val offset = (page - 1) * pageSize
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            
            // Order by id (UUID) which is random, ensuring variety across categories
            // This prevents showing only one category when all quotes have the same created_at timestamp
            val quoteEntities = supabaseClient.postgrest.from("quotes")
                .select {
                    order("id", Order.ASCENDING) // Use id for variety (UUIDs are random)
                    limit(pageSize.toLong())
                    range(offset.toLong(), (offset + pageSize - 1).toLong())
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
                    order("created_at", Order.DESCENDING)
                    limit(pageSize.toLong())
                    range(offset.toLong(), (offset + pageSize - 1).toLong())
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
        return searchQuotesWithCategory(query, null)
    }
    
    /**
     * Search quotes with optional category filter
     * @param query Search query string
     * @param category Optional category to filter by
     * @return Result containing list of matching quotes
     */
    suspend fun searchQuotesWithCategory(query: String, category: QuoteCategory?): Result<List<Quote>> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            
            // Build the filter with category if provided
            val quoteEntities = if (category != null) {
                supabaseClient.postgrest.from("quotes")
                    .select {
                        filter {
                            // Filter by category first
                            eq("category", category.name)
                            
                            // Then search in text or author
                            or {
                                ilike("text", "%$query%")
                                ilike("author", "%$query%")
                            }
                        }
                        order("created_at", Order.DESCENDING)
                        limit(100L)
                    }
                    .decodeList<QuoteEntity>()
            } else {
                supabaseClient.postgrest.from("quotes")
                    .select {
                        filter {
                            // Search in text or author
                            or {
                                ilike("text", "%$query%")
                                ilike("author", "%$query%")
                            }
                        }
                        order("created_at", Order.DESCENDING)
                        limit(100L)
                    }
                    .decodeList<QuoteEntity>()
            }
            
            val quotes = quoteEntities.map { entity ->
                val isFavorite = userId?.let { checkIfFavorite(it, entity.id) } ?: false
                mapToDomainQuote(entity, isFavorite)
            }
            
            // Apply intelligent client-side filtering
            val filteredQuotes = filterSearchResults(quotes, query)
            
            Result.Success(filteredQuotes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Intelligent client-side filtering to separate author matches from text matches
     * Prioritizes exact/close author matches over text matches
     * When searching for an author, only shows quotes from that author (not quotes containing author name in text)
     */
    private fun filterSearchResults(quotes: List<Quote>, query: String): List<Quote> {
        val lowerQuery = query.lowercase().trim()
        if (lowerQuery.isEmpty()) return quotes
        
        // Check if query matches a category name
        val matchingCategory = QuoteCategory.values().firstOrNull { 
            val categoryName = it.name.lowercase()
            val categoryDisplay = getCategoryDisplayName(it).lowercase()
            categoryName == lowerQuery || 
            categoryName.contains(lowerQuery) ||
            categoryDisplay == lowerQuery ||
            categoryDisplay.contains(lowerQuery)
        }
        
        // Separate quotes by match type
        val authorMatches = mutableListOf<Quote>()
        val textMatches = mutableListOf<Quote>()
        val categoryMatches = mutableListOf<Quote>()
        
        quotes.forEach { quote ->
            val authorLower = quote.author.lowercase()
            val textLower = quote.text.lowercase()
            
            // First priority: Check for category match (if query matches a category name)
            if (matchingCategory != null && quote.category == matchingCategory) {
                categoryMatches.add(quote)
            }
            // Second priority: Check for author match
            else if (authorLower.contains(lowerQuery)) {
                // Prioritize exact/close matches at the beginning
                if (authorLower == lowerQuery || authorLower.startsWith(lowerQuery)) {
                    authorMatches.add(0, quote) // Add at beginning for exact matches
                } else {
                    authorMatches.add(quote)
                }
            }
            // Third priority: Check for text match (only if NOT an author match)
            else if (textLower.contains(lowerQuery)) {
                textMatches.add(quote)
            }
        }
        
        // Return results in priority order:
        // 1. If query matches a category name -> show only that category
        // 2. If author matches exist -> show ONLY author matches (not text matches)
        // 3. Otherwise -> show text matches
        return when {
            categoryMatches.isNotEmpty() -> categoryMatches
            authorMatches.isNotEmpty() -> {
                // If we found author matches, show ONLY those (don't mix with text matches)
                // This ensures when searching "Einstein", you only see Einstein's quotes
                authorMatches
            }
            textMatches.isNotEmpty() -> textMatches
            else -> emptyList() // Return empty if no matches (not all quotes)
        }
    }
    
    /**
     * Helper function to get display name for category
     */
    private fun getCategoryDisplayName(category: QuoteCategory): String {
        return when (category) {
            QuoteCategory.MOTIVATION -> "Motivation"
            QuoteCategory.LOVE -> "Love"
            QuoteCategory.SUCCESS -> "Success"
            QuoteCategory.WISDOM -> "Wisdom"
            QuoteCategory.HUMOR -> "Humor"
            QuoteCategory.GENERAL -> "General"
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
                    order("created_at", Order.DESCENDING)
                    limit(50L)
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
    
    override suspend fun getQuoteById(quoteId: String): Result<Quote> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            
            val entity = supabaseClient.postgrest.from("quotes")
                .select {
                    filter {
                        eq("id", quoteId)
                    }
                }
                .decodeSingle<QuoteEntity>()
            
            val isFavorite = userId?.let { checkIfFavorite(it, entity.id) } ?: false
            val quote = mapToDomainQuote(entity, isFavorite)
            
            Result.Success(quote)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get quote of the day using deterministic logic
     * Uses day of year modulo total quotes for consistent daily selection
     * Handles edge cases:
     * - Time zone changes: Calendar.getInstance() uses device timezone
     * - Device time changes: Day of year recalculates based on current time
     * - Empty database: Returns error with descriptive message
     * - Network issues: Uses cached quote if available
     */
    override suspend fun getQuoteOfTheDay(): Result<Quote> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            
            // Use day of year to get a consistent quote for the day
            // Edge case: Time zone changes are handled automatically by Calendar.getInstance()
            // Edge case: Device time changes cause dayOfYear to recalculate
            val calendar = Calendar.getInstance()
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            
            // Check if we have a cached quote for today
            if (cachedQuoteOfTheDay != null && cachedQuoteOfTheDayDate == dayOfYear) {
                android.util.Log.d("SupabaseQuoteRepository", "Returning cached quote of the day")
                return Result.Success(cachedQuoteOfTheDay!!)
            }
            
            android.util.Log.d("SupabaseQuoteRepository", "Fetching quote of the day for day $dayOfYear")
            
            // Get all quotes and use modulo to select one consistently per day
            // Using order by id for deterministic ordering
            val allQuotes = supabaseClient.postgrest.from("quotes")
                .select {
                    order("id", Order.ASCENDING) // Deterministic ordering by ID
                }
                .decodeList<QuoteEntity>()
            
            if (allQuotes.isEmpty()) {
                android.util.Log.w("SupabaseQuoteRepository", "No quotes available in database")
                // Return random fallback quote if database is empty
                // For now, return error - in production, you might have default quotes
                return Result.Error(Exception("No quotes available in database"))
            }
            
            // Use modulo to get a consistent quote index for the day
            // Formula: (dayOfYear % totalQuotes) ensures same quote each day
            val quoteIndex = dayOfYear % allQuotes.size
            val entity = allQuotes[quoteIndex]
            
            android.util.Log.d("SupabaseQuoteRepository", "Selected quote index $quoteIndex of ${allQuotes.size} quotes for day $dayOfYear")
            
            val isFavorite = userId?.let { checkIfFavorite(it, entity.id) } ?: false
            val quote = mapToDomainQuote(entity, isFavorite)
            
            // Cache the quote for today
            cachedQuoteOfTheDay = quote
            cachedQuoteOfTheDayDate = dayOfYear
            
            Result.Success(quote)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseQuoteRepository", "Error getting quote of the day: ${e.message}", e)
            // If we have a cached quote, return it even if fetch failed
            if (cachedQuoteOfTheDay != null) {
                android.util.Log.w("SupabaseQuoteRepository", "Using cached quote due to error")
                return Result.Success(cachedQuoteOfTheDay!!)
            }
            Result.Error(e)
        }
    }
    
    override suspend fun addToFavorites(quoteId: String): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.Error(Exception("User not authenticated. Please log in to sync favorites across devices."))
            
            android.util.Log.d("SupabaseQuoteRepository", "Adding quote $quoteId to favorites in cloud for user: $userId")
            
            try {
                // Sync to cloud
                supabaseClient.postgrest.from("user_favorites")
                    .insert(
                        FavoriteEntity(
                            user_id = userId,
                            quote_id = quoteId
                        )
                    )
                
                android.util.Log.d("SupabaseQuoteRepository", "Successfully added favorite to cloud, reloading favorites...")
                
                // Reload favorites from cloud to ensure sync
                loadFavorites()
                
                Result.Success(Unit)
            } catch (insertError: Exception) {
                // If duplicate key error (already favorited), treat as success (idempotent)
                if (insertError.message?.contains("duplicate") == true || 
                    insertError.message?.contains("unique") == true ||
                    insertError.message?.contains("23505") == true) { // PostgreSQL unique violation error code
                    android.util.Log.d("SupabaseQuoteRepository", "Favorite already exists in cloud, reloading to ensure sync")
                    // Reload favorites to ensure sync
                    loadFavorites()
                    Result.Success(Unit)
                } else {
                    android.util.Log.e("SupabaseQuoteRepository", "Error adding favorite to cloud: ${insertError.message}", insertError)
                    throw insertError
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseQuoteRepository", "Error adding to favorites: ${e.message}", e)
            Result.Error(e)
        }
    }
    
    override suspend fun removeFromFavorites(quoteId: String): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.Error(Exception("User not authenticated. Please log in to sync favorites across devices."))
            
            android.util.Log.d("SupabaseQuoteRepository", "Removing quote $quoteId from favorites in cloud for user: $userId")
            
            // Remove from cloud
            supabaseClient.postgrest.from("user_favorites")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("quote_id", quoteId)
                    }
                }
            
            android.util.Log.d("SupabaseQuoteRepository", "Successfully removed favorite from cloud, reloading favorites...")
            
            // Reload favorites from cloud to ensure sync
            loadFavorites()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseQuoteRepository", "Error removing from favorites: ${e.message}", e)
            Result.Error(e)
        }
    }
    
    override fun getFavorites(): Flow<List<Quote>> {
        // Ensure favorites are loaded when Flow is accessed
        // This is important because each repository instance needs to load its own favorites
        CoroutineScope(Dispatchers.IO).launch {
            loadFavorites()
        }
        return favoritesFlow.asStateFlow()
    }
    
    /**
     * Force reload favorites from cloud (cloud sync)
     * Can be called externally to refresh favorites and ensure sync
     */
    suspend fun forceReloadFavorites() {
        android.util.Log.d("SupabaseQuoteRepository", "Force reloading favorites from cloud...")
        loadFavorites()
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
            
            // Get quote details for favorite IDs - fetch all and filter in memory as workaround for 'in' operator
            val allQuoteEntities = if (favoriteIds.isEmpty()) {
                emptyList()
            } else {
                supabaseClient.postgrest.from("quotes")
                    .select {
                        order("created_at", Order.DESCENDING)
                        limit(1000) // Reasonable limit
                    }
                    .decodeList<QuoteEntity>()
            }
            
            val quoteEntities = allQuoteEntities.filter { it.id in favoriteIds }
            
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
                    limit(1L)
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
