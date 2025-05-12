package com.example.hobbyreads.util

object Constants {
    // For emulator
//    const val BASE_URL = "http://10.0.2.2:8080/api/"

    // For physical device, use your computer's IP address
     const val BASE_URL = "http://192.168.8.73:8080/api/"
     const val Image_URL = "http://192.168.8.73:8080/uploads/books/"

    // API endpoints
    const val LOGIN_ENDPOINT = "auth/login"
    const val REGISTER_ENDPOINT = "auth/register"
    const val PROFILE_ENDPOINT = "auth/profile"

    // Shared Preferences
    const val PREFS_NAME = "hobby_prefs"
    const val KEY_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_PASSWORD = "password" // Note: In a production app, consider more secure storage
}
