package com.example.hobbyreads.data.api


import com.example.hobbyreads.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth endpoints
    @POST("auth/login")
    suspend fun login(@Body loginRequest: AuthRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>

    // User endpoints
    @GET("auth/profile")  // Correct endpoint URL
    suspend fun getUserProfile(
        @Header("Authorization") token: String  // Ensure that token is passed as an Authorization header
    ): Response<User>

     // User endpoints
    @GET("users")
    suspend fun getAllUsers(@Header("Authorization") token: String): Response<List<User>>

    @GET("admin/stats/users")
    suspend fun getUserStats(@Header("Authorization") token: String): Response<Map<String, Int>>

    @GET("admin/stats/hobbies")
    suspend fun getHobbyStats(@Header("Authorization") token: String): Response<Map<String, Int>>
    @DELETE("users/{id}")
    suspend fun deleteUserById(
        @Path("id") userId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    // Hobby endpoints
    @GET("hobbies")
    suspend fun getAllHobbies(@Header("Authorization") token: String): Response<List<Hobby>>

    @POST("hobbies")
    suspend fun createHobby(
        @Header("Authorization") token: String,
        @Body hobbyData: Map<String, String>
    ): Response<Map<String, Any>>

    @PUT("hobbies/{id}")
    suspend fun updateHobby(
        @Header("Authorization") token: String,
        @Path("id") hobbyId: Int,
        @Body hobbyData: Map<String, String>
    ): Response<Map<String, String>>

    @DELETE("hobbies/{id}")
    suspend fun deleteHobby(
        @Header("Authorization") token: String,
        @Path("id") hobbyId: Int
    ): Response<Map<String, String>>


}