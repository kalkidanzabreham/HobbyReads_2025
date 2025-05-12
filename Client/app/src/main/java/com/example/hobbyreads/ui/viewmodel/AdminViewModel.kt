package com.example.hobbyreads.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyreads.data.model.Hobby
import com.example.hobbyreads.data.model.User
import com.example.hobbyreads.data.repository.AdminRepository
import com.example.hobbyreads.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    // Hobbies state
    private val _hobbies = MutableStateFlow<Resource<List<Hobby>>>(Resource.Loading)
    val hobbies: StateFlow<Resource<List<Hobby>>> = _hobbies

    // Users state
    private val _users = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val users: StateFlow<Resource<List<User>>> = _users

    // Create hobby state
    private val _createHobbyStatus = MutableStateFlow<Resource<Hobby?>>(Resource.Success(null))
    val createHobbyStatus: StateFlow<Resource<Hobby?>> = _createHobbyStatus

    // Update hobby state
    private val _updateHobbyStatus = MutableStateFlow<Resource<Boolean>>(Resource.Success(false))
    val updateHobbyStatus: StateFlow<Resource<Boolean>> = _updateHobbyStatus

    // Delete hobby state
    private val _deleteHobbyStatus = MutableStateFlow<Resource<Boolean>>(Resource.Success(false))
    val deleteHobbyStatus: StateFlow<Resource<Boolean>> = _deleteHobbyStatus

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val totalUsers: StateFlow<Int> = _users.map { resource ->
        when (resource) {
            is Resource.Success -> resource.data.size
            else -> 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalHobbies: StateFlow<Int> = _hobbies.map { resource ->
        when (resource) {
            is Resource.Success -> resource.data.size
            else -> 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Toast message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    private val _deleteUserStatus = MutableStateFlow<Resource<Any>>(Resource.Success(Unit))
    val deleteUserStatus: StateFlow<Resource<Any>> = _deleteUserStatus


    init {
        fetchHobbies()
        fetchUsers()
    }

    fun fetchHobbies() {
        viewModelScope.launch {
            _hobbies.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = adminRepository.getAllHobbies()
                _hobbies.value = Resource.Success(result)
            } catch (e: Exception) {
                _hobbies.value = Resource.Error(e.message ?: "Failed to fetch hobbies")
                _error.value = e.message ?: "Failed to fetch hobbies"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            _users.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = adminRepository.getAllUsers()
                _users.value = Resource.Success(result)
            } catch (e: Exception) {
                _users.value = Resource.Error(e.message ?: "Failed to fetch users")
                _error.value = e.message ?: "Failed to fetch users"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createHobby(name: String) {
        viewModelScope.launch {
            _createHobbyStatus.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = adminRepository.createHobby(name)
                _createHobbyStatus.value = Resource.Success(result)
                _toastMessage.value = "Hobby created successfully"

                // Refresh hobbies
                fetchHobbies()
            } catch (e: Exception) {
                _createHobbyStatus.value = Resource.Error(e.message ?: "Failed to create hobby")
                _error.value = e.message ?: "Failed to create hobby"
                _toastMessage.value = "Failed to create hobby: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateHobby(id: Int, name: String) {
        viewModelScope.launch {
            _updateHobbyStatus.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = adminRepository.updateHobby(id, name)
                _updateHobbyStatus.value = Resource.Success(result)

                if (result) {
                    _toastMessage.value = "Hobby updated successfully"
                    fetchHobbies()
                } else {
                    _toastMessage.value = "Failed to update hobby"
                }
            } catch (e: Exception) {
                _updateHobbyStatus.value = Resource.Error(e.message ?: "Failed to update hobby")
                _error.value = e.message ?: "Failed to update hobby"
                _toastMessage.value = "Failed to update hobby: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteHobby(id: Int) {
        viewModelScope.launch {
            _deleteHobbyStatus.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = adminRepository.deleteHobby(id)
                _deleteHobbyStatus.value = Resource.Success(result)

                if (result) {
                    _toastMessage.value = "Hobby deleted successfully"
                    fetchHobbies()

                } else {
                    _toastMessage.value = "Failed to delete hobby"
                }
            } catch (e: Exception) {
                _deleteHobbyStatus.value = Resource.Error(e.message ?: "Failed to delete hobby")
                _error.value = e.message ?: "Failed to delete hobby"
                _toastMessage.value = "Failed to delete hobby: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 1. Delete own account (password required)
    fun deleteOwnAccount(password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = adminRepository.deleteAccount(password)
                onResult(success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }




    fun deleteUserById(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _deleteUserStatus.value = Resource.Loading  // Start loading

            try {
                val result = adminRepository.deleteUserById(userId)

                if (result is Resource.Success<*> && result.data == true) {
                    // ✅ Deletion actually succeeded
                    _toastMessage.value = "User deleted successfully"
                    _deleteUserStatus.value = Resource.Success(true)

                    // Remove user locally
                    val currentList = (_users.value as? Resource.Success)?.data?.toMutableList() ?: mutableListOf()
                    val updatedList = currentList.filterNot { it.id.toString() == userId }
                    _users.value = Resource.Success(updatedList)

                } else {
                    // ❌ Either Resource.Error or Success(false)
                    _deleteUserStatus.value = Resource.Error("Failed to delete user")
                }

            } catch (e: Exception) {
                _deleteUserStatus.value = Resource.Error(e.message ?: "Failed to delete user")
//                _error.value = e.message ?: "Failed to delete user"
                _toastMessage.value = "Failed to delete user: ${e.message}"
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

    fun resetCreateHobbyStatus() {
        _createHobbyStatus.value = Resource.Success(null)
    }

    fun resetUpdateHobbyStatus() {
        _updateHobbyStatus.value = Resource.Success(false)
    }

    fun resetDeleteHobbyStatus() {
        _deleteHobbyStatus.value = Resource.Success(false)
    }
}
