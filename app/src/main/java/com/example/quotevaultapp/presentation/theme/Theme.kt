package com.example.quotevaultapp.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.quotevaultapp.domain.model.AppTheme

/**
 * QuoteVaultApp Material 3 Theme
 * Supports light, dark, and system theme modes with dynamic colors on Android 12+
 * 
 * @param darkTheme Whether to use dark theme (auto-detected from system if not specified)
 * @param appTheme User's theme preference (LIGHT, DARK, or SYSTEM)
 * @param dynamicColor Whether to use dynamic colors on Android 12+ (default: true)
 * @param content The composable content to apply the theme to
 */
@Composable
fun QuoteVaultTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Determine dark theme based on app theme preference
    val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    
    // Select color scheme
    val colorScheme = when {
        // Dynamic colors on Android 12+ (API 31+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        // Fallback to custom color schemes
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
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
