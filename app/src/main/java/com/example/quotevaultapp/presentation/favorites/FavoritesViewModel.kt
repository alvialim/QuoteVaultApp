package com.example.quotevaultapp.presentation.favorites

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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Sealed class representing the favorites screen UI state
 */
sealed class FavoritesUiState {
    data object Idle : FavoritesUiState()
    data object Loading : FavoritesUiState()
    data object Success : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

/**
 * ViewModel for Favorites screen
 * Manages favorite quotes, search, filtering, and sync state
 */
class FavoritesViewModel(
    private val quoteRepository: QuoteRepository = SupabaseQuoteRepository()
) : ViewModel() {
    
    // Favorites list (reactive from repository)
    private val _favorites = MutableStateFlow<List<Quote>>(emptyList())
    val favorites: StateFlow<List<Quote>> = _favorites.asStateFlow()
    
    // Selected category filter
    private val _selectedCategory = MutableStateFlow<QuoteCategory?>(null)
    val selectedCategory: StateFlow<QuoteCategory?> = _selectedCategory.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Refreshing state (pull to refresh)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // Syncing state (syncing with cloud)
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    // UI State
    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    
    // Expose error for convenience
    val error: String?
        get() = (_uiState.value as? FavoritesUiState.Error)?.message
    
    val isLoading: Boolean
        get() = _uiState.value is FavoritesUiState.Loading
    
    init {
        // Observe favorites from repository (reactive Flow)
        observeFavorites()
    }
    
    /**
     * Observe favorites Flow from repository
     * This automatically updates when favorites change
     */
    private fun observeFavorites() {
        quoteRepository.getFavorites()
            .onEach { quotes ->
                _favorites.value = quotes
                _uiState.value = FavoritesUiState.Success
                _isSyncing.value = false
            }
            .catch { exception ->
                _uiState.value = FavoritesUiState.Error(
                    exception.message ?: "Failed to load favorites"
                )
                _isSyncing.value = false
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Load favorites (initial load or manual refresh)
     */
    fun loadFavorites() {
        if (_uiState.value is FavoritesUiState.Loading) return
        
        _uiState.value = FavoritesUiState.Loading
        _isSyncing.value = true
        
        // Force reload favorites from database
        viewModelScope.launch {
            try {
                // If repository is SupabaseQuoteRepository, trigger a reload
                if (quoteRepository is SupabaseQuoteRepository) {
                    (quoteRepository as SupabaseQuoteRepository).forceReloadFavorites()
                }
            } catch (e: Exception) {
                _uiState.value = FavoritesUiState.Error(
                    e.message ?: "Failed to load favorites"
                )
                _isSyncing.value = false
            }
        }
    }
    
    /**
     * Refresh favorites (pull to refresh)
     */
    fun refreshFavorites() {
        _isRefreshing.value = true
        _isSyncing.value = true
        
        viewModelScope.launch {
            try {
                // Trigger a refresh by re-observing favorites
                // The repository Flow should emit updated data
                // For now, we just reset the refreshing state after a delay
                kotlinx.coroutines.delay(500) // Simulate network delay
                
                // Favorites will be updated automatically via Flow observation
            } catch (e: Exception) {
                _uiState.value = FavoritesUiState.Error(
                    e.message ?: "Failed to refresh favorites"
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Handle category selection
     */
    fun onCategorySelected(category: QuoteCategory?) {
        _selectedCategory.value = category
    }
    
    /**
     * Handle search query change
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Remove quote from favorites
     */
    fun removeFromFavorites(quoteId: String) {
        _isSyncing.value = true
        
        viewModelScope.launch {
            val result = quoteRepository.removeFromFavorites(quoteId)
            
            when (result) {
                is Result.Success -> {
                    // Favorites will be updated automatically via Flow observation
                    // No need to manually update the list
                }
                is Result.Error -> {
                    _isSyncing.value = false
                    // Could emit error state if needed
                    _uiState.value = FavoritesUiState.Error(
                        result.exception.message ?: "Failed to remove from favorites"
                    )
                }
                is Result.Loading -> {
                    // Loading state
                }
            }
        }
    }
    
    /**
     * Toggle favorite status (add/remove)
     */
    fun toggleFavorite(quoteId: String) {
        val quote = _favorites.value.find { it.id == quoteId }
            ?: return
        
        _isSyncing.value = true
        
        viewModelScope.launch {
            val result = if (quote.isFavorite) {
                quoteRepository.removeFromFavorites(quoteId)
            } else {
                quoteRepository.addToFavorites(quoteId)
            }
            
            when (result) {
                is Result.Success -> {
                    // Favorites will be updated automatically via Flow observation
                }
                is Result.Error -> {
                    _isSyncing.value = false
                    _uiState.value = FavoritesUiState.Error(
                        result.exception.message ?: "Failed to update favorite"
                    )
                }
                is Result.Loading -> {
                    // Loading state
                }
            }
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        if (_uiState.value is FavoritesUiState.Error) {
            _uiState.value = FavoritesUiState.Success
        }
    }
}
