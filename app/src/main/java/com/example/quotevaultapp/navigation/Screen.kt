package com.example.quotevaultapp.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Screen route definitions for type-safe navigation
 */
sealed class Screen(val route: String) {
    // Auth screens
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    
    // Main app screens (bottom navigation)
    object Home : Screen("home")
    object Favorites : Screen("favorites")
    object Collections : Screen("collections")
    object Profile : Screen("profile")
    
    // Detail screens with arguments
    object QuoteDetail : Screen("quote_detail/{quoteId}") {
        fun createRoute(quoteId: String) = "quote_detail/$quoteId"
        
        val arguments = listOf(
            navArgument("quoteId") {
                type = NavType.StringType
            }
        )
    }
    
    object CollectionDetail : Screen("collection_detail/{collectionId}") {
        fun createRoute(collectionId: String) = "collection_detail/$collectionId"
        
        val arguments = listOf(
            navArgument("collectionId") {
                type = NavType.StringType
            }
        )
    }
    
    // Settings
    object Settings : Screen("settings")
}

/**
 * Deep link patterns for notifications
 */
object DeepLink {
    const val QUOTE_DETAIL = "quotevaultapp://quote/{quoteId}"
    const val COLLECTION_DETAIL = "quotevaultapp://collection/{collectionId}"
    const val HOME = "quotevaultapp://home"
    const val FAVORITES = "quotevaultapp://favorites"
}
