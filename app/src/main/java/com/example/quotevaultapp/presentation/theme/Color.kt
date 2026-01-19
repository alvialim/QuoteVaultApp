package com.example.quotevaultapp.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// ============================================================================
// STITCH DESIGN COLOR SCHEMES
// ============================================================================
// Exact colors from Google Stitch design specifications
// Dark theme with vibrant purple/cyan accents
// ============================================================================

// ============================================================================
// STITCH PRIMARY COLOR SCHEME (Dark Theme - Default)
// ============================================================================

/**
 * Stitch Dark Color Scheme
 * Dark background (#18181B) with purple primary (#A855F7) and cyan secondary (#06B6D4)
 * Quote cards use white background (#F5F5F5) for contrast
 */
val StitchDarkScheme = darkColorScheme(
    // Primary: Purple #A855F7 - Main brand color (buttons, icons, accents)
    primary = Color(0xFFA855F7), // Stitch Purple
    onPrimary = Color(0xFFFFFFFF), // White text on purple
    primaryContainer = Color(0xFF7C3AED), // Darker purple for containers
    onPrimaryContainer = Color(0xFFFFFFFF), // White text on container
    
    // Secondary: Cyan #06B6D4 - Secondary accent (heart icons, links)
    secondary = Color(0xFF06B6D4), // Stitch Cyan
    onSecondary = Color(0xFFFFFFFF), // White text on cyan
    secondaryContainer = Color(0xFF0891B2), // Darker cyan
    onSecondaryContainer = Color(0xFFFFFFFF), // White text on container
    
    // Tertiary: Gold accent (for special elements)
    tertiary = Color(0xFFF59E0B), // Gold/Amber
    onTertiary = Color(0xFF000000), // Black text on gold
    tertiaryContainer = Color(0xFFD97706), // Darker gold
    onTertiaryContainer = Color(0xFFFFFFFF), // White text
    
    // Error: Red for errors
    error = Color(0xFFEF4444), // Red
    onError = Color(0xFFFFFFFF), // White text on red
    errorContainer = Color(0xFFDC2626), // Darker red
    onErrorContainer = Color(0xFFFFFFFF), // White text
    
    // Background: Very dark gray #18181B - Main app background
    background = Color(0xFF18181B), // Stitch Dark Background
    onBackground = Color(0xFFFFFFFF), // White text on background
    
    // Surface: Dark gray #27272A - Card backgrounds (collection cards)
    surface = Color(0xFF27272A), // Stitch Card Background
    onSurface = Color(0xFFFFFFFF), // White text on dark cards
    surfaceDim = Color(0xFF18181B), // Even darker for dimmed surfaces
    surfaceBright = Color(0xFF27272A), // Brighter surface
    
    // Surface Variant: For input fields and secondary surfaces
    surfaceVariant = Color(0xFF27272A), // Dark gray for inputs
    onSurfaceVariant = Color(0xFFA1A1AA), // Light gray text (Stitch Text Secondary)
    
    // Outline: For borders and dividers
    outline = Color(0xFF3F3F46), // Subtle border
    outlineVariant = Color(0xFF27272A) // Variant border
)

/**
 * Light Color Scheme - For quote cards (white background)
 * Quote cards use light theme on dark background for contrast
 */
val StitchLightScheme = lightColorScheme(
    // Primary: Same purple for consistency
    primary = Color(0xFFA855F7), // Stitch Purple
    onPrimary = Color(0xFFFFFFFF), // White text
    primaryContainer = Color(0xFFE9D5FF), // Light purple container
    onPrimaryContainer = Color(0xFF581C87), // Dark purple text
    
    // Secondary: Same cyan
    secondary = Color(0xFF06B6D4), // Stitch Cyan
    onSecondary = Color(0xFFFFFFFF), // White text
    secondaryContainer = Color(0xFFCFFAFE), // Light cyan container
    onSecondaryContainer = Color(0xFF164E63), // Dark cyan text
    
    // Tertiary
    tertiary = Color(0xFFF59E0B), // Gold
    onTertiary = Color(0xFFFFFFFF), // White text
    tertiaryContainer = Color(0xFFFEF3C7), // Light gold container
    onTertiaryContainer = Color(0xFF78350F), // Dark gold text
    
    // Error
    error = Color(0xFFEF4444), // Red
    onError = Color(0xFFFFFFFF), // White text
    errorContainer = Color(0xFFFEE2E2), // Light red container
    onErrorContainer = Color(0xFF991B1B), // Dark red text
    
    // Background: White for quote cards #F5F5F5
    background = Color(0xFFF5F5F5), // Stitch Quote Card Background (light gray-white)
    onBackground = Color(0xFF000000), // Black text on white cards
    
    // Surface: White for quote cards
    surface = Color(0xFFF5F5F5), // White/light gray for quote cards
    onSurface = Color(0xFF000000), // Black text on white
    surfaceDim = Color(0xFFF5F5F5), // Same for quote cards
    surfaceBright = Color(0xFFFFFFFF), // Pure white for brightness
    
    // Surface Variant
    surfaceVariant = Color(0xFFE5E7EB), // Light gray variant
    onSurfaceVariant = Color(0xFF6B7280), // Gray text
    
    // Outline
    outline = Color(0xFFD1D5DB), // Light border
    outlineVariant = Color(0xFFE5E7EB) // Lighter variant
)

// Legacy support - map to Stitch Dark Scheme
val LightPurpleScheme = StitchDarkScheme
val LightColorScheme = StitchDarkScheme
val DarkColorScheme = StitchDarkScheme

