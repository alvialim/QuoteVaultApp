package com.example.quotevaultapp.presentation.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.domain.model.Collection
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Sealed class representing the collections screen UI state
 */
sealed class CollectionsUiState {
    data object Idle : CollectionsUiState()
    data object Loading : CollectionsUiState()
    data object Success : CollectionsUiState()
    data class Error(val message: String) : CollectionsUiState()
}

/**
 * ViewModel for Collections screen
 * Manages user's collections list
 */
@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository
) : ViewModel() {
    
    // Collections list (reactive from repository)
    private val _collections = MutableStateFlow<List<Collection>>(emptyList())
    val collections: StateFlow<List<Collection>> = _collections.asStateFlow()
    
    // UI State
    private val _uiState = MutableStateFlow<CollectionsUiState>(CollectionsUiState.Loading)
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()
    
    // Expose error and loading for convenience
    val error: String?
        get() = (_uiState.value as? CollectionsUiState.Error)?.message
    
    val isLoading: Boolean
        get() = _uiState.value is CollectionsUiState.Loading
    
    init {
        // Observe collections from repository (reactive Flow)
        observeCollections()
    }
    
    /**
     * Observe collections Flow from repository
     * This automatically updates when collections change
     */
    private fun observeCollections() {
        collectionRepository.getCollections()
            .onEach { collections ->
                _collections.value = collections
                _uiState.value = CollectionsUiState.Success
            }
            .catch { exception ->
                _uiState.value = CollectionsUiState.Error(
                    exception.message ?: "Failed to load collections"
                )
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Load collections (manual refresh)
     */
    fun loadCollections() {
        _uiState.value = CollectionsUiState.Loading
        // Collections are automatically updated via Flow observation
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        if (_uiState.value is CollectionsUiState.Error) {
            _uiState.value = CollectionsUiState.Success
        }
    }
}
