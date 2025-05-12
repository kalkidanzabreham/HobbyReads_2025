package com.example.hobbyreads.ui.navigation

import androidx.navigation.NavHostController
import com.example.hobbyreads.data.api.ApiService
import com.example.hobbyreads.data.repository.TokenManager
import com.google.gson.Gson

class AppNavigation(
    navController: NavHostController,
    apiService: ApiService,
    tokenManager: TokenManager,
    gson: Gson
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Landing.route
    ) {
        composable(route = Screen.Landing.route) {
            LandingScreen(navController = navController)
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                navController = navController,

            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                navController = navController,

            )
        }

        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,

            )
        }
    composable(route = Screen.Connections.route) {
            ConnectionsScreen(
                navController = navController,

            )
        }
        composable(route = Screen.AdminDashboard.route) { AdminDashboardScreen(navController) }
        composable(route = Screen.Users.route) { UsersScreen(navController) }
        composable(route = Screen.Hobbies.route) { HobbiesScreen(navController) }

         composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
}
}