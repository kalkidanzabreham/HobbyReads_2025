package com.example.hobbyreads.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.hobby.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        Log.d("TokenManager", "Saving token: ${token.take(10)}...")
        prefs.edit().putString(Constants.KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        val token = prefs.getString(Constants.KEY_TOKEN, null)
        Log.d("TokenManager", "Getting token: ${token?.take(10) ?: "null"}...")
        return token
    }

    fun clearToken() {
        Log.d("TokenManager", "Clearing token")
        prefs.edit().remove(Constants.KEY_TOKEN).apply()
    }

    fun saveUserCredentials(username: String, password: String) {
        Log.d("TokenManager", "Saving credentials for user: $username")
        prefs.edit()
            .putString(Constants.KEY_USERNAME, username)
            .putString(Constants.KEY_PASSWORD, password)
            .apply()
    }

    fun getUsername(): String? {
        return prefs.getString(Constants.KEY_USERNAME, null)
    }

    fun getPassword(): String? {
        return prefs.getString(Constants.KEY_PASSWORD, null)
    }

    fun clearCredentials() {
        Log.d("TokenManager", "Clearing credentials")
        prefs.edit()
            .remove(Constants.KEY_USERNAME)
            .remove(Constants.KEY_PASSWORD)
            .remove(Constants.KEY_TOKEN) // 🔥 Clear the token too!
            .apply()
    }


}
