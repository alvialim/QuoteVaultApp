package com.example.quotevaultapp.util

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey

/**
 * DataStore preference keys for app settings
 */
object PreferencesKeys {
    // Theme preferences
    val THEME = stringPreferencesKey("theme")
    
    // Font size preferences
    val FONT_SIZE = stringPreferencesKey("font_size")
    
    // Notification preferences
    val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
    val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
    val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
}
