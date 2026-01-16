package com.example.quotevaultapp.presentation.quote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Quote Detail screen
 */
@HiltViewModel
class QuoteDetailViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {
    
    private val _quote = MutableStateFlow<Quote?>(null)
    val quote: StateFlow<Quote?> = _quote.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load quote by ID
     * Attempts to find quote in favorites first, then searches through paginated results
     */
    fun loadQuote(quoteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // First, try to find in favorites (most common case)
                val favorites = quoteRepository.getFavorites().first()
                var foundQuote: Quote? = favorites.firstOrNull { it.id == quoteId }
                
                // If not found in favorites, search through all quotes (paginated)
                if (foundQuote == null) {
                    var page = 1
                    var found = false
                    
                    // Search up to 10 pages (500 quotes) for performance
                    while (!found && page <= 10) {
                        when (val result = quoteRepository.getQuotes(page, 50)) {
                            is Result.Success -> {
                                foundQuote = result.data.firstOrNull { it.id == quoteId }
                                if (foundQuote != null || result.data.isEmpty()) {
                                    found = true
                                } else {
                                    page++
                                }
                            }
                            is Result.Error -> {
                                _error.value = "Failed to load quote"
                                _isLoading.value = false
                                return@launch
                            }
                        }
                    }
                }
                
                if (foundQuote != null) {
                    _quote.value = foundQuote
                } else {
                    _error.value = "Quote not found"
                }
            } catch (e: Exception) {
                _error.value = "Error loading quote: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setQuote(quote: Quote) {
        _quote.value = quote
    }
}
