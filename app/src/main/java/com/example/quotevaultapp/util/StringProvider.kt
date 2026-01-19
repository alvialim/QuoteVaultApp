package com.example.quotevaultapp.util

import android.content.Context
import androidx.annotation.StringRes

/**
 * StringProvider utility class for ViewModels
 * Provides string resources without requiring Context injection
 * 
 * ViewModels should use this to access string resources without
 * directly depending on Android Context
 */
class StringProvider(private val context: Context) {
    
    /**
     * Get a string resource by ID
     * 
     * @param resId String resource ID
     * @return The string value
     */
    fun getString(@StringRes resId: Int): String = context.getString(resId)
    
    /**
     * Get a formatted string resource with arguments
     * 
     * @param resId String resource ID
     * @param args Format arguments
     * @return The formatted string value
     */
    fun getString(@StringRes resId: Int, vararg args: Any): String =
        context.getString(resId, *args)
    
    /**
     * Get a string resource with quantity support (plurals)
     * 
     * @param resId Plural resource ID
     * @param quantity The quantity for plural selection
     * @return The appropriate plural string
     */
    fun getQuantityString(@StringRes resId: Int, quantity: Int): String =
        context.resources.getQuantityString(resId, quantity, quantity)
    
    /**
     * Get a string resource with quantity and format arguments
     * 
     * @param resId Plural resource ID
     * @param quantity The quantity for plural selection
     * @param args Format arguments
     * @return The formatted plural string
     */
    fun getQuantityString(@StringRes resId: Int, quantity: Int, vararg args: Any): String =
        context.resources.getQuantityString(resId, quantity, *args)
}
