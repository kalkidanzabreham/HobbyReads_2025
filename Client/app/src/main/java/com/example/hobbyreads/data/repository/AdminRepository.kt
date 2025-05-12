package com.example.hobbyreads.data.repository

import com.example.hobbyreads.data.api.ApiService
import com.example.hobbyreads.data.model.Hobby
import com.example.hobbyreads.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    /**
     * Get all hobbies
     */
    suspend fun getAllHobbies(): List<Hobby> = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"

        try {
            val response = apiService.getAllHobbies(token)

            if (response.isSuccessful) {
                return@withContext response.body() ?: emptyList()
            } else {
                throw Exception("Failed to fetch hobbies: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch hobbies: ${e.message}")
        }
    }

    /**
     * Create a new hobby
     */
    suspend fun createHobby(name: String): Hobby = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"

        try {
            val hobbyData = mapOf("name" to name)
            val response = apiService.createHobby(token, hobbyData)

            if (response.isSuccessful) {
                val responseBody = response.body()
                val hobbyData = responseBody?.get("hobby") as? Map<*, *>
                    ?: throw Exception("Invalid response format")

                return@withContext Hobby(
                    id = (hobbyData["id"] as Number).toInt(),
                    name = hobbyData["name"] as String
                )
            } else {
                throw Exception("Failed to create hobby: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to create hobby: ${e.message}")
        }
    }

    /**
     * Update a hobby
     */
    suspend fun updateHobby(id: Int, name: String): Boolean = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"

        try {
            val hobbyData = mapOf("name" to name)
            val response = apiService.updateHobby(token, id, hobbyData)

            return@withContext response.isSuccessful
        } catch (e: Exception) {
            throw Exception("Failed to update hobby: ${e.message}")
        }
    }

    /**
     * Delete a hobby
     */
    suspend fun deleteHobby(id: Int): Boolean = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"

        try {
            val response = apiService.deleteHobby(token, id)

            return@withContext response.isSuccessful
        } catch (e: Exception) {
            throw Exception("Failed to delete hobby: ${e.message}")
        }
    }

    /**
     * Get all users
     */
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"

        try {
            val response = apiService.getAllUsers(token)

            if (response.isSuccessful) {
                return@withContext response.body() ?: emptyList()
            } else {
                throw Exception("Failed to fetch users: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch users: ${e.message}")
        }
    }

    /**
     * Delete account
     */
    suspend fun deleteAccount(password: String): Boolean = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"

        try {
            val passwordData = mapOf("password" to password)
            val response = apiService.deleteAccount(token, passwordData)

            if (response.isSuccessful) {
                // Clear token on successful deletion
                tokenManager.clearToken()
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            throw Exception("Failed to delete account: ${e.message}")
        }
    }

    suspend fun deleteUserById(userId: String): Boolean = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"
        try {
            val response = apiService.deleteUserById(userId, token)
            if (response.isSuccessful) {
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            throw Exception("Failed to delete user: ${e.message}")
        }
    }

}