// Dark Purple Scheme - Extract from dark mode versions or adjust light colors
val DarkPurpleScheme = darkColorScheme(
    // Primary: Lighter version of light primary for dark theme
    primary = Color(0xFFCE93D8), // TODO: Extract from Stitch dark - button color
    onPrimary = Color(0xFF6A1B9A), // TODO: Extract from Stitch dark - text on buttons
    primaryContainer = Color(0xFF4A148C), // TODO: Extract from Stitch dark - container
    onPrimaryContainer = Color(0xFFE1BEE7), // TODO: Extract from Stitch dark - text on container
    
    // Secondary
    secondary = Color(0xFFCE93D8), // TODO: Extract from Stitch dark
    onSecondary = Color(0xFF6A1B9A),
    secondaryContainer = Color(0xFF4A148C),
    onSecondaryContainer = Color(0xFFE1BEE7),
    
    // Tertiary
    tertiary = Color(0xFFAB47BC), // TODO: Extract from Stitch dark
    onTertiary = Color(0xFF4A148C),
    tertiaryContainer = Color(0xFF6A1B9A),
    onTertiaryContainer = Color(0xFFCE93D8),
    
    // Error
    error = Color(0xFFFFB4AB), // TODO: Extract from Stitch dark - error color
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    // Background: Extract from dark mode screenshots
    background = Color(0xFF1C1B1F), // TODO: Extract from Stitch dark - main background
    onBackground = Color(0xFFE6E1E5), // TODO: Extract from Stitch dark - main text
    
    // Surface: Extract from dark mode cards
    surface = Color(0xFF1C1B1F), // TODO: Extract from Stitch dark - card background
    onSurface = Color(0xFFE6E1E5), // TODO: Extract from Stitch dark - card text
    
    // Surface Variant
    surfaceVariant = Color(0xFF49454F), // TODO: Extract from Stitch dark - input background
    onSurfaceVariant = Color(0xFFCAC4D0), // TODO: Extract from Stitch dark - input text
    
    // Outline
    outline = Color(0xFF938F99), // TODO: Extract from Stitch dark - border
    outlineVariant = Color(0xFF49454F) // TODO: Extract from Stitch dark - subtle border
)

// ============================================================================
// STITCH ACCENT COLOR SCHEMES (For different accent colors)
// ============================================================================

// Blue Color Scheme (uses Cyan as primary)
val LightBlueScheme = StitchDarkScheme.copy(
    primary = Color(0xFF06B6D4), // Cyan as primary
    secondary = Color(0xFFA855F7) // Purple as secondary
)

val DarkBlueScheme = StitchDarkScheme.copy(
    primary = Color(0xFF06B6D4), // Cyan as primary
    secondary = Color(0xFFA855F7) // Purple as secondary
)

// Green Color Scheme (for future use)
val LightGreenScheme = StitchDarkScheme.copy(
    primary = Color(0xFF10B981), // Green
    secondary = Color(0xFF06B6D4) // Cyan as secondary
)

val DarkGreenScheme = StitchDarkScheme.copy(
    primary = Color(0xFF10B981), // Green
    secondary = Color(0xFF06B6D4) // Cyan as secondary
)

// ============================================================================
// STITCH DESIGN COLORS (Specific color values)
// ============================================================================

// Main Colors
val StitchPrimary = Color(0xFFA855F7) // Purple - Primary brand color
val StitchSecondary = Color(0xFF06B6D4) // Cyan - Secondary accent
val StitchBackground = Color(0xFF18181B) // Dark background
val StitchCardBackground = Color(0xFF27272A) // Dark card background
val StitchQuoteCardBackground = Color(0xFFF5F5F5) // White/light gray for quote cards
val StitchTextPrimary = Color(0xFFFFFFFF) // White text
val StitchTextSecondary = Color(0xFFA1A1AA) // Light gray text

// Quote Card Colors
val QuoteCardBackground = Color(0xFFF5F5F5) // Stitch Quote Card Background (white/light gray)
val DarkQuoteCardBackground = Color(0xFF27272A) // Dark card background (collection cards)
val QuoteCardText = Color(0xFF000000) // Black text on white quote cards
val QuoteCardAuthor = Color(0xFF6B7280) // Gray author text on white cards

// Category Accent Colors - Stitch design uses primary/secondary colors
val MotivationAccent = Color(0xFFA855F7) // Purple
val LoveAccent = Color(0xFFEC4899) // Pink
val SuccessAccent = Color(0xFF10B981) // Green
val WisdomAccent = Color(0xFF8B5CF6) // Purple variant
val HumorAccent = Color(0xFFF59E0B) // Gold/Amber
val GeneralAccent = Color(0xFF06B6D4) // Cyan

// Gradient Colors
val StitchGradientStart = Color(0xFFA855F7) // Purple
val StitchGradientEnd = Color(0xFF06B6D4) // Cyan

/**
 * Stitch gradient brush - Purple to Cyan
 * Used for Quote of the Day card, FAB, buttons
 */
val StitchGradientBrush = Brush.linearGradient(
    colors = listOf(
        StitchGradientStart,
        StitchGradientEnd
    )
)

/**
 * Stitch gradient brush - Horizontal
 */
val StitchGradientBrushHorizontal = Brush.horizontalGradient(
    colors = listOf(
        StitchGradientStart,
        StitchGradientEnd
    )
)

/**
 * Stitch gradient brush - Vertical
 */
val StitchGradientBrushVertical = Brush.verticalGradient(
    colors = listOf(
        StitchGradientStart,
        StitchGradientEnd
    )
)
