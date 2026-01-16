package com.example.quotevaultapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
 * ViewModel for Home screen
 * Manages quotes display, pagination, filtering, search, and favorites
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {
    
    companion object {
        private const val PAGE_SIZE = 20
        private const val INITIAL_PAGE = 1
    }
    
    // Quotes list with manual pagination
    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()
    
    // Quote of the Day
    private val _quoteOfTheDay = MutableStateFlow<Quote?>(null)
    val quoteOfTheDay: StateFlow<Quote?> = _quoteOfTheDay.asStateFlow()
    
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
    private var hasMorePages = true
    private var isLoadingMore = false
    private var searchResults: List<Quote>? = null
    
    // Expose pagination state to UI
    val hasMore: Boolean
        get() = hasMorePages && !isLoadingMore
    
    val isLoading: Boolean
        get() = _uiState.value is HomeUiState.Loading || isLoadingMore
    
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
        _searchQuery.value = "" // Clear search when category changes
        searchResults = null
        resetPagination()
        loadQuotes()
    }
    
    /**
     * Handle search query change
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Execute search
     */
    fun onSearch() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) {
            // Clear search and reload quotes
            searchResults = null
            _selectedCategory.value = null
            resetPagination()
            loadQuotes()
            return
        }
        
        _uiState.value = HomeUiState.Loading
        
        viewModelScope.launch {
            // Try searching by text first, then by author
            val textResult = quoteRepository.searchQuotes(query)
            val authorResult = quoteRepository.searchByAuthor(query)
            
            when {
                textResult is Result.Success && authorResult is Result.Success -> {
                    // Combine and deduplicate results
                    val combined = (textResult.data + authorResult.data)
                        .distinctBy { it.id }
                    searchResults = combined
                    _quotes.value = combined
                    _uiState.value = HomeUiState.Success
                }
                textResult is Result.Success -> {
                    searchResults = textResult.data
                    _quotes.value = textResult.data
                    _uiState.value = HomeUiState.Success
                }
                authorResult is Result.Success -> {
                    searchResults = authorResult.data
                    _quotes.value = authorResult.data
                    _uiState.value = HomeUiState.Success
                }
                else -> {
                    val errorMsg = (textResult as? Result.Error)?.exception?.message
                        ?: (authorResult as? Result.Error)?.exception?.message
                        ?: "No quotes found"
                    _uiState.value = HomeUiState.Error(errorMsg)
                    _quotes.value = emptyList()
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
            val quote = _quotes.value.find { it.id == quoteId }
                ?: _quoteOfTheDay.value?.takeIf { it.id == quoteId }
                ?: return@launch
            
            val result = if (quote.isFavorite) {
                quoteRepository.removeFromFavorites(quoteId)
            } else {
                quoteRepository.addToFavorites(quoteId)
            }
            
            when (result) {
                is Result.Success -> {
                    // Update quote in list
                    updateQuoteFavoriteStatus(quoteId, !quote.isFavorite)
                    
                    // Update quote of the day if it's the same quote
                    if (_quoteOfTheDay.value?.id == quoteId) {
                        _quoteOfTheDay.value = _quoteOfTheDay.value?.copy(
                            isFavorite = !quote.isFavorite
                        )
                    }
                }
                is Result.Error -> {
                    // Handle error silently or show toast
                    // Could emit an error state if needed
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
        hasMorePages = true
        
        viewModelScope.launch {
            val result = quoteRepository.getQuotes(currentPage, PAGE_SIZE)
            
            when (result) {
                is Result.Success -> {
                    _quotes.value = result.data
                    hasMorePages = result.data.size == PAGE_SIZE
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
        if (isLoadingMore || !hasMorePages || searchResults != null) return
        if (_searchQuery.value.isNotBlank()) return // Don't paginate search results
        
        isLoadingMore = true
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
                    hasMorePages = newQuotes.size == PAGE_SIZE
                }
                is Result.Error -> {
                    // Revert page on error
                    currentPage--
                    // Could emit error state for pagination failures
                }
            }
            
            isLoadingMore = false
        }
    }
    
    /**
     * Load quotes by category
     */
    private fun loadQuotesByCategory(category: QuoteCategory) {
        _uiState.value = HomeUiState.Loading
        currentPage = INITIAL_PAGE
        hasMorePages = true
        
        viewModelScope.launch {
            val result = quoteRepository.getQuotesByCategory(category, currentPage)
            
            when (result) {
                is Result.Success -> {
                    _quotes.value = result.data
                    hasMorePages = result.data.size == PAGE_SIZE
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
            val result = quoteRepository.getQuoteOfTheDay()
            when (result) {
                is Result.Success -> {
                    _quoteOfTheDay.value = result.data
                }
                is Result.Error -> {
                    // Fail silently for quote of the day
                    _quoteOfTheDay.value = null
                }
            }
        }
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
        hasMorePages = true
        isLoadingMore = false
    }
}
