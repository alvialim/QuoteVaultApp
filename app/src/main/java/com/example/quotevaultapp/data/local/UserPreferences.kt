package com.example.quotevaultapp.data.local

import com.example.quotevaultapp.domain.model.AccentColor
import com.example.quotevaultapp.domain.model.AppTheme
import com.example.quotevaultapp.domain.model.FontSize

/**
 * Data class for user preferences
 */
data class UserPreferences(
    val theme: AppTheme,
    val fontSize: FontSize,
    val accentColor: AccentColor
)