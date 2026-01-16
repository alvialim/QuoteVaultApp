package com.example.quotevaultapp.data.remote.supabase

import com.example.quotevaultapp.domain.model.Collection
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.CollectionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import com.example.quotevaultapp.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Supabase implementation of CollectionRepository
 * Handles collection operations using Supabase Postgrest
 */
class SupabaseCollectionRepository : CollectionRepository {
    
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
    
    private val collectionsFlow = MutableStateFlow<List<Collection>>(emptyList())
    
    init {
        // Load initial collections
        loadCollections()
    }
    
    override suspend fun createCollection(
        name: String,
        description: String?
    ): Result<Collection> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.Error(Exception("User not authenticated"))
            
            val insertData = buildMap<String, Any?> {
                put("user_id", userId)
                put("name", name)
                if (description != null) {
                    put("description", description)
                }
            }
            
            val created = supabaseClient.postgrest.from("collections")
                .insert(insertData) {
                    select()
                }
                .decodeSingle<CollectionEntity>()
            
            val collection = mapToDomainCollection(created)
            
            // Reload collections
            loadCollections()
            
            Result.Success(collection)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun getCollections(): Flow<List<Collection>> {
        return collectionsFlow.asStateFlow()
    }
    
    override suspend fun addQuoteToCollection(
        collectionId: String,
        quoteId: String
    ): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.Error(Exception("User not authenticated"))
            
            // Verify collection belongs to user
            val collection = supabaseClient.postgrest.from("collections")
                .select {
                    filter {
                        eq("id", collectionId)
                        eq("user_id", userId)
                    }
                    limit(1)
                }
                .decodeList<CollectionEntity>()
            
            if (collection.isEmpty()) {
                return Result.Error(Exception("Collection not found or access denied"))
            }
            
            // Add quote to collection
            supabaseClient.postgrest.from("collection_quotes")
                .insert(
                    CollectionQuoteEntity(
                        collection_id = collectionId,
                        quote_id = quoteId
                    )
                )
            
            // Reload collections to update quoteIds
            loadCollections()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun removeQuoteFromCollection(
        collectionId: String,
        quoteId: String
    ): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.Error(Exception("User not authenticated"))
            
            // Verify collection belongs to user
            val collection = supabaseClient.postgrest.from("collections")
                .select {
                    filter {
                        eq("id", collectionId)
                        eq("user_id", userId)
                    }
                    limit(1)
                }
                .decodeList<CollectionEntity>()
            
            if (collection.isEmpty()) {
                return Result.Error(Exception("Collection not found or access denied"))
            }
            
            // Remove quote from collection
            supabaseClient.postgrest.from("collection_quotes")
                .delete {
                    filter {
                        eq("collection_id", collectionId)
                        eq("quote_id", quoteId)
                    }
                }
            
            // Reload collections to update quoteIds
            loadCollections()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getCollectionQuotes(collectionId: String): Result<List<Quote>> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.Error(Exception("User not authenticated"))
            
            // Verify collection belongs to user
            val collection = supabaseClient.postgrest.from("collections")
                .select {
                    filter {
                        eq("id", collectionId)
                        eq("user_id", userId)
                    }
                    limit(1)
                }
                .decodeList<CollectionEntity>()
            
            if (collection.isEmpty()) {
                return Result.Error(Exception("Collection not found or access denied"))
            }
            
            // Get quote IDs in collection
            val collectionQuotes = supabaseClient.postgrest.from("collection_quotes")
                .select {
                    filter {
                        eq("collection_id", collectionId)
                    }
                }
                .decodeList<CollectionQuoteEntity>()
            
            if (collectionQuotes.isEmpty()) {
                return Result.Success(emptyList())
            }
            
            val quoteIds = collectionQuotes.map { it.quote_id }
            
            // Get quote details
            val quoteEntities = supabaseClient.postgrest.from("quotes")
                .select {
                    filter {
                        `in`("id", quoteIds)
                    }
                    order("created_at", ascending = false)
                }
                .decodeList<QuoteEntity>()
            
            // Check which quotes are favorites
            val favorites = supabaseClient.postgrest.from("user_favorites")
                .select {
                    filter {
                        eq("user_id", userId)
                        `in`("quote_id", quoteIds)
                    }
                }
                .decodeList<FavoriteEntity>()
                .map { it.quote_id }
                .toSet()
            
            val quotes = quoteEntities.map { entity ->
                mapToDomainQuote(entity, favorites.contains(entity.id))
            }
            
            Result.Success(quotes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun deleteCollection(collectionId: String): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.Error(Exception("User not authenticated"))
            
            // Verify collection belongs to user
            val collection = supabaseClient.postgrest.from("collections")
                .select {
                    filter {
                        eq("id", collectionId)
                        eq("user_id", userId)
                    }
                    limit(1)
                }
                .decodeList<CollectionEntity>()
            
            if (collection.isEmpty()) {
                return Result.Error(Exception("Collection not found or access denied"))
            }
            
            // Delete collection (cascade will delete collection_quotes)
            supabaseClient.postgrest.from("collections")
                .delete {
                    filter {
                        eq("id", collectionId)
                    }
                }
            
            // Reload collections
            loadCollections()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Load all collections for the current user
     */
    private suspend fun loadCollections() {
        try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: run {
                collectionsFlow.value = emptyList()
                return
            }
            
            // Get all collections for user
            val collectionEntities = supabaseClient.postgrest.from("collections")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", ascending = false)
                }
                .decodeList<CollectionEntity>()
            
            // Get quote IDs for each collection
            val collectionsWithQuotes = collectionEntities.map { entity ->
                val quoteIds = supabaseClient.postgrest.from("collection_quotes")
                    .select {
                        filter {
                            eq("collection_id", entity.id)
                        }
                    }
                    .decodeList<CollectionQuoteEntity>()
                    .map { it.quote_id }
                
                mapToDomainCollection(entity, quoteIds)
            }
            
            collectionsFlow.value = collectionsWithQuotes
        } catch (e: Exception) {
            collectionsFlow.value = emptyList()
        }
    }
    
    /**
     * Map database entity to domain model
     */
    private fun mapToDomainCollection(
        entity: CollectionEntity,
        quoteIds: List<String> = emptyList()
    ): Collection {
        return Collection(
            id = entity.id,
            userId = entity.user_id,
            name = entity.name,
            description = entity.description,
            quoteIds = quoteIds,
            createdAt = parseTimestamp(entity.created_at)
        )
    }
    
    /**
     * Map database quote entity to domain model
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
     * Database entity for collections table
     */
    @Serializable
    private data class CollectionEntity(
        val id: String,
        val user_id: String,
        val name: String,
        val description: String?,
        val created_at: String
    )
    
    /**
     * Database entity for collection_quotes table
     */
    @Serializable
    private data class CollectionQuoteEntity(
        val collection_id: String,
        val quote_id: String
    )
    
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
