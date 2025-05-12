package com.example.hobbyreads.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyreads.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // Profile update state
    private val _profileState = MutableStateFlow(ProfileUpdateState())
    val profileState: StateFlow<ProfileUpdateState> = _profileState

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun updateProfile(
        name: String,
        bio: String,
        hobbies: List<String>,
        profilePicture: Uri? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _profileState.value = ProfileUpdateState(isLoading = true)

            try {
                val success = userRepository.updateProfile(
                    name = name,
                    bio = bio,
                    hobbies = hobbies,
                    profilePictureUri = profilePicture
                )

                if (success) {
                    _profileState.value = ProfileUpdateState(isSuccess = true)
                } else {
                    _profileState.value = ProfileUpdateState(error = "Failed to update profile")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileUpdateState(error = e.message ?: "An error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetProfileState() {
        _profileState.value = ProfileUpdateState()
    }
}

data class ProfileUpdateState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
