package com.example.quotevaultapp.util

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.haptics.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Utility object for haptic feedback
 */
object HapticFeedback {
    
    /**
     * Trigger haptic feedback when button is pressed
     * Use this modifier on clickable elements that need haptic feedback
     */
    @Composable
    fun Modifier.withHapticFeedback(
        onPress: () -> Unit
    ): Modifier {
        val haptic = LocalHapticFeedback.current
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        
        LaunchedEffect(isPressed) {
            if (isPressed) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
        
        return this.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPress()
            }
        )
    }
    
    /**
     * Perform haptic feedback for button click
     */
    @Composable
    fun performButtonClick() {
        LocalHapticFeedback.current.performHapticFeedback(
            HapticFeedbackType.LongPress
        )
    }
    
    /**
     * Perform haptic feedback for success action
     */
    @Composable
    fun performSuccess() {
        LocalHapticFeedback.current.performHapticFeedback(
            HapticFeedbackType.TextHandleMove
        )
    }
    
    /**
     * Perform haptic feedback for error
     */
    @Composable
    fun performError() {
        LocalHapticFeedback.current.performHapticFeedback(
            HapticFeedbackType.LongPress
        )
    }
}
