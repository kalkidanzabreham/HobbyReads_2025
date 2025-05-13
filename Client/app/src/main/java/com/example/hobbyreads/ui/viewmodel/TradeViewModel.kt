package com.example.hobbyreads.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyreads.data.model.TradeRequest
import com.example.hobbyreads.data.model.TradeStatus
import com.example.hobbyreads.data.model.TradeType
import com.example.hobbyreads.data.repository.TradeRepository
import com.example.hobbyreads.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

@HiltViewModel
class TradeViewModel @Inject constructor(
    private val tradeRepository: TradeRepository
) : ViewModel() {

    // Trade requests state
    private val _tradeRequests = MutableStateFlow<Resource<List<TradeRequest>>>(Resource.Loading)
    val tradeRequests: StateFlow<Resource<List<TradeRequest>>> = _tradeRequests

    // Sent trade request bookIds
    private val _sentRequests = MutableStateFlow<Set<Int>>(emptySet())
    val sentRequests: StateFlow<Set<Int>> = _sentRequests

    // Create trade request state
    private val _createTradeRequestStatus = MutableStateFlow<Resource<TradeRequest?>>(Resource.Success(null))
    val createTradeRequestStatus: StateFlow<Resource<TradeRequest?>> = _createTradeRequestStatus

    // Update trade request state
    private val _updateTradeRequestStatus = MutableStateFlow<Resource<TradeRequest?>>(Resource.Success(null))
    val updateTradeRequestStatus: StateFlow<Resource<TradeRequest?>> = _updateTradeRequestStatus

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Toast message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    private val _incomingRequestCount = MutableStateFlow(0)
    val incomingRequestCount: StateFlow<Int> = _incomingRequestCount


    // Incoming pending trades
    val incomingTradeRequests: StateFlow<List<TradeRequest>> =
        tradeRequests.map { res ->
            (res as? Resource.Success)?.data
                ?.filter { it.type == TradeType.INCOMING && it.status == "pending" }
                ?: emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    //  Outgoing pending trades
    val outgoingTradeRequests: StateFlow<List<TradeRequest>> =
        tradeRequests.map { res ->
            (res as? Resource.Success)?.data
                ?.filter { it.type == TradeType.OUTGOING && it.status == "pending" }
                ?: emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Optional: All trades (pending + completed), useful for history screen
    val allTradeRequests: StateFlow<List<TradeRequest>> =
        tradeRequests.map { res ->
            (res as? Resource.Success)?.data ?: emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        fetchTradeRequests()
    }


    private val _acceptedRequests = MutableStateFlow<List<TradeRequest>>(emptyList())
    val acceptedRequests: StateFlow<List<TradeRequest>> = _acceptedRequests

    fun loadAcceptedRequests(userId: String) {
        viewModelScope.launch {
            try {
                val accepted = tradeRepository.fetchAcceptedRequests(userId)
                _acceptedRequests.value = accepted
            } catch (e: Exception) {
                Log.e("TradeVM", "Error fetching accepted requests", e)
            }
        }
    }

    fun fetchTradeRequests() {
        viewModelScope.launch {
            _tradeRequests.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = tradeRepository.getPendingTradeRequests()
                _tradeRequests.value = Resource.Success(result)
                Log.d("TradesScreen", "Fetched trade requests: $result")

                // Get sent (outgoing) book IDs
                val sentBookIds = result.filter { it.type == TradeType.OUTGOING }
                    .map { it.book.id }
                    .toSet()
                _sentRequests.value = sentBookIds

                //  Update outgoing requests count
                val incomingCount = result.count { it.type == TradeType.INCOMING}
                _incomingRequestCount.value = incomingCount

                _isLoading.value = false
            } catch (e: Exception) {
                _tradeRequests.value = Resource.Error(e.localizedMessage ?: "Unknown Error")
                _isLoading.value = false
                _error.value = e.localizedMessage
            }
        }
    }

    fun createTradeRequest(bookId: Int, message: String? = null) {
        viewModelScope.launch {
            _createTradeRequestStatus.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            // Optimistically add this bookId to sentRequests immediately
            val previousSentRequests = _sentRequests.value
            _sentRequests.value = previousSentRequests + bookId

            try {
                val result = tradeRepository.createTradeRequest(bookId, message)

                _createTradeRequestStatus.value = Resource.Success(result)
                _toastMessage.value = "Trade request sent successfully"

                launch {
                    delay(1500) // 1.5 seconds
                    fetchTradeRequests()
                }

            } catch (e: Exception) {
                _createTradeRequestStatus.value = Resource.Error(e.message ?: "Failed to create trade request")
                _error.value = e.message ?: "Failed to create trade request"
                _toastMessage.value = "Failed to send trade request: ${e.message}"

                //  Rollback optimistic update
                _sentRequests.value = previousSentRequests
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTradeRequestStatus(tradeId: Int, status: TradeStatus, bookId: Int? = null) {
        viewModelScope.launch {
            _updateTradeRequestStatus.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                // Send status.value (lowercase) instead of enum
                val result = tradeRepository.updateTradeRequestStatus(tradeId, status.value)
                _updateTradeRequestStatus.value = Resource.Success(result)

                val statusMessage = when (status) {
                    TradeStatus.ACCEPTED -> "Trade request accepted"
                    TradeStatus.REJECTED -> "Trade request rejected"
                    TradeStatus.CANCELLED -> "Trade request cancelled"
                    else -> "Trade request updated"
                }

                _toastMessage.value = statusMessage

                // Optimistically update tradeRequests list locally
                val currentList = (_tradeRequests.value as? Resource.Success)?.data ?: emptyList()
                val updatedList = currentList.map {
                    if (it.id == tradeId) it.copy(status = status.value) else it
                }
                _tradeRequests.value = Resource.Success(updatedList)

                // Always fetch latest trade requests from API to sync pending/accepted/rejected
                fetchTradeRequests()


            } catch (e: Exception) {
                _updateTradeRequestStatus.value = Resource.Error(e.message ?: "Failed to update trade request")
                _error.value = e.message ?: "Failed to update trade request"
                _toastMessage.value = "Failed to update trade request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun resetCreateTradeRequestStatus() {
        _createTradeRequestStatus.value = Resource.Success(null)
    }

    fun resetUpdateTradeRequestStatus() {
        _updateTradeRequestStatus.value = Resource.Success(null)
    }
}
