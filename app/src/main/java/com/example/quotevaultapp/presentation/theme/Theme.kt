package com.example.quotevaultapp.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.quotevaultapp.domain.model.AccentColor
import com.example.quotevaultapp.domain.model.AppTheme
import com.example.quotevaultapp.domain.model.FontSize

/**
 * QuoteVaultApp Material 3 Theme
 * Supports light, dark, and system theme modes with multiple accent colors
 * 
 * @param theme User's theme preference (LIGHT, DARK, or SYSTEM)
 * @param accentColor User's accent color preference (PURPLE, BLUE, GREEN)
 * @param dynamicColor Whether to use dynamic colors on Android 12+ (default: false, uses accent colors instead)
 * @param content The composable content to apply the theme to
 */
@Composable
fun QuoteVaultTheme(
    theme: AppTheme = AppTheme.SYSTEM,
    accentColor: AccentColor = AccentColor.PURPLE,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Determine dark theme based on app theme preference
    val darkTheme = when (theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    
    // Select color scheme based on accent color and theme
    val colorScheme = when {
        // Dynamic colors on Android 12+ (only if enabled)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        // Use Stitch color schemes based on accent color
        // Stitch design uses dark theme by default (#18181B background)
        darkTheme -> when (accentColor) {
            AccentColor.PURPLE -> StitchDarkScheme // Default: Purple primary
            AccentColor.BLUE -> LightBlueScheme // Cyan primary
            AccentColor.GREEN -> DarkGreenScheme // Green primary
        }
        else -> when (accentColor) {
            // Light theme uses StitchLightScheme for quote cards on dark background
            AccentColor.PURPLE -> StitchLightScheme // White cards for contrast
            AccentColor.BLUE -> LightBlueScheme
            AccentColor.GREEN -> LightGreenScheme
        }
    }
    
    // Apply system UI styling (edge-to-edge)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            
            // Enable edge-to-edge
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Get quote text style based on font size preference
 */
@Composable
fun quoteTextStyle(fontSize: com.example.quotevaultapp.domain.model.FontSize) = when (fontSize) {
    com.example.quotevaultapp.domain.model.FontSize.SMALL -> QuoteTypography.Small
    com.example.quotevaultapp.domain.model.FontSize.MEDIUM -> QuoteTypography.Medium
    com.example.quotevaultapp.domain.model.FontSize.LARGE -> QuoteTypography.Large
}

/**
 * CompositionLocal for FontSize to be accessed throughout the composition tree
 */
val LocalFontSize = compositionLocalOf { FontSize.MEDIUM }
