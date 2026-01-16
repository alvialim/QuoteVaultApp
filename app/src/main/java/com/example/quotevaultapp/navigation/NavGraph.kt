package com.example.quotevaultapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.quotevaultapp.domain.repository.AuthRepository
import com.example.quotevaultapp.presentation.auth.ForgotPasswordScreen
import com.example.quotevaultapp.presentation.auth.LoginScreen
import com.example.quotevaultapp.presentation.auth.SignUpScreen
import com.example.quotevaultapp.presentation.collections.CollectionDetailScreen
import com.example.quotevaultapp.presentation.collections.CollectionDetailViewModel
import com.example.quotevaultapp.presentation.collections.CollectionsScreen
import com.example.quotevaultapp.presentation.favorites.FavoritesScreen
import com.example.quotevaultapp.presentation.home.HomeScreen
import com.example.quotevaultapp.presentation.profile.ProfileScreen
import com.example.quotevaultapp.presentation.quote.QuoteDetailScreen
import com.example.quotevaultapp.presentation.quote.QuoteDetailViewModel
import com.example.quotevaultapp.presentation.settings.SettingsScreen
import com.example.quotevaultapp.presentation.splash.SplashScreen
import kotlinx.coroutines.flow.first

/**
 * Main navigation graph for the app
 * Handles auth state-based navigation and deep linking
 */
@Composable
fun QuoteVaultNavGraph(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository,
    startDestination: String = Screen.Splash.route
) {
    // Observe auth state - convert Flow to State
    var authState by remember { mutableStateOf<com.example.quotevaultapp.domain.model.User?>(null) }
    
    LaunchedEffect(Unit) {
        authRepository.observeAuthState().collect { user ->
            authState = user
        }
    }
    
    // Determine start destination based on auth state
    val initialRoute = remember(startDestination) {
        if (authState != null && startDestination == Screen.Splash.route) {
            Screen.Home.route
        } else {
            startDestination
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = initialRoute
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                authRepository = authRepository
            )
        }
        
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Main App Screens with Bottom Navigation (Nested Graph)
        composable(Screen.Home.route) {
            HomeScreen(
                onQuoteClick = { quoteId ->
                    navController.navigate(Screen.QuoteDetail.createRoute(quoteId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onQuoteClick = { quoteId ->
                    navController.navigate(Screen.QuoteDetail.createRoute(quoteId))
                }
            )
        }
        
        composable(Screen.Collections.route) {
            CollectionsScreen(
                onCollectionClick = { collectionId ->
                    navController.navigate(Screen.CollectionDetail.createRoute(collectionId))
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Detail Screens with Arguments
        composable(
            route = Screen.QuoteDetail.route,
            arguments = Screen.QuoteDetail.arguments,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = DeepLink.QUOTE_DETAIL
                }
            )
        ) { backStackEntry ->
            val quoteId = backStackEntry.arguments?.getString("quoteId") ?: ""
            
            QuoteDetailScreen(
                quoteId = quoteId,
                onBack = {
                    navController.popBackStack()
                },
                viewModel = hiltViewModel<QuoteDetailViewModel>()
            )
        }
        
        composable(
            route = Screen.CollectionDetail.route,
            arguments = Screen.CollectionDetail.arguments,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = DeepLink.COLLECTION_DETAIL
                }
            )
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
            
            CollectionDetailScreen(
                collectionId = collectionId,
                onBack = {
                    navController.popBackStack()
                },
                viewModel = hiltViewModel<CollectionDetailViewModel>()
            )
        }
        
        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        val currentRoute = navController.currentDestination?.route
        
        when {
            authState == null && currentRoute !in listOf(
                Screen.Login.route,
                Screen.SignUp.route,
                Screen.ForgotPassword.route,
                Screen.Splash.route
            ) -> {
                // User signed out - navigate to login
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            authState != null && currentRoute in listOf(
                Screen.Login.route,
                Screen.SignUp.route,
                Screen.ForgotPassword.route
            ) -> {
                // User signed in - navigate to home
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}

