package com.example.quotevaultapp.presentation.quote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseQuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for Quote Detail screen
 */
class QuoteDetailViewModel(
    private val quoteRepository: QuoteRepository = SupabaseQuoteRepository()
) : ViewModel() {
    
    private val _quote = MutableStateFlow<Quote?>(null)
    val quote: StateFlow<Quote?> = _quote.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load quote by ID using repository's getQuoteById method
     */
    fun loadQuote(quoteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                when (val result = quoteRepository.getQuoteById(quoteId)) {
                    is Result.Success -> {
                        _quote.value = result.data
                    }
                    is Result.Error -> {
                        _error.value = result.exception.message ?: "Failed to load quote"
                    }
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
