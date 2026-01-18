package com.example.quotevaultapp.domain.model

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Font size preferences for the application
 */
enum class FontSize {
    SMALL,
    MEDIUM,
    LARGE;
    
    fun toSp(): TextUnit = when (this) {
        SMALL -> 14.sp
        MEDIUM -> 18.sp
        LARGE -> 22.sp
    }
}
