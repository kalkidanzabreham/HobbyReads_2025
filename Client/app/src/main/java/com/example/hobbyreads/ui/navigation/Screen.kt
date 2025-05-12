package com.example.hobbyreads.ui.navigation


sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
    object Books : Screen("books")

    // Routes with parameters
    object BookDetail : Screen("books/{bookId}") {
        fun createRoute(bookId: String) = "books/$bookId"
    }

    object AddBook : Screen("books/add")
    object EditBook : Screen("books/edit/{bookId}") {
        fun createRoute(bookId: String) = "books/edit/$bookId"
    }

    object Connections : Screen("connections")
    object Trades : Screen("trades")

    object AdminDashboard : Screen("admin_dashboard")
    object Users : Screen("admin_users")
    object Hobbies : Screen("admin_hobbies")
    object EditAdmin : Screen("admin_edit")
}