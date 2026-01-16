package com.example.quotevaultapp.domain.repository

import com.example.quotevaultapp.domain.model.Collection
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for collection operations
 * Defines the contract for managing user-created quote collections
 */
interface CollectionRepository {
    
    /**
     * Create a new collection
     * @param name Collection name
     * @param description Optional collection description
     * @return Result containing the created Collection
     */
    suspend fun createCollection(name: String, description: String?): Result<Collection>
    
    /**
     * Get all collections for the current user as a Flow (reactive)
     * @return Flow that emits list of collections, updates when collections change
     */
    fun getCollections(): Flow<List<Collection>>
    
    /**
     * Add a quote to a collection
     * @param collectionId The ID of the collection
     * @param quoteId The ID of the quote to add
     * @return Result<Unit> indicating success or failure
     */
    suspend fun addQuoteToCollection(collectionId: String, quoteId: String): Result<Unit>
    
    /**
     * Remove a quote from a collection
     * @param collectionId The ID of the collection
     * @param quoteId The ID of the quote to remove
     * @return Result<Unit> indicating success or failure
     */
    suspend fun removeQuoteFromCollection(collectionId: String, quoteId: String): Result<Unit>
    
    /**
     * Get all quotes in a collection
     * @param collectionId The ID of the collection
     * @return Result containing list of quotes in the collection
     */
    suspend fun getCollectionQuotes(collectionId: String): Result<List<Quote>>
    
    /**
     * Delete a collection
     * @param collectionId The ID of the collection to delete
     * @return Result<Unit> indicating success or failure
     */
    suspend fun deleteCollection(collectionId: String): Result<Unit>
}
