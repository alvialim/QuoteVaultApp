package com.example.quotevaultapp.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Composable that provides bottom navigation bar
 * Should be used in Scaffold's bottomBar parameter
 */
@Composable
fun MainBottomNavigationBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    val showBottomNav = currentRoute in listOf(
        "home",
        "favorites", 
        "collections",
        "profile"
    )
    
    if (showBottomNav) {
        BottomNavigationBar(
            currentRoute = currentRoute ?: "home",
            onNavigate = { route ->
                navController.navigate(route) {
                    // Pop up to the start destination of the graph to avoid building up
                    // a large stack of destinations on the back stack
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when reselecting the same item
                    launchSingleTop = true
                    // Restore state when reselecting a previously selected item
                    restoreState = true
                }
            },
            modifier = modifier
        )
    }
}
