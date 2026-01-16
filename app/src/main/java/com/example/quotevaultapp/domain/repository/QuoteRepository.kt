package com.example.quotevaultapp.domain.repository

import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for quote operations
 * Defines the contract for quote retrieval, search, and favorites management
 */
interface QuoteRepository {
    
    /**
     * Get paginated quotes
     * @param page Page number (1-indexed)
     * @param pageSize Number of quotes per page
     * @return Result containing list of quotes
     */
    suspend fun getQuotes(page: Int, pageSize: Int): Result<List<Quote>>
    
    /**
     * Get quotes filtered by category with pagination
     * @param category The quote category to filter by
     * @param page Page number (1-indexed)
     * @return Result containing list of quotes in the specified category
     */
    suspend fun getQuotesByCategory(category: QuoteCategory, page: Int): Result<List<Quote>>
    
    /**
     * Search quotes by text content
     * @param query Search query string
     * @return Result containing list of matching quotes
     */
    suspend fun searchQuotes(query: String): Result<List<Quote>>
    
    /**
     * Search quotes by author name
     * @param author Author name to search for
     * @return Result containing list of quotes by the author
     */
    suspend fun searchByAuthor(author: String): Result<List<Quote>>
    
    /**
     * Get quote of the day (typically a random quote)
     * @return Result containing the quote of the day
     */
    suspend fun getQuoteOfTheDay(): Result<Quote>
    
    /**
     * Add a quote to user's favorites
     * @param quoteId The ID of the quote to favorite
     * @return Result<Unit> indicating success or failure
     */
    suspend fun addToFavorites(quoteId: String): Result<Unit>
    
    /**
     * Remove a quote from user's favorites
     * @param quoteId The ID of the quote to unfavorite
     * @return Result<Unit> indicating success or failure
     */
    suspend fun removeFromFavorites(quoteId: String): Result<Unit>
    
    /**
     * Get user's favorite quotes as a Flow (reactive)
     * @return Flow that emits list of favorite quotes, updates when favorites change
     */
    fun getFavorites(): Flow<List<Quote>>
}
