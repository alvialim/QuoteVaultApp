package com.example.quotevaultapp.util

import com.example.quotevaultapp.BuildConfig

/**
 * Application-wide constants
 * All magic numbers, strings, and configuration values should be defined here
 */
object Constants {
    
    // ============================================================================
    // SUPABASE CONFIGURATION
    // ============================================================================
    const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    const val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY
    
    // ============================================================================
    // PAGINATION
    // ============================================================================
    /**
     * Default number of items per page
     */
    const val DEFAULT_PAGE_SIZE = 20
    
    /**
     * Initial page number (0-indexed)
     */
    const val INITIAL_PAGE = 0
    
    /**
     * Number of items before the end to trigger loading more
     */
    const val PAGINATION_TRIGGER_THRESHOLD = 3
    
    // ============================================================================
    // PREFERENCES
    // ============================================================================
    /**
     * DataStore preferences file name
     */
    const val PREFERENCES_NAME = "quote_vault_prefs"
    
    // ============================================================================
    // NOTIFICATIONS
    // ============================================================================
    /**
     * Notification channel ID for daily quotes
     */
    const val NOTIFICATION_CHANNEL_ID = "daily_quote_channel"
    
    /**
     * Notification channel name displayed to users
     */
    const val NOTIFICATION_CHANNEL_NAME = "Daily Quote"
    
    /**
     * Notification channel description
     */
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Notifications for your daily inspirational quotes"
    
    /**
     * Default notification ID
     */
    const val NOTIFICATION_ID = 1001
    
    /**
     * Unique work name for daily quote notification
     */
    const val WORK_NAME_DAILY_QUOTE = "daily_quote_work"
    
    /**
     * Default notification hour (24-hour format)
     */
    const val DEFAULT_NOTIFICATION_HOUR = 9
    
    /**
     * Default notification minute
     */
    const val DEFAULT_NOTIFICATION_MINUTE = 0
    
    /**
     * Notification repeat interval in hours
     */
    const val NOTIFICATION_REPEAT_INTERVAL_HOURS = 24L
    
    // ============================================================================
    // SHARING & EXPORT
    // ============================================================================
    /**
     * Quote card image width in pixels (1080p)
     */
    const val QUOTE_CARD_WIDTH = 1080
    
    /**
     * Quote card image height in pixels (1920p)
     */
    const val QUOTE_CARD_HEIGHT = 1920
    
    /**
     * JPEG compression quality (0-100)
     */
    const val JPEG_QUALITY = 85
    
    /**
     * PNG compression quality (0-9, not used currently)
     */
    const val PNG_QUALITY = 6
    
    /**
     * Directory name for saved quote cards
     */
    const val QUOTE_CARDS_DIRECTORY = "QuoteVault"
    
    // ============================================================================
    // DEEP LINKS
    // ============================================================================
    /**
     * Deep link scheme for the app
     */
    const val DEEP_LINK_SCHEME = "quotevault"
    
    /**
     * Deep link path for quote detail
     */
    const val DEEP_LINK_QUOTE_DETAIL = "quote"
    
    /**
     * Deep link path for daily quote
     */
    const val DEEP_LINK_DAILY_QUOTE = "daily-quote"
    
    // ============================================================================
    // FILE PROVIDER
    // ============================================================================
    /**
     * FileProvider authority
     */
    const val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"
    
    /**
     * Cache directory name for file provider
     */
    const val FILE_PROVIDER_CACHE_DIR = "quote_cards"
    
    // ============================================================================
    // NETWORK & TIMEOUTS
    // ============================================================================
    /**
     * Network request timeout in seconds
     */
    const val NETWORK_TIMEOUT_SECONDS = 30L
    
    /**
     * Debounce delay for search input in milliseconds
     */
    const val DEBOUNCE_DELAY_MS = 300L
    
    /**
     * Retry delay for failed requests in milliseconds
     */
    const val RETRY_DELAY_MS = 1000L
    
