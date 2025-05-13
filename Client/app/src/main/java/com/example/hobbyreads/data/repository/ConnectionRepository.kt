package com.example.hobbyreads.data.repository

import com.example.hobbyreads.data.api.ApiService
import com.example.hobbyreads.data.model.Connection
import com.example.hobbyreads.data.model.ConnectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    /**
     * Get all accepted connections for the current user
     */
    suspend fun getConnections(): List<Connection> = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"
        val response = apiService.getConnections(token)

        if (response.isSuccessful) {
            return@withContext response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch connections: ${response.message()}")
        }
    }

    /**
     * Get all pending connection requests for the current user
     */
    suspend fun getPendingConnections(): List<Connection> = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"
        val response = apiService.getPendingConnections(token)

        if (response.isSuccessful) {
            return@withContext response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch pending connections: ${response.message()}")
        }
    }

    /**
     * Get suggested connections for the current user
     */
    suspend fun getSuggestedConnections(): List<Connection> = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"
        val response = apiService.getSuggestedConnections(token)

        if (response.isSuccessful) {
            return@withContext response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch suggested connections: ${response.message()}")
        }
    }

    /**
     * Send a connection request to another user
     */
    suspend fun sendConnectionRequest(userId: Int): Boolean = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"
        val response = apiService.sendConnectionRequest(token, userId)

        if (response.isSuccessful) {
            return@withContext true
        } else {
            throw Exception("Failed to send connection request: ${response.message()}")
        }
    }

    /**
     * Accept a pending connection request
     */
    suspend fun acceptConnection(connectionId: Int): Boolean = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"
        val response = apiService.acceptConnection(token, connectionId)

        if (response.isSuccessful) {
            return@withContext true
        } else {
            throw Exception("Failed to accept connection: ${response.message()}")
        }
    }

    /**
     * Reject a pending connection request
     */
    suspend fun rejectConnection(connectionId: Int): Boolean = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"
        val response = apiService.rejectConnection(token, connectionId)

        if (response.isSuccessful) {
            return@withContext true
        } else {
            throw Exception("Failed to reject connection: ${response.message()}")
        }
    }

    /**
     * Remove an existing connection
     */
    suspend fun removeConnection(connectionId: Int): Boolean = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"
        val response = apiService.removeConnection(token, connectionId)

        if (response.isSuccessful) {
            return@withContext true
        } else {
            throw Exception("Failed to remove connection: ${response.message()}")
        }
    }
}
