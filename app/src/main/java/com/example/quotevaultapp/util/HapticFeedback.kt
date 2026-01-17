package com.example.quotevaultapp.util

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView

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
        val view = LocalView.current
        val interactionSource = remember { MutableInteractionSource() }
        val isPressedState = interactionSource.collectIsPressedAsState()
        
        LaunchedEffect(isPressedState.value) {
            if (isPressedState.value) {
                view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
            }
        }
        
        return this.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onPress()
            }
        )
    }
    
    /**
     * Perform haptic feedback for button click
     */
    @Composable
    fun performButtonClick() {
        val view = LocalView.current
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
    
    /**
     * Perform haptic feedback for success action
     */
    @Composable
    fun performSuccess() {
        val view = LocalView.current
        view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
    }
    
    /**
     * Perform haptic feedback for error
     */
    @Composable
    fun performError() {
        val view = LocalView.current
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}
