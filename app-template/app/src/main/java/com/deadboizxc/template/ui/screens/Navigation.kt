package com.deadboizxc.template.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * =============================================================================
 * Navigation - маршрутизация приложения
 * =============================================================================
 */

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{id}"
    
    fun detail(id: String) = "detail/$id"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToDetail = { id -> navController.navigate(Routes.detail(id)) }
            )
        }
        
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.DETAIL) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DetailScreen(
                id = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
