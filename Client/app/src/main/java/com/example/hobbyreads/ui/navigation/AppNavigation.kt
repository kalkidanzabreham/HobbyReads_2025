package com.example.hobbyreads.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hobbyreads.data.api.ApiService
import com.example.hobbyreads.data.repository.AuthRepository
import com.example.hobbyreads.data.repository.TokenManager
import com.example.hobbyreads.ui.screens.admin.AdminDashboardScreen
import com.example.hobbyreads.ui.screens.admin.HobbiesScreen
import com.example.hobbyreads.ui.screens.admin.UsersScreen
import com.example.hobbyreads.ui.screens.auth.LoginScreen
import com.example.hobbyreads.ui.screens.auth.RegisterScreen
import com.example.hobbyreads.ui.screens.books.AddBookScreen
import com.example.hobbyreads.ui.screens.books.BookDetailScreen
import com.example.hobbyreads.ui.screens.books.BooksScreen
import com.example.hobbyreads.ui.screens.connections.ConnectionsScreen
import com.example.hobbyreads.ui.screens.dashboard.DashboardScreen
import com.example.hobbyreads.ui.screens.landing.LandingScreen
import com.example.hobbyreads.ui.screens.profile.ProfileScreen
import com.example.hobbyreads.ui.screens.trades.TradesScreen
import com.google.gson.Gson

@Composable
fun AppNavigation(
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

        composable(route = Screen.Books.route) {
            BooksScreen(navController = navController)
        }

        composable(route = Screen.BookDetail.route + "/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")?.toIntOrNull() ?: -1
            BookDetailScreen(
                navController = navController,
                bookId = bookId, // Now always Int, never null
            )
        }


        composable(route = Screen.AddBook.route) {
            AddBookScreen(navController = navController)
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

        composable(Screen.Trades.route) {
            TradesScreen(navController = navController)
        }
    }
}
