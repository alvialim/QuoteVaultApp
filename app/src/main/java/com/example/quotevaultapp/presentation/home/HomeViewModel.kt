package com.example.quotevaultapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseQuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Sealed class representing the home screen UI state
 */
sealed class HomeUiState {
    data object Idle : HomeUiState()
    data object Loading : HomeUiState()
    data object Success : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

/**
 * Sealed class representing UI state for any data
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

/**
 * ViewModel for Home screen
 * Manages quotes display, pagination, filtering, search, and favorites
 */
class HomeViewModel(
    private val quoteRepository: QuoteRepository = SupabaseQuoteRepository()
) : ViewModel() {
    
    companion object {
        private const val PAGE_SIZE = 20
        private const val INITIAL_PAGE = 1
    }
    
    // Quotes list with manual pagination
    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()
    
    // Quote of the Day with UI state
    private val _quoteOfTheDay = MutableStateFlow<UiState<Quote>>(UiState.Loading)
    val quoteOfTheDay: StateFlow<UiState<Quote>> = _quoteOfTheDay.asStateFlow()
    
    // Selected category filter
    private val _selectedCategory = MutableStateFlow<QuoteCategory?>(null)
    val selectedCategory: StateFlow<QuoteCategory?> = _selectedCategory.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Refreshing state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // UI State
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // Pagination state
    private var currentPage = INITIAL_PAGE
    private val _hasMorePages = MutableStateFlow(true)
    private val _isLoadingMore = MutableStateFlow(false)
    private var searchResults: List<Quote>? = null
    
    // Expose pagination state to UI
    val hasMore: Boolean
        get() = _hasMorePages.value && !_isLoadingMore.value
    
    val isLoading: Boolean
        get() = _uiState.value is HomeUiState.Loading || _isLoadingMore.value
    
    val isLoadingMore: StateFlow<Boolean>
        get() = _isLoadingMore.asStateFlow()
    
    val error: String?
        get() = (_uiState.value as? HomeUiState.Error)?.message
    
    init {
        loadQuoteOfTheDay()
        loadQuotes()
    }
    
    /**
     * Handle category selection
     */
    fun onCategorySelected(category: QuoteCategory?) {
        if (_selectedCategory.value == category) return
        
        _selectedCategory.value = category
        
        // If there's a search query, re-run search with new category
        // Otherwise, just load quotes for the category
        val currentQuery = _searchQuery.value.trim()
        if (currentQuery.isNotEmpty()) {
            // Re-run search with new category filter
            performSearch(currentQuery)
        } else {
            // Clear search results and load category quotes
            searchResults = null
            resetPagination()
            
            // Load quotes for selected category or all quotes
            if (category != null) {
                loadQuotesByCategory(category)
            } else {
                loadQuotes()
            }
        }
    }
    
    /**
     * Handle search query change with automatic debounced search
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        
        // Trigger debounced search automatically
        viewModelScope.launch {
            delay(500) // 500ms debounce to avoid too many API calls
            if (_searchQuery.value == query) { // Only search if query hasn't changed
                performSearch(query)
            }
        }
    }
    
    /**
     * Execute search (can be called manually or automatically)
     */
    fun onSearch() {
        performSearch(_searchQuery.value)
    }
    
    /**
     * Internal search function that searches both quote text and author
     * Respects the selected category filter if one is active
     */
    private fun performSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            // Clear search and reload quotes based on selected category
            searchResults = null
            resetPagination()
            val selectedCategory = _selectedCategory.value
            if (selectedCategory != null) {
                loadQuotesByCategory(selectedCategory)
            } else {
                loadQuotes()
            }
            return
        }
        
        // Set loading state only if we don't already have results
        if (_quotes.value.isEmpty()) {
            _uiState.value = HomeUiState.Loading
        }
        
