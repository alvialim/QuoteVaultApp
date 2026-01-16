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
    
    fun loadQuote(quoteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Search through favorites first, then try to find in all quotes
            // Note: This is a workaround since there's no getQuoteById method
            // In a real app, you'd want to add getQuoteById to QuoteRepository
            val favoritesResult = quoteRepository.getFavorites()
            
            // For now, we'll need to get quotes and search
            // This is not ideal but works until getQuoteById is added
            // Try searching in all quotes - we'll need to search for it
            _error.value = "Quote loading not yet implemented with getQuoteById"
            _isLoading.value = false
        }
    }
    
    fun setQuote(quote: Quote) {
        _quote.value = quote
    }
}
