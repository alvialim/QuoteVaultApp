package com.example.quotevaultapp.domain.usecase

import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseQuoteRepository

/**
 * Use case for getting quotes
 * Encapsulates the business logic for retrieving quotes
 */
class GetQuotesUseCase(
    private val quoteRepository: QuoteRepository = SupabaseQuoteRepository()
) {
    /**
     * Get paginated quotes
     * 
     * @param page Page number (1-indexed)
     * @param pageSize Number of quotes per page
     * @return Result containing list of quotes or error
     */
    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = 20
    ): Result<List<Quote>> {
        return quoteRepository.getQuotes(page, pageSize)
    }
    
    /**
     * Get quotes by category
     * 
     * @param category Quote category to filter by
     * @param page Page number (1-indexed)
     * @return Result containing list of quotes or error
     */
    suspend fun getByCategory(
        category: QuoteCategory,
        page: Int = 1
    ): Result<List<Quote>> {
        return quoteRepository.getQuotesByCategory(category, page)
    }
    
    /**
     * Search quotes by text
     * 
     * @param query Search query
     * @return Result containing list of matching quotes or error
     */
    suspend fun searchQuotes(query: String): Result<List<Quote>> {
        return quoteRepository.searchQuotes(query)
    }
    
    /**
     * Search quotes by author
     * 
     * @param author Author name to search for
     * @return Result containing list of matching quotes or error
     */
    suspend fun searchByAuthor(author: String): Result<List<Quote>> {
        return quoteRepository.searchByAuthor(author)
    }
    
    /**
     * Get quote of the day
     * 
     * @return Result containing quote of the day or error
     */
    suspend fun getQuoteOfTheDay(): Result<Quote> {
        return quoteRepository.getQuoteOfTheDay()
    }
}
