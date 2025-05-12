package com.example.hobbyreads.data.repository

import android.content.Context
import android.net.Uri
import com.example.hobbyreads.data.api.ApiService
import com.example.hobbyreads.data.model.Book
import com.example.hobbyreads.data.model.Review
import com.example.hobbyreads.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {

    suspend fun getBooks(): Resource<List<Book>> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() ?: return@withContext Resource.Error("Not authenticated")
            val response = apiService.getBooks("Bearer $token")

            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to fetch books")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}")
        }
    }

    suspend fun getBookById(id: String): Resource<Book?> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() ?: return@withContext Resource.Error("Not authenticated")
            val response = apiService.getBookById("Bearer $token", id)

            if (response.isSuccessful) {
                Resource.Success(response.body())
            } else {
                Resource.Error(response.message() ?: "Failed to fetch book")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}")
        }
    }

    suspend fun addBook(
        title: String,
        author: String,
        description: String,
        genre: String? = null,
        bookCondition: String? = null,
        status: String? = null,
        coverImageUri: Uri? = null
    ): Resource<Book> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() ?: return@withContext Resource.Error("Not authenticated")

            // Create multipart request parts
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val authorPart = author.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val genrePart = (genre ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
            val conditionPart = (bookCondition ?: "Good").toRequestBody("text/plain".toMediaTypeOrNull())
            val statusPart = (status ?: "Not for Trade").toRequestBody("text/plain".toMediaTypeOrNull())

            // Handle cover image if provided
            val imagePart = coverImageUri?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "cover_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("coverImage", file.name, requestFile)
            }

            // ✅ Correct order matching Retrofit interface!
            val response = apiService.addBook(
                "Bearer $token",
                titlePart,
                authorPart,
                descriptionPart,
                statusPart,       // ✅ status first
                conditionPart,    // ✅ bookCondition second
                genrePart,        // ✅ genre third
                null,             // isbn
                null,             // publishYear
                imagePart         // ✅ coverImage
            )

            return@withContext if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Failed to add book: Empty response")
            } else {
                Resource.Error(response.message() ?: "Failed to add book")
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("An error occurred: ${e.message}")
        }
    }


    suspend fun addReview(
        bookId: String,
        rating: Int,
        comment: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() ?: return@withContext false

            // Create request body
            val reviewData = mapOf(
                "bookId" to bookId,
                "rating" to rating.toString(),
                "comment" to comment
            )

            // Make API call
            val response = apiService.addReview("Bearer $token", reviewData)

            if (response.isSuccessful) {
                true // Return true if the review was successfully added
            } else {
                false // Return false if the response is unsuccessful
            }
        } catch (e: Exception) {
            false // Return false if an error occurred
        }
    }

    suspend fun getReview(bookId: String): Resource<List<Review>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getReviews(bookId)

            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it) // ✅ Use Resource.Success, not Resource.success
                } ?: Resource.Error("Failed to fetch reviews: Empty response") // ✅ Use Resource.Error
            } else {
                Resource.Error(response.message() ?: "Failed to fetch reviews") // ✅ Use Resource.Error
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}") // ✅ Use Resource.Error
        }
    }

    // Modify the deleteBook method to accept token as a parameter
    suspend fun deleteBook(bookId: String, token: String): Resource<Boolean> {
        return try {
            // API call to delete the book, passing the token in the headers
            val response = apiService.deleteBook(bookId, token)

            if (response.isSuccessful) {
                // Return Success(true) if deletion is successful
                Resource.Success(true)
            } else {
                // Return Error if deletion fails
                Resource.Error("Failed to delete book")
            }
        } catch (e: Exception) {
            // Handle exception and return error
            Resource.Error(e.message ?: "Failed to delete book")
        }
    }
    suspend fun updateBookTradeStatus(bookId: Int, status: String) {
        apiService.updateBookStatus(bookId, status)
    }


    suspend fun getMyBooks(): Resource<List<Book>> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() ?: return@withContext Resource.Error("Not authenticated")

            val response = apiService.getMyBooks("Bearer $token")

            if (response.isSuccessful) {
                val books = response.body() ?: emptyList()
                Resource.Success(books)
            } else {
                Resource.Error(response.message() ?: "Failed to fetch my books")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}")
        }
    }

}

