package com.example.quotevaultapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.quotevaultapp.navigation.NavAnimations.fadeInAnimation
import com.example.quotevaultapp.navigation.NavAnimations.fadeOutAnimation
import com.example.quotevaultapp.navigation.NavAnimations.slideInFromLeft
import com.example.quotevaultapp.navigation.NavAnimations.slideInFromRight
import com.example.quotevaultapp.navigation.NavAnimations.slideOutToLeft
import com.example.quotevaultapp.navigation.NavAnimations.slideOutToRight
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
import com.example.quotevaultapp.navigation.DeepLinks

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
    // Include authState in remember keys so it recomputes when auth state changes
    val initialRoute = remember(startDestination, authState) {
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
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeInAnimation() },
            exitTransition = { fadeOutAnimation() }
        ) {
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
        composable(
            route = Screen.Login.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
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
        
        composable(
            route = Screen.SignUp.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
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
        
        composable(
            route = Screen.ForgotPassword.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            ForgotPasswordScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Main App Screens with Bottom Navigation (Nested Graph)
        composable(
            route = Screen.Home.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = DeepLinks.HOME
                },
                navDeepLink {
                    uriPattern = DeepLinks.DAILY_QUOTE
                }
            ),
            enterTransition = { fadeInAnimation() },
            exitTransition = { fadeOutAnimation() }
        ) {
            HomeScreen(
                navController = navController,
                onQuoteClick = { quote ->
                    navController.navigate(Screen.QuoteDetail.createRoute(quote.id))
                }
            )
        }
        
        composable(
            route = Screen.Favorites.route,
            enterTransition = { fadeInAnimation() },
            exitTransition = { fadeOutAnimation() }
        ) {
            FavoritesScreen(
                onQuoteClick = { quote ->
                    navController.navigate(Screen.QuoteDetail.createRoute(quote.id))
                }
            )
        }
        
        composable(
            route = Screen.Collections.route,
            enterTransition = { fadeInAnimation() },
            exitTransition = { fadeOutAnimation() }
        ) {
            CollectionsScreen(
                onCollectionClick = { collection ->
                    navController.navigate(Screen.CollectionDetail.createRoute(collection.id))
                }
            )
        }
        
        composable(
            route = Screen.Profile.route,
            enterTransition = { fadeInAnimation() },
            exitTransition = { fadeOutAnimation() }
        ) {
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
                    uriPattern = DeepLinks.QUOTE_DETAIL
                },
                navDeepLink {
                    uriPattern = DeepLink.QUOTE_DETAIL // Legacy support
                }
            ),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val quoteId = backStackEntry.arguments?.getString("quoteId") ?: ""
            
            QuoteDetailScreen(
                quoteId = quoteId,
                onBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel<QuoteDetailViewModel>()
            )
        }
        
        composable(
            route = Screen.CollectionDetail.route,
            arguments = Screen.CollectionDetail.arguments,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = DeepLink.COLLECTION_DETAIL
                }
            ),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
            
            CollectionDetailScreen(
                collectionId = collectionId,
                onBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel<CollectionDetailViewModel>()
            )
        }
        
        // Settings Screen
        composable(
            route = Screen.Settings.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
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

