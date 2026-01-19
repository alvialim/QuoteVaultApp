package com.example.quotevaultapp.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Stitch Design Shapes
 * Exact border radius values from Google Stitch design specifications
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp), // Stitch: 16dp for buttons
    large = RoundedCornerShape(20.dp), // Stitch: 20dp for quote cards
    extraLarge = RoundedCornerShape(24.dp) // Stitch: 24dp for collection cards, QOTD
)

/**
 * Stitch Design Custom Shapes
 * Exact corner radius values from Stitch design specifications
 */
object AppShapes {
    
    /**
     * Quote card shape - Stitch: 20dp rounded corners
     * White/light background quote cards
     */
    val quoteCard = RoundedCornerShape(20.dp) // Stitch: 20dp
    
    /**
     * Quote of the Day card shape - Stitch: 24dp rounded corners
     * Large gradient card at top
     */
    val quoteCardElevated = RoundedCornerShape(24.dp) // Stitch: 24dp
    
    /**
     * Collection card shape - Stitch: 24dp rounded corners
     * Dark background collection cards
     */
    val collectionCard = RoundedCornerShape(24.dp) // Stitch: 24dp
    
    /**
     * Category chip shape - Stitch: Full pill shape (40dp height)
     * Rounded fully to create pill shape
     */
    val categoryChip = RoundedCornerShape(20.dp) // Pill shape (half of 40dp height)
    
    /**
     * Button shape - Stitch: 16dp rounded corners
     * Sign In button, primary buttons
     */
    val button = RoundedCornerShape(16.dp) // Stitch: 16dp
    
    /**
     * Text field shape - Stitch: 16dp rounded corners
     * Input fields in login screen
     */
    val textField = RoundedCornerShape(16.dp) // Stitch: 16dp
    
    /**
     * Small badge/pill shape - Stitch: Pill shape
     * Collection badge (quote count)
     */
    val badge = RoundedCornerShape(12.dp) // Stitch: Pill shape for badges
    
    /**
     * FAB shape - Stitch: Fully rounded (circular)
     * Floating action button
     */
    val fab = RoundedCornerShape(32.dp) // Circular (half of 64dp size)
    
    /**
     * Bottom sheet shape - Stitch: 24dp top corners
     * Modal bottom sheets
     */
    val bottomSheet = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp
    )
    
    /**
     * Dialog shape - Stitch: 24dp rounded corners
     * Alert dialogs, confirmation dialogs
     */
    val dialog = RoundedCornerShape(24.dp) // Stitch: 24dp
}
