package com.example.quotevaultapp.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Stitch Design Typography System
 * Exact typography values from Google Stitch design specifications
 */
val Typography = Typography(
    // Display styles - Stitch uses large, bold app title
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp, // App title size
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp, // App title variant
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp, // Screen titles
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    
    // Headline styles - Stitch screen titles
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // Stitch uses Bold
        fontSize = 32.sp, // App title
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // Stitch uses Bold
        fontSize = 28.sp, // Screen titles
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // Stitch uses Bold
        fontSize = 24.sp, // Large quote text (QOTD)
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Title styles - Collection card titles
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // Stitch uses Bold
        fontSize = 22.sp, // Collection card title
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Stitch uses Medium
        fontSize = 16.sp, // Standard title
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Stitch uses Medium
        fontSize = 14.sp, // Category chips, author
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    
    // Body styles - Quote text
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Stitch uses Regular
        fontSize = 18.sp, // Quote text (medium)
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Stitch uses Regular
        fontSize = 16.sp, // Body text
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Stitch uses Regular
        fontSize = 14.sp, // Preview text (italic)
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    
    // Label styles - Uppercase labels
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Stitch uses Medium
        fontSize = 14.sp, // Category chips
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Stitch uses Medium
        fontSize = 12.sp, // Labels, uppercase
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp // Stitch uses uppercase with spacing
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Stitch uses Medium
        fontSize = 12.sp, // Bottom nav labels
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Stitch Design Typography for Quotes
 * Exact typography values from Stitch design specifications
 */
object QuoteTypography {
    
    /**
     * Quote text typography - Small size
     * Stitch: 18sp, Regular, italic for quotes
     */
    val Small = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Regular
        fontSize = 18.sp, // Stitch: 18-20sp
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
        fontStyle = FontStyle.Italic // Stitch uses italic for quotes
    )
    
    /**
     * Quote text typography - Medium size (default)
     * Stitch: 18-20sp, Regular, italic for quotes
     */
    val Medium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Regular
        fontSize = 20.sp, // Stitch: 18-20sp (using 20sp as default)
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        fontStyle = FontStyle.Italic // Stitch uses italic for quotes
    )
    
    /**
     * Quote text typography - Large size
     * Stitch: 24sp for Quote of the Day, italic
     */
    val Large = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Regular
        fontSize = 24.sp, // Stitch: 24sp for QOTD
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        fontStyle = FontStyle.Italic // Stitch uses italic for quotes
    )
    
    /**
     * Quote author typography
     * Stitch: 14sp, Medium, Gray, Uppercase
     */
    val Author = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Stitch uses Medium
        fontSize = 14.sp, // Stitch: 14sp
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp // Stitch uses uppercase with spacing
    )
    
    /**
     * Quote category typography
     * Stitch: 14sp, Medium, for category chips
     */
    val Category = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Stitch uses Medium
        fontSize = 14.sp, // Stitch: 14sp for category chips
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )
    
    /**
     * Quote of the Day label
     * Stitch: Small, uppercase, white
     */
    val QuoteOfTheDayLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp, // Small
        lineHeight = 16.sp,
        letterSpacing = 1.sp // Uppercase with spacing
    )
    
    /**
     * Collection card preview text
     * Stitch: 14sp, italic, gray
     */
    val CollectionPreview = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
    )
}
