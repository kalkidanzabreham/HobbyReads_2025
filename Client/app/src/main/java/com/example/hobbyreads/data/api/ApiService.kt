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

    // User endpoints
    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<User>

    @Multipart
    @PUT("users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("bio") bio: RequestBody,
        @Part("hobbies") hobbies: RequestBody,
        @Part profilePicture: MultipartBody.Part?
    ): Response<User>

    @PUT("users/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body passwordData: Map<String, String>
    ): Response<Map<String, String>>

    @HTTP(method = "DELETE", path = "users/delete-account", hasBody = true)
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Body passwordData: Map<String, String>
    ): Response<Map<String, String>>

    // Book endpoints
    @GET("books")
    suspend fun getBooks(@Header("Authorization") token: String): Response<List<Book>>

    @GET("books/{id}")
    suspend fun getBookById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Book>

    @Multipart
    @POST("books")
    suspend fun addBook(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("author") author: RequestBody,
        @Part("description") description: RequestBody,
        @Part("status") status: RequestBody,              // ✅ added
        @Part("bookCondition") bookCondition: RequestBody, // ✅ added
        @Part("genre") genre: RequestBody?,
        @Part("isbn") isbn: RequestBody?,
        @Part("publishYear") publishYear: RequestBody?,
        @Part coverImage: MultipartBody.Part?
    ): Response<Book>


    @DELETE("books/{id}")
    suspend fun deleteBook(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Void>

    @GET("books/my")
    suspend fun getMyBooks(
        @Header("Authorization") token: String
    ): Response<List<Book>>




    // Review endpoints
    @GET("books/{bookId}/reviews")
    suspend fun getReviewsForBook(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String
    ): Response<List<Review>>

    @POST("reviews")
    suspend fun addReview(
        @Header("Authorization") token: String,
        @Body reviewData: Map<String, String>
    ): Response<Review>

    @GET("reviews/books/{bookId}")
    suspend fun getReviews(
        @Path("bookId") bookId: String // Pass the bookId as a path parameter
    ): Response<List<Review>>

    @PUT("books/{bookId}/status")
    suspend fun updateBookStatus(
        @Path("bookId") bookId: Int,
        @Body statusUpdate: String
    )

    @PUT("reviews/{id}")
    suspend fun updateReview(@Path("id") reviewId: Int, @Body review: Review): Response<Review>

    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") reviewId: Int): Response<Void>


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



    // Connection endpoints
    @GET("connections")
    suspend fun getConnections(@Header("Authorization") token: String): Response<List<Connection>>

    @GET("connections/pending")
    suspend fun getPendingConnections(@Header("Authorization") token: String): Response<List<Connection>>

    @GET("connections/suggested")
    suspend fun getSuggestedConnections(@Header("Authorization") token: String): Response<List<Connection>>

    @POST("connections/{userId}")
    suspend fun sendConnectionRequest(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<Connection>

    @PUT("connections/{connectionId}/accept")
    suspend fun acceptConnection(
        @Header("Authorization") token: String,
        @Path("connectionId") connectionId: Int
    ): Response<Connection>

    @PUT("connections/{connectionId}/reject")
    suspend fun rejectConnection(
        @Header("Authorization") token: String,
        @Path("connectionId") connectionId: Int
    ): Response<Connection>

    @DELETE("connections/{connectionId}")
    suspend fun removeConnection(
        @Header("Authorization") token: String,
        @Path("connectionId") connectionId: Int
    ): Response<Unit>


}