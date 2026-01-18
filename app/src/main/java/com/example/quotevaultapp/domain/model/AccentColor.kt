package com.example.quotevaultapp.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Accent color options for the application
 */
enum class AccentColor {
    PURPLE, BLUE, GREEN;
    
    @Composable
    fun toColor(): Color = when (this) {
        PURPLE -> Color(0xFF9C27B0)
        BLUE -> Color(0xFF2196F3)
        GREEN -> Color(0xFF4CAF50)
    }
}