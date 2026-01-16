package com.example.quotevaultapp.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Shapes for QuoteVaultApp
 * Custom shapes for quote cards and UI elements
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Custom shapes for specific UI components
 */
object AppShapes {
    
    /**
     * Quote card shape - rounded corners with medium radius
     */
    val quoteCard = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )
    
    /**
     * Quote card shape with shadow/elevation - slightly larger radius
     */
    val quoteCardElevated = RoundedCornerShape(20.dp)
    
    /**
     * Category chip shape
     */
    val categoryChip = RoundedCornerShape(12.dp)
    
    /**
     * Button shape
     */
    val button = RoundedCornerShape(12.dp)
    
    /**
     * Text field shape
     */
    val textField = RoundedCornerShape(12.dp)
    
    /**
     * Small badge/pill shape
     */
    val badge = RoundedCornerShape(8.dp)
    
    /**
     * FAB shape
     */
    val fab = RoundedCornerShape(16.dp)
    
    /**
     * Bottom sheet shape
     */
    val bottomSheet = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp
    )
    
    /**
     * Dialog shape
     */
    val dialog = RoundedCornerShape(24.dp)
}
