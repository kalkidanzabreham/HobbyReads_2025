package com.example.hobbyreads.ui.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.hobbyreads.data.model.User
import com.example.hobbyreads.data.repository.AuthRepository
import com.example.hobbyreads.data.repository.TokenManager
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Unit>>(Resource.Loading)
    val loginState: StateFlow<Resource<Unit>> = _loginState

    private val _registerState = MutableStateFlow<Resource<Unit>>(Resource.Loading)
    val registerState: StateFlow<Resource<Unit>> = _registerState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Unknown)
    val sessionState: StateFlow<SessionState> = _sessionState

    init {
        // Check if user is already logged in
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            _isLoading.value = true
            _sessionState.value = SessionState.Checking

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                Log.d("AuthViewModel", "No token found, user is not logged in")
                _sessionState.value = SessionState.LoggedOut
                _isLoading.value = false
                return@launch
            }

            getUserProfile()
        }
    }

    fun login(username: String, password: String,navController: NavHostController) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = Resource.Loading

            Log.d("AuthViewModel", "Attempting login for user: $username")
            val result = authRepository.login(username, password)

            if (result is Resource.Success) {
                result.data.user?.let { user ->
                    Log.d("AuthViewModel", "Login successful for user: ${user.username}")
                    _currentUser.value = user
                    _loginState.value = Resource.Success(Unit)
                    _sessionState.value = SessionState.LoggedIn

                    // Save credentials for potential refresh
                    tokenManager.saveUserCredentials(username, password)
                    Log.d("isadmin", user.isAdmin.toString())
                    if (user.isAdmin) {
                        navController.navigate(Screen.AdminDashboard.route)

                    }
                } ?: run {
                    Log.e("AuthViewModel", "Login response contained null user")
                    _loginState.value = Resource.Error("Invalid user data received")
                    _sessionState.value = SessionState.LoggedOut
                }
            } else if (result is Resource.Error) {
                Log.e("AuthViewModel", "Login failed: ${result.message}")
                _loginState.value = Resource.Error(result.message)
                _sessionState.value = SessionState.LoggedOut
            }

            _isLoading.value = false
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        name: String? = null,
        hobbies: List<String>? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _registerState.value = Resource.Loading

            Log.d("AuthViewModel", "Attempting registration for user: $username")
            val result = authRepository.register(username, email, password, name, hobbies)

            if (result is Resource.Success) {
                result.data.user?.let { user ->
                    Log.d("AuthViewModel", "Registration successful for user: ${user.username}")
                    _currentUser.value = user
                    _registerState.value = Resource.Success(Unit)
                    _sessionState.value = SessionState.LoggedIn

                    // Save credentials for potential refresh
                    tokenManager.saveUserCredentials(username, password)
                } ?: run {
                    Log.e("AuthViewModel", "Registration response contained null user")
                    _registerState.value = Resource.Error("Invalid user data received")
                    _sessionState.value = SessionState.LoggedOut
                }
            } else if (result is Resource.Error) {
                Log.e("AuthViewModel", "Registration failed: ${result.message}")
                _registerState.value = Resource.Error(result.message)
                _sessionState.value = SessionState.LoggedOut
            }

            _isLoading.value = false
        }
    }

    fun getUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true

            Log.d("AuthViewModel", "Getting user profile")

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                Log.e("AuthViewModel", "No token found, cannot fetch user profile")
                _sessionState.value = SessionState.LoggedOut
                _isLoading.value = false
                return@launch
            }

            val result = authRepository.getUserProfile(token)

            if (result is Resource.Success) {
                result.data?.let { user ->
                    Log.d("AuthViewModel", "Got user profile: ${user.username}")
                    _currentUser.value = user
                    _sessionState.value = SessionState.LoggedIn
                } ?: run {
                    Log.e("AuthViewModel", "User profile response contained null user")
                    _sessionState.value = SessionState.LoggedOut
                }
            } else if (result is Resource.Error) {
                Log.e("AuthViewModel", "Failed to get user profile: ${result.message}")
                _sessionState.value = SessionState.LoggedOut
            }

            _isLoading.value = false
        }
    }



    fun logout() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Logging out")
            authRepository.logout()
            tokenManager.clearCredentials()
            _currentUser.value = null
            _sessionState.value = SessionState.LoggedOut
        }
    }

    fun resetLoginState() {
        _loginState.value = Resource.Loading
    }

    fun resetRegisterState() {
        _registerState.value = Resource.Loading
    }
}

sealed class SessionState {
    object Unknown : SessionState()
    object Checking : SessionState()
    object LoggedIn : SessionState()
    object LoggedOut : SessionState()
}
