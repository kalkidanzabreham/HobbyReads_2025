package com.example.hobbyreads.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyreads.data.model.Connection
import com.example.hobbyreads.data.repository.ConnectionRepository
import com.example.hobbyreads.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    // Connections state
    private val _connections = MutableStateFlow<Resource<List<Connection>>>(Resource.Loading)
    val connections: StateFlow<Resource<List<Connection>>> = _connections

    // Pending connections state
    private val _pendingConnections = MutableStateFlow<Resource<List<Connection>>>(Resource.Loading)
    val pendingConnections: StateFlow<Resource<List<Connection>>> = _pendingConnections

    private val _connectionsCount = MutableStateFlow(0)
    val connectionsCount: StateFlow<Int> get() = _connectionsCount


    // Suggested connections state
    private val _suggestedConnections = MutableStateFlow<Resource<List<Connection>>>(Resource.Loading)
    val suggestedConnections: StateFlow<Resource<List<Connection>>> = _suggestedConnections

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Toast message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    init {
        fetchConnections()
        fetchPendingConnections()
        fetchSuggestedConnections()
    }

    fun fetchConnections() {
        viewModelScope.launch {
            _connections.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = connectionRepository.getConnections()
                _connections.value = Resource.Success(result)
                _connectionsCount.value = result.size
            } catch (e: Exception) {
                _connections.value = Resource.Error(e.message ?: "Failed to fetch connections")
                _error.value = e.message ?: "Failed to fetch connections"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchPendingConnections() {
        viewModelScope.launch {
            _pendingConnections.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = connectionRepository.getPendingConnections()
                _pendingConnections.value = Resource.Success(result)
            } catch (e: Exception) {
                _pendingConnections.value = Resource.Error(e.message ?: "Failed to fetch pending connections")
                _error.value = e.message ?: "Failed to fetch pending connections"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchSuggestedConnections() {
        viewModelScope.launch {
            _suggestedConnections.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = connectionRepository.getSuggestedConnections()
                _suggestedConnections.value = Resource.Success(result)
            } catch (e: Exception) {
                _suggestedConnections.value = Resource.Error(e.message ?: "Failed to fetch suggested connections")
                _error.value = e.message ?: "Failed to fetch suggested connections"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendConnectionRequest(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = connectionRepository.sendConnectionRequest(userId)
                if (success) {
                    // Update suggested connections list
                    if (_suggestedConnections.value is Resource.Success) {
                        val currentList = (_suggestedConnections.value as Resource.Success<List<Connection>>).data
                        _suggestedConnections.value = Resource.Success(
                            currentList.filter { it.userId != userId }
                        )
                    }

                    _toastMessage.value = "Connection request sent"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send connection request"
                _toastMessage.value = "Failed to send connection request"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptConnection(connectionId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = connectionRepository.acceptConnection(connectionId)
                if (success) {
                    // Update both connections and pending connections lists
                    fetchConnections()
                    fetchPendingConnections()

                    // Find the connection to show in toast
                    if (_pendingConnections.value is Resource.Success) {
                        val pendingList = (_pendingConnections.value as Resource.Success<List<Connection>>).data
                        val connection = pendingList.find { it.id == connectionId }
                        if (connection != null) {
                            _toastMessage.value = "You are now connected with ${connection.name}"
                        } else {
                            _toastMessage.value = "Connection accepted"
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to accept connection"
                _toastMessage.value = "Failed to accept connection"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectConnection(connectionId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = connectionRepository.rejectConnection(connectionId)
                if (success) {
                    // Update pending connections list
                    if (_pendingConnections.value is Resource.Success) {
                        val currentList = (_pendingConnections.value as Resource.Success<List<Connection>>).data
                        val connection = currentList.find { it.id == connectionId }

                        _pendingConnections.value = Resource.Success(
                            currentList.filter { it.id != connectionId }
                        )

                        if (connection != null) {
                            _toastMessage.value = "You've rejected the connection request from ${connection.name}"
                        } else {
                            _toastMessage.value = "Connection request rejected"
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reject connection"
                _toastMessage.value = "Failed to reject connection"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeConnection(connectionId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = connectionRepository.removeConnection(connectionId)
                if (success) {
                    // Update connections list
                    if (_connections.value is Resource.Success) {
                        val currentList = (_connections.value as Resource.Success<List<Connection>>).data
                        _connections.value = Resource.Success(
                            currentList.filter { it.id != connectionId }
                        )
                    }

                    _toastMessage.value = "Connection has been removed"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to remove connection"
                _toastMessage.value = "Failed to remove connection"
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
}
