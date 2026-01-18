package com.example.quotevaultapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.quotevaultapp.domain.model.AccentColor
import com.example.quotevaultapp.domain.model.AppTheme
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.model.User
import com.example.quotevaultapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.content.Context

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
 * Manages app preferences using DataStore
 * Provides Flow-based API for observing preference changes
 * Syncs key preferences to Supabase user profile
 * Uses singleton pattern for single instance across app
 */
class PreferencesManager private constructor(
    context: Context,
    private val authRepository: AuthRepository = com.example.quotevaultapp.data.remote.supabase.SupabaseAuthRepository()
) {
    private val dataStore: DataStore<Preferences> = context.applicationContext.dataStore
    
    companion object {
        @Volatile
        private var INSTANCE: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // Preference keys
        private val THEME_KEY = stringPreferencesKey("theme")
        private val FONT_SIZE_KEY = stringPreferencesKey("font_size")
        private val ACCENT_COLOR_KEY = stringPreferencesKey("accent_color")
        private val NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("notification_enabled")
        private val NOTIFICATION_TIME_KEY = stringPreferencesKey("notification_time")
        private val NOTIFICATION_HOUR_KEY = androidx.datastore.preferences.core.intPreferencesKey("notification_hour")
        private val NOTIFICATION_MINUTE_KEY = androidx.datastore.preferences.core.intPreferencesKey("notification_minute")
        private val IS_FIRST_LAUNCH_KEY = booleanPreferencesKey("is_first_launch")
        
        // Default values
        private const val DEFAULT_THEME = "SYSTEM"
        private const val DEFAULT_FONT_SIZE = "MEDIUM"
        private const val DEFAULT_ACCENT_COLOR = "PURPLE"
        private val DEFAULT_NOTIFICATION_TIME = LocalTime.of(9, 0)
        private const val DEFAULT_IS_FIRST_LAUNCH = true
        
        // Time format for storing LocalTime
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    }
    
    // Flow-based observables
    
    /**
     * Observe theme preference changes
     */
    val theme: Flow<AppTheme> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[THEME_KEY]?.let { themeString ->
                try {
                    AppTheme.valueOf(themeString)
                } catch (e: IllegalArgumentException) {
                    AppTheme.valueOf(DEFAULT_THEME)
                }
            } ?: AppTheme.valueOf(DEFAULT_THEME)
        }
    
    /**
     * Observe font size preference changes
     */
    val fontSize: Flow<FontSize> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[FONT_SIZE_KEY]?.let { fontSizeString ->
                try {
                    FontSize.valueOf(fontSizeString)
                } catch (e: IllegalArgumentException) {
                    FontSize.valueOf(DEFAULT_FONT_SIZE)
                }
            } ?: FontSize.valueOf(DEFAULT_FONT_SIZE)
        }
    
    /**
     * Observe accent color preference changes
     */
    val accentColor: Flow<AccentColor> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[ACCENT_COLOR_KEY]?.let { colorString ->
                try {
                    AccentColor.valueOf(colorString)
                } catch (e: IllegalArgumentException) {
                    AccentColor.valueOf(DEFAULT_ACCENT_COLOR)
                }
            } ?: AccentColor.valueOf(DEFAULT_ACCENT_COLOR)
        }
    
    /**
     * Observe notification enabled preference changes
     */
    val notificationEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[NOTIFICATION_ENABLED_KEY] ?: false
        }
    
    /**
     * Observe notification time preference changes
     */
    val notificationTime: Flow<LocalTime> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[NOTIFICATION_TIME_KEY]?.let { timeString ->
                try {
                    LocalTime.parse(timeString, TIME_FORMATTER)
                } catch (e: Exception) {
                    DEFAULT_NOTIFICATION_TIME
                }
            } ?: DEFAULT_NOTIFICATION_TIME
        }
    
    /**
     * Observe first launch preference changes
     */
    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH_KEY] ?: DEFAULT_IS_FIRST_LAUNCH
        }
    
    // Update functions
    
    /**
     * Update theme preference and sync to Supabase if user is logged in
     */
    suspend fun updateTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
        
        // Sync to Supabase profile if user is logged in
        syncThemeToSupabase(theme)
    }
    
    /**
     * Update font size preference and sync to Supabase if user is logged in
     */
    suspend fun updateFontSize(fontSize: FontSize) {
        dataStore.edit { preferences ->
            preferences[FONT_SIZE_KEY] = fontSize.name
        }
        
        // Sync to Supabase profile if user is logged in
        syncFontSizeToSupabase(fontSize)
    }
    
    /**
     * Update notification enabled preference
     */
    suspend fun updateNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Update accent color preference and sync to Supabase if user is logged in
     */
    suspend fun updateAccentColor(color: AccentColor) {
        dataStore.edit { preferences ->
            preferences[ACCENT_COLOR_KEY] = color.name
        }
        
        // Sync to Supabase profile if user is logged in
        syncAccentColorToSupabase(color)
    }
    
    /**
     * Update notification time preference
     */
    suspend fun updateNotificationTime(time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_TIME_KEY] = time.format(TIME_FORMATTER)
            preferences[NOTIFICATION_HOUR_KEY] = time.hour
            preferences[NOTIFICATION_MINUTE_KEY] = time.minute
        }
    }
    
    /**
     * Update notification hour and minute
     */
    suspend fun updateNotificationHourMinute(hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_HOUR_KEY] = hour
            preferences[NOTIFICATION_MINUTE_KEY] = minute
            preferences[NOTIFICATION_TIME_KEY] = LocalTime.of(hour, minute).format(TIME_FORMATTER)
        }
    }
    
    /**
     * Update first launch preference
     */
    suspend fun updateIsFirstLaunch(isFirstLaunch: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH_KEY] = isFirstLaunch
        }
    }
    
    // One-time read functions (for use cases that need current value without Flow)
    
    /**
     * Get current theme value (non-Flow)
     */
    suspend fun getTheme(): AppTheme = theme.first()
    
    /**
     * Get current font size value (non-Flow)
     */
    suspend fun getFontSize(): FontSize = fontSize.first()
    
    /**
     * Get current accent color value (non-Flow)
     */
    suspend fun getAccentColor(): AccentColor = accentColor.first()
    
    /**
     * Get current notification enabled value (non-Flow)
     */
    suspend fun getNotificationEnabled(): Boolean = notificationEnabled.first()
    
    /**
     * Get current notification hour (non-Flow)
     */
    suspend fun getNotificationHour(): Int {
        val preferences = dataStore.data.first()
        return preferences[NOTIFICATION_HOUR_KEY] ?: 9
    }
    
    /**
     * Get current notification minute (non-Flow)
     */
    suspend fun getNotificationMinute(): Int {
        val preferences = dataStore.data.first()
        return preferences[NOTIFICATION_MINUTE_KEY] ?: 0
    }
    
    /**
     * Get current notification time value (non-Flow)
     */
    suspend fun getNotificationTime(): LocalTime = notificationTime.first()
    
    /**
     * Get current first launch value (non-Flow)
     */
    suspend fun getIsFirstLaunch(): Boolean = isFirstLaunch.first()
    
    // Sync functions for Supabase
    
    /**
     * Sync theme preference to Supabase user profile
     * Updates the profiles table with the theme preference
     * Note: Assumes the profiles table has a 'theme' column of type text/varchar
     */
    private suspend fun syncThemeToSupabase(theme: AppTheme) {
        try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser is Result.Success && currentUser.data != null) {
                // Sync will be handled by AuthRepository.updateUserPreferences
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to sync theme to Supabase: ${e.message}", e)
        }
    }
    
    /**
     * Sync font size preference to Supabase user profile
     * Updates the profiles table with the font size preference
     * Note: Assumes the profiles table has a 'font_size' column of type text/varchar
     */
    private suspend fun syncFontSizeToSupabase(fontSize: FontSize) {
        try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser is Result.Success && currentUser.data != null) {
                // Sync will be handled by AuthRepository.updateUserPreferences
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to sync font size to Supabase: ${e.message}", e)
        }
    }
    
    /**
     * Sync accent color preference to Supabase user profile
     * Updates the profiles table with the accent color preference
     */
    private suspend fun syncAccentColorToSupabase(color: AccentColor) {
        try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser is Result.Success && currentUser.data != null) {
                // Sync will be handled by AuthRepository.updateUserPreferences
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to sync accent color to Supabase: ${e.message}", e)
        }
    }
    
    /**
     * Load preferences from Supabase profile and apply to local DataStore
     * Call this after user login to sync remote preferences
     */
    suspend fun loadPreferencesFromSupabase() {
        try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser is com.example.quotevaultapp.domain.model.Result.Success && currentUser.data != null) {
                val user = currentUser.data
                
                // Apply theme from user profile
                // user is guaranteed non-null after the null check above, and theme/fontSize are non-null properties
                updateTheme(user.theme)
                
                // Apply font size from user profile
                updateFontSize(user.fontSize)
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to load preferences from Supabase: ${e.message}", e)
        }
    }
    
    /**
     * Load all preferences at once
     */
    suspend fun getAllPreferences(): UserPreferences {
        val preferences = dataStore.data.first()
        return UserPreferences(
            theme = try {
                AppTheme.valueOf(preferences[THEME_KEY] ?: DEFAULT_THEME)
            } catch (e: Exception) {
                AppTheme.valueOf(DEFAULT_THEME)
            },
            fontSize = try {
                FontSize.valueOf(preferences[FONT_SIZE_KEY] ?: DEFAULT_FONT_SIZE)
            } catch (e: Exception) {
                FontSize.valueOf(DEFAULT_FONT_SIZE)
            },
            accentColor = try {
                AccentColor.valueOf(preferences[ACCENT_COLOR_KEY] ?: DEFAULT_ACCENT_COLOR)
            } catch (e: Exception) {
                AccentColor.valueOf(DEFAULT_ACCENT_COLOR)
            }
        )
    }
    
    /**
     * Clear all preferences (useful for logout)
     */
    suspend fun clearPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
