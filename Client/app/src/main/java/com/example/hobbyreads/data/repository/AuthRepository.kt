package com.example.hobbyreads.data.repository


import android.util.Log
import com.example.hobby.util.Resource
import com.example.hobbyreads.data.model.AuthResponse
import com.example.hobbyreads.data.api.ApiService
import com.example.hobbyreads.data.model.AuthRequest
import com.example.hobbyreads.data.model.RegisterRequest
import com.example.hobbyreads.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    val apiService: ApiService,
    val tokenManager: TokenManager
) {
    suspend fun login(username: String, password: String): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    apiService.login(AuthRequest(username = username, password = password))
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Save token
                        authResponse.token?.let { token ->
                            tokenManager.saveToken(token)
                        }
                        Resource.Success(authResponse)
                    } ?: Resource.Error("Empty response body")
                } else {
                    Resource.Error(response.message() ?: "Login failed")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        name: String? = null,
        hobbies: List<String>? = null
    ): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    name = name,
                    hobbies = hobbies
                )
                val response = apiService.register(request)
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Save token
                        authResponse.token?.let { token ->
                            tokenManager.saveToken(token)
                        }
                        Resource.Success(authResponse)
                    } ?: Resource.Error("Empty response body")
                } else {
                    Resource.Error(response.message() ?: "Registration failed")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getUserProfile(token: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserProfile("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        return@withContext Resource.Success(user)
                    } ?: run {
                        return@withContext Resource.Error("Empty response body")
                    }
                } else {
                    return@withContext Resource.Error(response.message() ?: "Failed to get user profile")
                }
            } catch (e: Exception) {
                return@withContext Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getCurrentUser(): User? {
        val token = tokenManager.getToken() ?: return null
        return when (val result = getUserProfile(token)) {
            is Resource.Success -> result.data
            else -> null
        }
    }

    suspend fun logout() {
        // Optionally: Call logout API if your backend has it
        // apiService.logout()

        // Or just clear local user session (most apps do this)
        tokenManager.clearCredentials()
    }
}
