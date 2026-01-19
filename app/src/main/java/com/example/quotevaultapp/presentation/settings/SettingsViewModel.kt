package com.example.quotevaultapp.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quotevaultapp.data.local.PreferencesManager
import com.example.quotevaultapp.domain.model.AccentColor
import com.example.quotevaultapp.domain.model.AppTheme
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.model.User
import com.example.quotevaultapp.domain.repository.AuthRepository
import com.example.quotevaultapp.util.NotificationTester
import com.example.quotevaultapp.util.WorkScheduler
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings screen
 * Uses PreferencesManager singleton instead of direct DataStore access
 */
class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val appContext: Context? = null // Not needed since we use PreferencesManager
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _theme = MutableStateFlow<AppTheme>(AppTheme.SYSTEM)
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()
    
    private val _fontSize = MutableStateFlow<FontSize>(FontSize.MEDIUM)
    val fontSize: StateFlow<FontSize> = _fontSize.asStateFlow()
    
    private val _accentColor = MutableStateFlow<AccentColor>(AccentColor.PURPLE)
    val accentColor: StateFlow<AccentColor> = _accentColor.asStateFlow()
    
    private val _notificationEnabled = MutableStateFlow(false)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()
    
    private val _notificationHour = MutableStateFlow(9)
    val notificationHour: StateFlow<Int> = _notificationHour.asStateFlow()
    
    private val _notificationMinute = MutableStateFlow(0)
    val notificationMinute: StateFlow<Int> = _notificationMinute.asStateFlow()
    
    private val _nextScheduledTime = MutableStateFlow<String?>(null)
    val nextScheduledTime: StateFlow<String?> = _nextScheduledTime.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
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
        // Observe theme from PreferencesManager
        viewModelScope.launch {
            preferencesManager.theme.collect { theme ->
                _theme.value = theme
            }
        }
        
        // Observe font size from PreferencesManager
        viewModelScope.launch {
            preferencesManager.fontSize.collect { fontSize ->
                _fontSize.value = fontSize
            }
        }
        
        // Observe accent color from PreferencesManager
        viewModelScope.launch {
            preferencesManager.accentColor.collect { color ->
                _accentColor.value = color
            }
        }
        
        // Observe notification settings from PreferencesManager
        viewModelScope.launch {
            preferencesManager.notificationEnabled.collect { enabled ->
                _notificationEnabled.value = enabled
            }
        }
        
        viewModelScope.launch {
            preferencesManager.notificationTime.collect { time ->
                _notificationHour.value = time.hour
                _notificationMinute.value = time.minute
            }
        }
        
        // Note: updateNextScheduledTime requires context, so it's called from UI when needed
    }
    
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesManager.updateTheme(theme)
            _theme.value = theme
            syncToSupabase()
        }
    }
    
    fun setFontSize(fontSize: FontSize) {
        viewModelScope.launch {
            preferencesManager.updateFontSize(fontSize)
            _fontSize.value = fontSize
            syncToSupabase()
        }
    }
    
    fun setAccentColor(color: AccentColor) {
        viewModelScope.launch {
            preferencesManager.updateAccentColor(color)
            _accentColor.value = color
            syncToSupabase()
        }
    }
    
    fun updateNotificationEnabled(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Updating notification enabled: $enabled")
            preferencesManager.updateNotificationEnabled(enabled)
            
            if (enabled) {
                val hour = _notificationHour.value
                val minute = _notificationMinute.value
                Log.d("SettingsViewModel", "Enabling notifications and scheduling for ${String.format("%02d:%02d", hour, minute)}")
                WorkScheduler.scheduleDailyQuoteNotification(context, hour, minute)
                updateNextScheduledTime(context)
            } else {
                Log.d("SettingsViewModel", "Disabling notifications and cancelling scheduled work")
                WorkScheduler.cancelDailyQuoteNotification(context)
                _nextScheduledTime.value = null
            }
        }
    }
    
    fun updateNotificationTime(hour: Int, minute: Int, context: Context) {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "User changed notification time to ${String.format("%02d:%02d", hour, minute)}")
            preferencesManager.updateNotificationHourMinute(hour, minute)
            
            if (_notificationEnabled.value) {
                Log.d("SettingsViewModel", "Rescheduling notifications with new time: ${String.format("%02d:%02d", hour, minute)}")
                WorkScheduler.scheduleDailyQuoteNotification(context, hour, minute)
                updateNextScheduledTime(context)
            }
        }
    }
    
    /**
     * Send a test notification with the current quote of the day
     * Useful for debugging notification setup
     */
    fun sendTestNotification(context: Context) {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "User requested test notification")
            try {
                NotificationTester.testImmediateNotification(context)
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error sending test notification: ${e.message}", e)
            }
        }
    }
    
    /**
     * Update next scheduled notification time for display
     */
    fun updateNextScheduledTime(context: Context) {
        if (_notificationEnabled.value) {
            val hour = _notificationHour.value
            val minute = _notificationMinute.value
            val nextTime = NotificationTester.getNextScheduledTime(context, hour, minute)
            _nextScheduledTime.value = nextTime
            Log.d("SettingsViewModel", "Next scheduled notification time: $nextTime (hour: $hour, minute: $minute)")
        } else {
            _nextScheduledTime.value = null
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
                is Result.Loading -> {
                    // Loading state
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
                is Result.Loading -> {
                    // Loading state
                }
            }
        }
    }
    
    /**
     * Sync preferences to Supabase with debouncing
     */
    private suspend fun syncToSupabase() {
        _isSyncing.value = true
        try {
            delay(300) // Debounce rapid changes
            val currentUser = _currentUser.value
            if (currentUser != null) {
                // Sync will be handled by AuthRepository.updateUserPreferences if implemented
                // For now, PreferencesManager handles sync internally
            }
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Failed to sync to Supabase: ${e.message}", e)
        } finally {
            _isSyncing.value = false
        }
    }
}

/**
 * Factory for creating SettingsViewModel without Hilt
 */
class SettingsViewModelFactory(
    private val preferencesManager: PreferencesManager,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(preferencesManager, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
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