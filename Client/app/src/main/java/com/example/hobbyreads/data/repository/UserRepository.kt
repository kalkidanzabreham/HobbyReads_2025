package com.example.hobbyreads.data.repository

import android.content.Context
import android.net.Uri
import com.example.hobbyreads.data.api.ApiService
import com.example.hobbyreads.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {

    /**
     * Get current user profile
     */
    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"
        val response = apiService.getCurrentUser(token)

        if (response.isSuccessful) {
            return@withContext response.body()
        } else {
            throw Exception("Failed to fetch user profile: ${response.message()}")
        }
    }

    /**
     * Update user profile
     */

    suspend fun updateProfile(
        name: String,
        bio: String,
        hobbies: List<String>,
        profilePictureUri: Uri? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"

        try {
            // Create request parts
            val nameRequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val bioRequestBody = bio.toRequestBody("text/plain".toMediaTypeOrNull())

            // Convert hobbies list to JSON
            val hobbiesJson = JSONArray(hobbies).toString()

            val hobbiesRequestBody = hobbiesJson.toRequestBody("application/json".toMediaTypeOrNull())

            // Handle profile picture if provided
            val profilePicturePart = profilePictureUri?.let { uri ->
                // Copy the file from content URI to a temporary file
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = "profile_picture_temp"
                val fileExtension = context.contentResolver.getType(uri)?.split("/")?.last() ?: "jpg"
                val tempFile = File(context.cacheDir, "$fileName.$fileExtension")

                FileOutputStream(tempFile).use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }

                // Create MultipartBody.Part from the temporary file
                val requestBody = tempFile.readBytes().toRequestBody(
                    "image/*".toMediaTypeOrNull()
                )

                MultipartBody.Part.createFormData(
                    "profilePicture",
                    tempFile.name,
                    requestBody
                )
            }

            val response = apiService.updateProfile(
                token = token,
                name = nameRequestBody,
                bio = bioRequestBody,
                hobbies = hobbiesRequestBody,
                profilePicture = profilePicturePart
            )

            return@withContext response.isSuccessful
        } catch (e: Exception) {
            throw Exception("Failed to update profile: ${e.message}")
        }
    }

    /**
     * Change password
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Boolean = withContext(Dispatchers.IO) {
        val token = "Bearer ${tokenManager.getToken()}"

        try {
            val passwordData = mapOf(
                "currentPassword" to currentPassword,
                "newPassword" to newPassword
            )

            val response = apiService.changePassword(token, passwordData)
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            throw Exception("Failed to change password: ${e.message}")
        }
    }

   }
