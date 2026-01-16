package com.example.quotevaultapp.presentation.quote

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for Quote Detail screen
 * Currently minimal - can be extended for quote operations
 */
@HiltViewModel
class QuoteDetailViewModel @Inject constructor() : ViewModel() {
    // Can add quote-specific operations here if needed
}
