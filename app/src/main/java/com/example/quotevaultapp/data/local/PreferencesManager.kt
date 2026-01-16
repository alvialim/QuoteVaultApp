package com.example.quotevaultapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.quotevaultapp.domain.model.AppTheme
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.model.User
import com.example.quotevaultapp.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages app preferences using DataStore
 * Provides Flow-based API for observing preference changes
 * Syncs key preferences to Supabase user profile
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val authRepository: AuthRepository,
    private val supabaseClient: SupabaseClient
) {
    
    companion object {
        // Preference keys
        private val THEME_KEY = stringPreferencesKey("theme")
        private val FONT_SIZE_KEY = stringPreferencesKey("font_size")
        private val NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("notification_enabled")
        private val NOTIFICATION_TIME_KEY = stringPreferencesKey("notification_time")
        private val IS_FIRST_LAUNCH_KEY = booleanPreferencesKey("is_first_launch")
        
        // Default values
        private const val DEFAULT_THEME = "SYSTEM"
        private const val DEFAULT_FONT_SIZE = "MEDIUM"
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
     * Update notification time preference
     */
    suspend fun updateNotificationTime(time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_TIME_KEY] = time.format(TIME_FORMATTER)
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
     * Get current notification enabled value (non-Flow)
     */
    suspend fun getNotificationEnabled(): Boolean = notificationEnabled.first()
    
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
            val currentSession = supabaseClient.auth.currentSessionOrNull()
            if (currentSession != null && currentSession.user != null) {
                // Update profile in Supabase profiles table
                supabaseClient.postgrest.from("profiles")
                    .update(mapOf("theme" to theme.name)) {
                        filter {
                            eq("id", currentSession.user!!.id)
                        }
                    }
            }
        } catch (e: Exception) {
            // Log error but don't fail - local preference is already saved
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
            val currentSession = supabaseClient.auth.currentSessionOrNull()
            if (currentSession != null && currentSession.user != null) {
                // Update profile in Supabase profiles table
                supabaseClient.postgrest.from("profiles")
                    .update(mapOf("font_size" to fontSize.name)) {
                        filter {
                            eq("id", currentSession.user!!.id)
                        }
                    }
            }
        } catch (e: Exception) {
            // Log error but don't fail - local preference is already saved
            android.util.Log.e("PreferencesManager", "Failed to sync font size to Supabase: ${e.message}", e)
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
                
                // Apply theme from user profile if available
                user?.theme?.let { theme ->
                    updateTheme(theme)
                }
                
                // Apply font size from user profile if available
                user?.fontSize?.let { fontSize ->
                    updateFontSize(fontSize)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to load preferences from Supabase: ${e.message}", e)
        }
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
