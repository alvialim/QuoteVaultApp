package com.example.quotevaultapp.presentation.settings

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.domain.model.AppTheme
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.model.User
import com.example.quotevaultapp.domain.repository.AuthRepository
import com.example.quotevaultapp.util.PreferencesKeys
import com.example.quotevaultapp.util.WorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _theme = MutableStateFlow<AppTheme>(AppTheme.SYSTEM)
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()
    
    private val _fontSize = MutableStateFlow<FontSize>(FontSize.MEDIUM)
    val fontSize: StateFlow<FontSize> = _fontSize.asStateFlow()
    
    private val _notificationEnabled = MutableStateFlow(false)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()
    
    private val _notificationHour = MutableStateFlow(9)
    val notificationHour: StateFlow<Int> = _notificationHour.asStateFlow()
    
    private val _notificationMinute = MutableStateFlow(0)
    val notificationMinute: StateFlow<Int> = _notificationMinute.asStateFlow()
    
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentUser()
        loadPreferences()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                _currentUser.value = user
            }
        }
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            // Load theme
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.THEME]?.let { themeString ->
                    try {
                        AppTheme.valueOf(themeString)
                    } catch (e: IllegalArgumentException) {
                        AppTheme.SYSTEM
                    }
                } ?: AppTheme.SYSTEM
            }.collect { theme ->
                _theme.value = theme
            }
        }
        
        viewModelScope.launch {
            // Load font size
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.FONT_SIZE]?.let { fontSizeString ->
                    try {
                        FontSize.valueOf(fontSizeString)
                    } catch (e: IllegalArgumentException) {
                        FontSize.MEDIUM
                    }
                } ?: FontSize.MEDIUM
            }.collect { fontSize ->
                _fontSize.value = fontSize
            }
        }
        
        viewModelScope.launch {
            // Load notification settings
            dataStore.data.collect { preferences ->
                _notificationEnabled.value = preferences[PreferencesKeys.NOTIFICATION_ENABLED] ?: false
                _notificationHour.value = preferences[PreferencesKeys.NOTIFICATION_HOUR] ?: 9
                _notificationMinute.value = preferences[PreferencesKeys.NOTIFICATION_MINUTE] ?: 0
            }
        }
    }
    
    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.THEME] = theme.name
            }
            // Sync to Supabase if user is logged in
            syncThemeToSupabase(theme)
        }
    }
    
    fun updateFontSize(fontSize: FontSize) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.FONT_SIZE] = fontSize.name
            }
            // Sync to Supabase if user is logged in
            syncFontSizeToSupabase(fontSize)
        }
    }
    
    fun updateNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.NOTIFICATION_ENABLED] = enabled
            }
            
            if (enabled) {
                val hour = _notificationHour.value
                val minute = _notificationMinute.value
                WorkScheduler.scheduleDailyQuoteNotification(context, hour, minute)
            } else {
                WorkScheduler.cancelDailyQuoteNotification(context)
            }
        }
    }
    
    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.NOTIFICATION_HOUR] = hour
                preferences[PreferencesKeys.NOTIFICATION_MINUTE] = minute
            }
            
            if (_notificationEnabled.value) {
                WorkScheduler.scheduleDailyQuoteNotification(context, hour, minute)
            }
        }
    }
    
    fun updateDisplayName(displayName: String) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            when (val result = authRepository.updateProfile(displayName, null)) {
                is Result.Success -> {
                    _uiState.value = SettingsUiState.Success("Display name updated")
                }
                is Result.Error -> {
                    _uiState.value = SettingsUiState.Error(result.exception.message ?: "Failed to update display name")
                }
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            when (val result = authRepository.signOut()) {
                is Result.Success -> {
                    _uiState.value = SettingsUiState.SignedOut
                }
                is Result.Error -> {
                    _uiState.value = SettingsUiState.Error(result.exception.message ?: "Failed to sign out")
                }
            }
        }
    }
    
    private suspend fun syncThemeToSupabase(theme: AppTheme) {
        _currentUser.value?.let { user ->
            // Note: This would need to be added to AuthRepository if theme is stored in profile
            // For now, we only store it locally in DataStore
        }
    }
    
    private suspend fun syncFontSizeToSupabase(fontSize: FontSize) {
        _currentUser.value?.let { user ->
            // Note: This would need to be added to AuthRepository if fontSize is stored in profile
            // For now, we only store it locally in DataStore
        }
    }
}

/**
 * UI state for Settings screen
 */
sealed class SettingsUiState {
    data object Loading : SettingsUiState()
    data class Success(val message: String) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
    data object SignedOut : SettingsUiState()
}
