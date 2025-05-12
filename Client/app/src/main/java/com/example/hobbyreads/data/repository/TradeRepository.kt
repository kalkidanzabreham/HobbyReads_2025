package com.example.hobbyreads.data.repository

import com.example.hobbyreads.data.api.ApiService
import com.example.hobbyreads.data.model.TradeRequest
import com.example.hobbyreads.data.model.TradeRequestCreate
import com.example.hobbyreads.data.model.TradeRequestStatusUpdate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TradeRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    /**
     * Get pending trade requests
     */
    suspend fun getPendingTradeRequests(): List<TradeRequest> = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"

        try {
            val response = apiService.getPendingTradeRequests(token)

            if (response.isSuccessful) {
                return@withContext response.body() ?: emptyList()
            } else {
                throw Exception("Failed to fetch trade requests: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch trade requests: ${e.message}")
        }
    }

    /**
     * Create a new trade request
     */
    suspend fun createTradeRequest(bookId: Int, message: String? = null): TradeRequest = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"

        try {
            val tradeRequestCreate = TradeRequestCreate(bookId, message)
            val response = apiService.createTradeRequest(token, tradeRequestCreate)

            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to create trade request: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to create trade request: ${e.message}")
        }
    }

    /**
     * Update trade request status
     */
    suspend fun updateTradeRequestStatus(tradeId: Int, status: String): TradeRequest = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"

        try {
            // ✅ Just pass status directly (it's already lowercase string like "accepted", "rejected")
            val statusUpdate = TradeRequestStatusUpdate(status)
            val response = apiService.updateTradeRequestStatus(token, tradeId, statusUpdate)

            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to update trade request: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to update trade request: ${e.message}")
        }
    }

    suspend fun fetchAcceptedRequests(userId: String): List<TradeRequest> {
        return apiService.getAcceptedRequests(userId)
    }
}
