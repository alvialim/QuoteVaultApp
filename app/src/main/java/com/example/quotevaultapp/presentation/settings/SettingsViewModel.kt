package com.example.quotevaultapp.presentation.settings

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.domain.model.AppTheme
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.model.User
import com.example.quotevaultapp.domain.repository.AuthRepository
import com.example.quotevaultapp.domain.repository.QuoteRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseAuthRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseQuoteRepository
import com.example.quotevaultapp.util.NotificationHelper
import com.example.quotevaultapp.util.NotificationTester
import com.example.quotevaultapp.util.PreferencesKeys
import com.example.quotevaultapp.util.WorkScheduler
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * DataStore extension property for preferences
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    ),
    scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
)

/**
 * ViewModel for Settings screen
 */
class SettingsViewModel(
    context: Context,
    private val authRepository: AuthRepository = SupabaseAuthRepository(),
    private val quoteRepository: QuoteRepository = SupabaseQuoteRepository()
) : ViewModel() {
    
    private val dataStore: DataStore<Preferences> = context.dataStore
    private val appContext: Context = context.applicationContext
    
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
    
    private val _nextScheduledTime = MutableStateFlow<String?>(null)
    val nextScheduledTime: StateFlow<String?> = _nextScheduledTime.asStateFlow()
    
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
            Log.d("SettingsViewModel", "Updating notification enabled: $enabled")
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.NOTIFICATION_ENABLED] = enabled
            }
            
            if (enabled) {
                val hour = _notificationHour.value
                val minute = _notificationMinute.value
                Log.d("SettingsViewModel", "Enabling notifications and scheduling for ${String.format("%02d:%02d", hour, minute)}")
                WorkScheduler.scheduleDailyQuoteNotification(appContext, hour, minute)
                updateNextScheduledTime()
            } else {
                Log.d("SettingsViewModel", "Disabling notifications and cancelling scheduled work")
                WorkScheduler.cancelDailyQuoteNotification(appContext)
                _nextScheduledTime.value = null
            }
        }
    }
    
    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "User changed notification time to ${String.format("%02d:%02d", hour, minute)}")
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.NOTIFICATION_HOUR] = hour
                preferences[PreferencesKeys.NOTIFICATION_MINUTE] = minute
            }
            
            if (_notificationEnabled.value) {
                Log.d("SettingsViewModel", "Rescheduling notifications with new time: ${String.format("%02d:%02d", hour, minute)}")
                WorkScheduler.scheduleDailyQuoteNotification(appContext, hour, minute)
                updateNextScheduledTime()
            }
        }
    }
    
    /**
     * Send a test notification with the current quote of the day
     * Useful for debugging notification setup
     */
    fun sendTestNotification() {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "User requested test notification")
            try {
                NotificationTester.testImmediateNotification(appContext)
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error sending test notification: ${e.message}", e)
            }
        }
    }
    
    /**
     * Update next scheduled notification time for display
     */
    fun updateNextScheduledTime() {
        if (_notificationEnabled.value) {
            val hour = _notificationHour.value
            val minute = _notificationMinute.value
            val nextTime = NotificationTester.getNextScheduledTime(appContext, hour, minute)
            _nextScheduledTime.value = nextTime
            Log.d("SettingsViewModel", "Next scheduled notification time: $nextTime (hour: $hour, minute: $minute)")
        } else {
            _nextScheduledTime.value = null
        }
    }
    
    init {
        loadCurrentUser()
        loadPreferences()
        
        // Update next scheduled time when notifications are enabled
        viewModelScope.launch {
            _notificationEnabled.collect { enabled ->
                if (enabled) {
                    updateNextScheduledTime()
                } else {
                    _nextScheduledTime.value = null
                }
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