        viewModelScope.launch {
            // Use category-aware search if repository supports it
            val selectedCategory = _selectedCategory.value
            val repository = quoteRepository as? SupabaseQuoteRepository
            
            val result = if (repository != null && selectedCategory != null) {
                // Use category-aware search
                repository.searchQuotesWithCategory(trimmedQuery, selectedCategory)
            } else if (repository != null) {
                // Search without category filter
                repository.searchQuotesWithCategory(trimmedQuery, null)
            } else {
                // Fallback to regular search
                quoteRepository.searchQuotes(trimmedQuery)
            }
            
            when (result) {
                is Result.Success -> {
                    searchResults = result.data
                    _quotes.value = result.data
                    _uiState.value = HomeUiState.Success
                }
                is Result.Error -> {
                    val errorMsg = result.exception.message ?: "No quotes found"
                    _uiState.value = HomeUiState.Error(errorMsg)
                    _quotes.value = emptyList()
                    searchResults = null
                }
            }
        }
    }
    
    /**
     * Refresh quotes (pull to refresh)
     */
    fun onRefresh() {
        refreshQuotes()
    }
    
    /**
     * Internal refresh function
     */
    private fun refreshQuotes() {
        _isRefreshing.value = true
        
        viewModelScope.launch {
            try {
                // Reload quote of the day
                loadQuoteOfTheDay()
                
                // Reset and reload quotes
                resetPagination()
                searchResults = null
                _searchQuery.value = ""
                
                when {
                    _selectedCategory.value != null -> {
                        loadQuotesByCategory(_selectedCategory.value!!)
                    }
                    else -> {
                        loadQuotes()
                    }
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Toggle favorite status for a quote
     */
    fun toggleFavorite(quoteId: String) {
        viewModelScope.launch {
            // Try to find quote in the list first
            val quote = _quotes.value.find { it.id == quoteId }
                // If not found, check quote of the day
                ?: (_quoteOfTheDay.value as? UiState.Success<Quote>)?.data?.takeIf { it.id == quoteId }
                ?: return@launch
            
            val result = if (quote.isFavorite) {
                quoteRepository.removeFromFavorites(quoteId)
            } else {
                quoteRepository.addToFavorites(quoteId)
            }
            
            when (result) {
                is Result.Success -> {
                    val newFavoriteStatus = !quote.isFavorite
                    
                    // Update quote in list
                    updateQuoteFavoriteStatus(quoteId, newFavoriteStatus)
                    
                    // Update quote of the day if it's the same quote
                    val currentQuoteOfTheDay = _quoteOfTheDay.value
                    if (currentQuoteOfTheDay is UiState.Success && currentQuoteOfTheDay.data.id == quoteId) {
                        _quoteOfTheDay.value = UiState.Success(
                            currentQuoteOfTheDay.data.copy(
                                isFavorite = newFavoriteStatus
                            )
                        )
                    }
                }
                is Result.Error -> {
                    // Log error for debugging
                    android.util.Log.e("HomeViewModel", "Error toggling favorite: ${result.exception.message}", result.exception)
                    // Optionally: emit an error state or show a toast
                    // For now, we'll silently fail but the error is logged
                }
            }
        }
    }
    
    /**
     * Handle quote click (for navigation)
     */
    fun onQuoteClick(quote: Quote) {
        // This is typically handled in the UI layer for navigation
        // Can emit a navigation event if needed
    }
    
    /**
     * Load quotes (initial load or after filter change)
     */
    fun loadQuotes() {
        if (_uiState.value is HomeUiState.Loading) return
        
        _uiState.value = HomeUiState.Loading
        currentPage = INITIAL_PAGE
        _hasMorePages.value = true
        
        viewModelScope.launch {
            val result = quoteRepository.getQuotes(currentPage, PAGE_SIZE)
            
            when (result) {
                is Result.Success -> {
                    _quotes.value = result.data
                    _hasMorePages.value = result.data.size == PAGE_SIZE
                    _uiState.value = HomeUiState.Success
                }
                is Result.Error -> {
                    val errorMsg = result.exception.message 
                        ?: "Failed to load quotes"
                    _uiState.value = HomeUiState.Error(errorMsg)
                    _quotes.value = emptyList()
                }
            }
        }
    }
    
    /**
     * Load more quotes (pagination)
     */
    fun loadMoreQuotes() {
        if (_isLoadingMore.value || !_hasMorePages.value || searchResults != null) return
        if (_searchQuery.value.isNotBlank()) return // Don't paginate search results
        
        _isLoadingMore.value = true
        currentPage++
        
        viewModelScope.launch {
            val result = if (_selectedCategory.value != null) {
                quoteRepository.getQuotesByCategory(_selectedCategory.value!!, currentPage)
            } else {
                quoteRepository.getQuotes(currentPage, PAGE_SIZE)
            }
            
            when (result) {
                is Result.Success -> {
                    val newQuotes = result.data
                    _quotes.update { it + newQuotes }
                    _hasMorePages.value = newQuotes.size == PAGE_SIZE
                }
                is Result.Error -> {
                    // Revert page on error
                    currentPage--
                    // Could emit error state for pagination failures
                }
            }
            
            _isLoadingMore.value = false
        }
    }
    
    /**
     * Load quotes by category
     */
    private fun loadQuotesByCategory(category: QuoteCategory) {
        _uiState.value = HomeUiState.Loading
        currentPage = INITIAL_PAGE
        _hasMorePages.value = true
        
        viewModelScope.launch {
            val result = quoteRepository.getQuotesByCategory(category, currentPage)
            
            when (result) {
                is Result.Success -> {
                    _quotes.value = result.data
                    _hasMorePages.value = result.data.size == PAGE_SIZE
                    _uiState.value = HomeUiState.Success
                }
                is Result.Error -> {
                    val errorMsg = result.exception.message 
                        ?: "Failed to load quotes"
                    _uiState.value = HomeUiState.Error(errorMsg)
                    _quotes.value = emptyList()
                }
            }
        }
    }
    
    /**
     * Load quote of the day
     */
    private fun loadQuoteOfTheDay() {
        viewModelScope.launch {
            _quoteOfTheDay.value = UiState.Loading
            
            val result = quoteRepository.getQuoteOfTheDay()
            when (result) {
                is Result.Success -> {
                    _quoteOfTheDay.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _quoteOfTheDay.value = UiState.Error(
                        result.exception.message ?: "Failed to load quote of the day"
                    )
                }
            }
        }
    }
    
    /**
     * Refresh quote of the day
     */
    fun refreshQuoteOfTheDay() {
        loadQuoteOfTheDay()
    }
    
    /**
     * Update quote favorite status in the list
     */
    private fun updateQuoteFavoriteStatus(quoteId: String, isFavorite: Boolean) {
        _quotes.update { quotes ->
            quotes.map { quote ->
                if (quote.id == quoteId) {
                    quote.copy(isFavorite = isFavorite)
                } else {
                    quote
                }
            }
        }
    }
    
    /**
     * Reset pagination state
     */
    private fun resetPagination() {
        currentPage = INITIAL_PAGE
        _hasMorePages.value = true
        _isLoadingMore.value = false
    }
}