    /**
     * Maximum number of retry attempts
     */
    const val MAX_RETRY_ATTEMPTS = 3
    
    // ============================================================================
    // UI CONSTANTS
    // ============================================================================
    /**
     * Maximum length for quote preview text
     */
    const val MAX_QUOTE_PREVIEW_LENGTH = 150
    
    /**
     * Default animation duration in milliseconds
     */
    const val ANIMATION_DURATION_MS = 300
    
    /**
     * Long animation duration in milliseconds
     */
    const val ANIMATION_DURATION_LONG_MS = 500
    
    /**
     * Default elevation for cards in dp
     */
    const val DEFAULT_CARD_ELEVATION_DP = 4
    
    /**
     * Default padding for screens in dp
     */
    const val DEFAULT_SCREEN_PADDING_DP = 16
    
    /**
     * Default spacing between cards in dp
     */
    const val DEFAULT_CARD_SPACING_DP = 16
    
    /**
     * Default corner radius for cards in dp
     */
    const val DEFAULT_CARD_CORNER_RADIUS_DP = 20
    
    /**
     * Default corner radius for buttons in dp
     */
    const val DEFAULT_BUTTON_CORNER_RADIUS_DP = 16
    
    // ============================================================================
    // VALIDATION
    // ============================================================================
    /**
     * Minimum password length
     */
    const val MIN_PASSWORD_LENGTH = 6
    
    /**
     * Maximum password length (optional, for validation)
     */
    const val MAX_PASSWORD_LENGTH = 128
    
    /**
     * Maximum display name length
     */
    const val MAX_DISPLAY_NAME_LENGTH = 50
    
    /**
     * Maximum collection name length
     */
    const val MAX_COLLECTION_NAME_LENGTH = 50
    
    /**
     * Maximum collection description length
     */
    const val MAX_COLLECTION_DESCRIPTION_LENGTH = 200
    
    /**
     * Minimum collection name length
     */
    const val MIN_COLLECTION_NAME_LENGTH = 1
    
    // ============================================================================
    // CACHING
    // ============================================================================
    /**
     * Cache duration for quotes in milliseconds (24 hours)
     */
    const val QUOTE_CACHE_DURATION_MS = 24 * 60 * 60 * 1000L
    
    /**
     * Cache duration for user profile in milliseconds (1 hour)
     */
    const val USER_PROFILE_CACHE_DURATION_MS = 60 * 60 * 1000L
    
    // ============================================================================
    // LOGGING
    // ============================================================================
    /**
     * Application-wide log tag
     */
    const val LOG_TAG = "QuoteVault"
    
    /**
     * Maximum log message length (truncate if longer)
     */
    const val MAX_LOG_MESSAGE_LENGTH = 1000
    
    // ============================================================================
    // PERMISSIONS
    // ============================================================================
    /**
     * Request code for notification permission
     */
    const val REQUEST_CODE_NOTIFICATION_PERMISSION = 1001
    
    /**
     * Request code for storage permission
     */
    const val REQUEST_CODE_STORAGE_PERMISSION = 1002
    
    // ============================================================================
    // DATE/TIME FORMATTING
    // ============================================================================
    /**
     * Default date format pattern
     */
    const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd"
    
    /**
     * Default time format pattern (12-hour)
     */
    const val DEFAULT_TIME_FORMAT_12H = "h:mm a"
    
    /**
     * Default time format pattern (24-hour)
     */
    const val DEFAULT_TIME_FORMAT_24H = "HH:mm"
    
    // ============================================================================
    // QUOTE OF THE DAY
    // ============================================================================
    /**
     * Cache key for quote of the day
     */
    const val QOTD_CACHE_KEY = "quote_of_the_day"
    
    /**
     * Cache timestamp key for quote of the day
     */
    const val QOTD_CACHE_TIMESTAMP_KEY = "quote_of_the_day_timestamp"
}
