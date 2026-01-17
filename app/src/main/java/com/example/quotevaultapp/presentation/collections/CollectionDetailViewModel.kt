package com.example.quotevaultapp.presentation.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.domain.model.Collection
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.CollectionRepository
import com.example.quotevaultapp.domain.repository.QuoteRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseCollectionRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseQuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Sealed class representing the collection detail screen UI state
 */
sealed class CollectionDetailUiState {
    data object Idle : CollectionDetailUiState()
    data object Loading : CollectionDetailUiState()
    data object Success : CollectionDetailUiState()
    data class Error(val message: String) : CollectionDetailUiState()
}

/**
 * ViewModel for Collection Detail screen
 * Manages collection details, quotes in collection, and collection operations
 */
class CollectionDetailViewModel(
    private val collectionRepository: CollectionRepository = SupabaseCollectionRepository(),
    private val quoteRepository: QuoteRepository = SupabaseQuoteRepository()
) : ViewModel() {
    
    // Collection data
    private val _collection = MutableStateFlow<Collection?>(null)
    val collection: StateFlow<Collection?> = _collection.asStateFlow()
    
    // Quotes in collection
    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()
    
    // Refreshing state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // UI State
    private val _uiState = MutableStateFlow<CollectionDetailUiState>(CollectionDetailUiState.Idle)
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()
    
    // Dialog states
    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()
    
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()
    
    private val _showAddQuoteDialog = MutableStateFlow(false)
    val showAddQuoteDialog: StateFlow<Boolean> = _showAddQuoteDialog.asStateFlow()
    
    // Quotes for add quote dialog
    private val _availableQuotes = MutableStateFlow<List<Quote>>(emptyList())
    val availableQuotes: StateFlow<List<Quote>> = _availableQuotes.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoadingQuotes = MutableStateFlow(false)
    val isLoadingQuotes: StateFlow<Boolean> = _isLoadingQuotes.asStateFlow()
    
    // Expose error and loading for convenience
    val error: String?
        get() = (_uiState.value as? CollectionDetailUiState.Error)?.message
    
    val isLoading: Boolean
        get() = _uiState.value is CollectionDetailUiState.Loading
    
    // Store collectionId for operations
    private var currentCollectionId: String? = null
    
    /**
     * Load collection and its quotes
     */
    fun loadCollection(collectionId: String) {
        currentCollectionId = collectionId
        _uiState.value = CollectionDetailUiState.Loading
        
        viewModelScope.launch {
            // Load collection quotes
            val quotesResult = collectionRepository.getCollectionQuotes(collectionId)
            
            when (quotesResult) {
                is Result.Success -> {
                    val quotes = quotesResult.data
                    _quotes.value = quotes
                    
                    // Try to find collection from collections list or create a placeholder
                    // In a real implementation, you might have a getCollectionById method
                    // For now, we'll need to get collection from the collections list
                    // This is a limitation - ideally the repository would have getCollectionById
                    if (_collection.value == null) {
                        // Try to find in collections list (would need access to collections)
                        // For now, create a placeholder that will be updated when collection is loaded
                    }
                    
                    _uiState.value = CollectionDetailUiState.Success
                }
                is Result.Error -> {
                    val errorMsg = quotesResult.exception.message 
                        ?: "Failed to load collection"
                    _uiState.value = CollectionDetailUiState.Error(errorMsg)
                }
            }
        }
    }
    
    /**
     * Set collection data (called from UI when navigating with collection object)
     */
    fun setCollection(collection: Collection) {
        _collection.value = collection
        currentCollectionId = collection.id
    }
    
    /**
     * Refresh collection (pull to refresh)
     */
    fun refreshCollection() {
        val collectionId = _collection.value?.id ?: return
        _isRefreshing.value = true
        
        viewModelScope.launch {
            try {
                loadCollection(collectionId)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Add a quote to the collection
     */
    fun addQuote(quoteId: String) {
        val collectionId = currentCollectionId ?: _collection.value?.id ?: return
        
        viewModelScope.launch {
            val result = collectionRepository.addQuoteToCollection(collectionId, quoteId)
            
            when (result) {
                is Result.Success -> {
                    // Reload quotes to reflect changes
                    loadCollection(collectionId)
                    _showAddQuoteDialog.value = false
                }
                is Result.Error -> {
                    _uiState.value = CollectionDetailUiState.Error(
                        result.exception.message ?: "Failed to add quote"
                    )
                }
            }
        }
    }
    
    /**
     * Remove a quote from the collection
     */
    fun removeQuote(quoteId: String) {
        val collectionId = currentCollectionId ?: _collection.value?.id ?: return
        
        viewModelScope.launch {
            val result = collectionRepository.removeQuoteFromCollection(collectionId, quoteId)
            
            when (result) {
                is Result.Success -> {
                    // Update local list immediately for better UX
                    _quotes.value = _quotes.value.filter { it.id != quoteId }
                    
                    // Update collection quoteIds
                    _collection.value?.let { collection ->
                        _collection.value = collection.copy(
                            quoteIds = collection.quoteIds - quoteId
                        )
                    }
                }
                is Result.Error -> {
                    // Reload on error to get correct state
                    loadCollection(collectionId)
                    _uiState.value = CollectionDetailUiState.Error(
                        result.exception.message ?: "Failed to remove quote"
                    )
                }
            }
        }
    }
    
    /**
     * Update collection name and description
     */
    fun updateCollection(name: String, description: String?) {
        // Note: This would require an updateCollection method in the repository
        // For now, this is a placeholder implementation
        viewModelScope.launch {
            _collection.value?.let { collection ->
                _collection.value = collection.copy(
                    name = name,
                    description = description
                )
                _showEditDialog.value = false
            }
        }
    }
    
    /**
     * Delete the collection
     */
    fun deleteCollection() {
        val collectionId = currentCollectionId ?: _collection.value?.id ?: return
        
        viewModelScope.launch {
            val result = collectionRepository.deleteCollection(collectionId)
            
            when (result) {
                is Result.Success -> {
                    _showDeleteDialog.value = false
                    // Navigation is handled in the UI
                }
                is Result.Error -> {
                    _uiState.value = CollectionDetailUiState.Error(
                        result.exception.message ?: "Failed to delete collection"
                    )
                }
            }
        }
    }
    
    /**
     * Show edit collection dialog
     */
    fun showEditCollection() {
        _showEditDialog.value = true
    }
    
    /**
     * Hide edit collection dialog
     */
    fun hideEditDialog() {
        _showEditDialog.value = false
    }
    
    /**
     * Show delete collection dialog
     */
    fun showDeleteCollection() {
        _showDeleteDialog.value = true
    }
    
    /**
     * Hide delete collection dialog
     */
    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }
    
    /**
     * Show add quote search dialog
     */
    fun showAddQuoteSearch() {
        _showAddQuoteDialog.value = true
        _searchQuery.value = ""
        _availableQuotes.value = emptyList() // Clear previous results
        loadAvailableQuotes()
    }
    
    /**
     * Load available quotes for adding to collection
     */
    fun loadAvailableQuotes() {
        viewModelScope.launch {
            _isLoadingQuotes.value = true
            val result = quoteRepository.getQuotes(1, 100) // Load first 100 quotes
            when (result) {
                is Result.Success -> {
                    // Filter out quotes already in collection
                    val collectionId = currentCollectionId ?: _collection.value?.id
                    if (collectionId != null) {
                        val collectionQuotes = collectionRepository.getCollectionQuotes(collectionId)
                        if (collectionQuotes is Result.Success) {
                            val existingQuoteIds = collectionQuotes.data.map { it.id }.toSet()
                            _availableQuotes.value = result.data.filter { it.id !in existingQuoteIds }
                        } else {
                            _availableQuotes.value = result.data
                        }
                    } else {
                        _availableQuotes.value = result.data
                    }
                }
                is Result.Error -> {
                    _availableQuotes.value = emptyList()
                }
            }
            _isLoadingQuotes.value = false
        }
    }
    
    /**
     * Search quotes with debouncing to avoid too many API calls
     */
    fun searchQuotes(query: String) {
        _searchQuery.value = query
        
        // Trigger debounced search automatically
        viewModelScope.launch {
            delay(500) // 500ms debounce
            // Check again if the query matches (might have changed during delay)
            val currentQuery = _searchQuery.value
            if (currentQuery == query) {
                android.util.Log.d("CollectionDetailViewModel", "Performing search for: $query")
                performQuoteSearch(query)
            }
        }
    }
    
    /**
     * Internal method to perform the actual search
     */
    private fun performQuoteSearch(query: String) {
        val trimmedQuery = query.trim()
        
        if (trimmedQuery.isBlank()) {
            // If query is empty, reload all available quotes
            android.util.Log.d("CollectionDetailViewModel", "Query is empty, loading all available quotes")
            loadAvailableQuotes()
            return
        }
        
        viewModelScope.launch {
            _isLoadingQuotes.value = true
            android.util.Log.d("CollectionDetailViewModel", "Searching quotes with query: $trimmedQuery")
            
            // Use searchQuotesWithCategory if available (from SupabaseQuoteRepository)
            val repository = quoteRepository as? SupabaseQuoteRepository
            val result = if (repository != null) {
                // Search without category filter for add quote dialog
                repository.searchQuotesWithCategory(trimmedQuery, null)
            } else {
                quoteRepository.searchQuotes(trimmedQuery)
            }
            
            when (result) {
                is Result.Success -> {
                    android.util.Log.d("CollectionDetailViewModel", "Search returned ${result.data.size} quotes")
                    
                    // Filter out quotes already in collection
                    val collectionId = currentCollectionId ?: _collection.value?.id
                    if (collectionId != null) {
                        val collectionQuotesResult = collectionRepository.getCollectionQuotes(collectionId)
                        if (collectionQuotesResult is Result.Success) {
                            val existingQuoteIds = collectionQuotesResult.data.map { it.id }.toSet()
                            val filteredQuotes = result.data.filter { it.id !in existingQuoteIds }
                            android.util.Log.d("CollectionDetailViewModel", "After filtering collection quotes: ${filteredQuotes.size} quotes")
                            _availableQuotes.value = filteredQuotes
                        } else {
                            _availableQuotes.value = result.data
                        }
                    } else {
                        _availableQuotes.value = result.data
                    }
                }
                is Result.Error -> {
                    android.util.Log.e("CollectionDetailViewModel", "Error searching quotes: ${result.exception.message}", result.exception)
                    _availableQuotes.value = emptyList()
                }
            }
            _isLoadingQuotes.value = false
        }
    }
    
    /**
     * Hide add quote dialog
     */
    fun hideAddQuoteDialog() {
        _showAddQuoteDialog.value = false
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        if (_uiState.value is CollectionDetailUiState.Error) {
            _uiState.value = CollectionDetailUiState.Success
        }
    }
}
