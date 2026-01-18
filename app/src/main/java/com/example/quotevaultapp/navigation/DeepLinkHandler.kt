package com.example.quotevaultapp.navigation

/**
 * Deep link constants and utilities for handling navigation from external sources
 * (notifications, widgets, external apps)
 */
object DeepLinks {
    /**
     * Deep link pattern for quote detail screen
     * Format: quotevault://quote/{quoteId}
     */
    const val QUOTE_DETAIL = "quotevault://quote/{quoteId}"
    
    /**
     * Deep link pattern for daily quote screen
     * Opens home screen and highlights quote of the day
     * Format: quotevault://daily-quote
     */
    const val DAILY_QUOTE = "quotevault://daily-quote"
    
    /**
     * Deep link pattern for home screen
     * Format: quotevault://home
     */
    const val HOME = "quotevault://home"
    
    /**
     * Deep link pattern for favorites screen
     * Format: quotevault://favorites
     */
    const val FAVORITES = "quotevault://favorites"
    
    /**
     * Deep link pattern for collections screen
     * Format: quotevault://collections
     */
    const val COLLECTIONS = "quotevault://collections"
    
    /**
     * Deep link pattern for collection detail
     * Format: quotevault://collection/{collectionId}
     */
    const val COLLECTION_DETAIL = "quotevault://collection/{collectionId}"
}
