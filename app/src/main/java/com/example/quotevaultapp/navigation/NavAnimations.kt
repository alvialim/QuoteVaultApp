package com.example.quotevaultapp.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

/**
 * Navigation animation specifications for smooth transitions
 */
object NavAnimations {
    
    /**
     * Enter animation for screen transitions (slide in from right)
     */
    fun slideInFromRight() = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(300))
    
    /**
     * Exit animation for screen transitions (slide out to left)
     */
    fun slideOutToLeft() = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))
    
    /**
     * Enter animation for pop transitions (slide in from left)
     */
    fun slideInFromLeft() = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(300))
    
    /**
     * Exit animation for pop transitions (slide out to right)
     */
    fun slideOutToRight() = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))
    
    /**
     * Fade animation for dialog-like screens
     */
    fun fadeInAnimation() = fadeIn(animationSpec = tween(300))
    fun fadeOutAnimation() = fadeOut(animationSpec = tween(300))
    
    /**
     * Vertical slide animation (for bottom sheets)
     */
    fun slideInVerticallyFromBottom() = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(300))
    
    fun slideOutVerticallyToBottom() = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))
}
