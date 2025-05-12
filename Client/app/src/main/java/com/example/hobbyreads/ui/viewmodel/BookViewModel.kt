package com.example.hobbyreads.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyreads.data.model.Book
import com.example.hobbyreads.data.model.Review
import com.example.hobbyreads.data.repository.BookRepository
import com.example.hobbyreads.data.repository.TokenManager
import com.example.hobbyreads.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Book list state
    private val _books = MutableStateFlow<Resource<List<Book>>>(Resource.Loading)
    val books: StateFlow<Resource<List<Book>>> = _books

    // Selected book state
    private val _book = MutableStateFlow<Resource<Book?>>(Resource.Loading)
    val book: StateFlow<Resource<Book?>> = _book

    private val _myBooks = MutableStateFlow<Resource<List<Book>>>(Resource.Loading)
    val myBooks: StateFlow<Resource<List<Book>>> = _myBooks

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Add book status
    private val _addBookStatus = MutableStateFlow<Resource<Book>>(Resource.Loading)
    val addBookStatus: StateFlow<Resource<Book>> = _addBookStatus


    // Delete book status
    private val _deleteBookStatus = MutableStateFlow<Resource<Boolean>>(Resource.Success(false))
    val deleteBookStatus: StateFlow<Resource<Boolean>> = _deleteBookStatus

    // Add review status
    private val _addReviewStatus = MutableStateFlow<Resource<Boolean>>(Resource.Success(false))
    val addReviewStatus: StateFlow<Resource<Boolean>> = _addReviewStatus

    // Private mutable state
    private val _getReviewsStatus = MutableStateFlow<Resource<List<Review>>>(Resource.Loading)

    // Public immutable state
    val getReviewsStatus: StateFlow<Resource<List<Review>>> = _getReviewsStatus

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage



    // Book added flag
    var bookAdded by mutableStateOf(false)
        private set

    init {
        fetchBooks()
    }

    fun fetchBooks() {
        viewModelScope.launch {
            _books.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = bookRepository.getBooks()
                _books.value = result  // ✅ No extra wrapping
            } catch (e: Exception) {
                _books.value = Resource.Error(e.message ?: "Failed to fetch books")
                _error.value = e.message ?: "Failed to fetch books"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchBookById(bookId: Int) {
        viewModelScope.launch {
            _book.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = bookRepository.getBookById(bookId.toString())
                _book.value = result  // ✅ Directly assign the result (already Resource)
            } catch (e: Exception) {
                _book.value = Resource.Error(e.message ?: "Failed to fetch book details")
                _error.value = e.message ?: "Failed to fetch book details"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun fetchMyBooks() {
        viewModelScope.launch {
            _myBooks.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = bookRepository.getMyBooks()
                Log.d("Books", "Fetched books: ${result}")
                _myBooks.value = result  // Already Resource.Success or Error
            } catch (e: Exception) {
                _myBooks.value = Resource.Error(e.message ?: "Failed to fetch my books")
                _error.value = e.message ?: "Failed to fetch my books"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun addBook(
        title: String,
        author: String,
        description: String,
        genre: String,
        condition: String,
        status: String,
        coverImageUri: Uri? = null
    ) {
        viewModelScope.launch {
            _addBookStatus.value = Resource.Loading
            _isLoading.value = true
            _error.value = null
            bookAdded = false

            try {
                val result = bookRepository.addBook(
                    title = title,
                    author = author,
                    description = description,
                    genre = genre,
                    bookCondition = condition,
                    status = status,
                    coverImageUri = coverImageUri
                )

                _addBookStatus.value = result
                if (result is Resource.Success) {
                    bookAdded = true
                    fetchBooks()
                }

            } catch (e: Exception) {
                _addBookStatus.value = Resource.Error(e.message ?: "Failed to add book")
                _error.value = e.message ?: "Failed to add book"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBook(bookId: Int) {
        viewModelScope.launch {
            _deleteBookStatus.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                // Get the token from your token manager or other secure storage
                val token = tokenManager.getToken() ?: throw Exception("Token is missing")

                // Call the deleteBook method, passing the bookId and token
                val result = bookRepository.deleteBook(bookId.toString(), token)

                when (result) {
                    is Resource.Success -> {
                        if (result.data) { // If true, book is deleted
                            _deleteBookStatus.value = Resource.Success(true)
                            fetchBooks() // Refresh book list
                        } else {
                            _deleteBookStatus.value = Resource.Error("Failed to delete book")
                        }
                    }
                    is Resource.Error -> {
                        _deleteBookStatus.value = result  // Handle error result
                    }
                    is Resource.Loading -> {
                        // Handle loading state (if needed)
                    }
                }
            } catch (e: Exception) {
                _deleteBookStatus.value = Resource.Error(e.message ?: "Failed to delete book")
                _error.value = e.message ?: "Failed to delete book"
            } finally {
                _isLoading.value = false // Hide loading indicator
            }



        }
    }

    fun addReview(bookId: Int, rating: Int, comment: String) {
        viewModelScope.launch {
            _addReviewStatus.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = bookRepository.addReview(
                    bookId = bookId.toString(),
                    rating = rating,
                    comment = comment
                )

                // Use Boolean result to update _addReviewStatus
                _addReviewStatus.value = if (result) Resource.Success(true) else Resource.Error("Failed to add review")

                if (result) {
                    fetchBookById(bookId) // Refresh book details
                }
            } catch (e: Exception) {
                _addReviewStatus.value = Resource.Error(e.message ?: "Failed to add review")
                _error.value = e.message ?: "Failed to add review"
            } finally {
                _isLoading.value = false
            }

        }
    }

    fun getReviews(bookId: Int) {
        viewModelScope.launch {
            _getReviewsStatus.value = Resource.Loading
            _isLoading.value = true
            _error.value = null

            try {
                val result = bookRepository.getReview(bookId.toString())

                _getReviewsStatus.value = result // Assign the Resource result
            } catch (e: Exception) {
                _getReviewsStatus.value = Resource.Error(e.message ?: "Failed to fetch reviews")
                _error.value = e.message ?: "Failed to fetch reviews"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetBookAdded() {
        bookAdded = false
    }

//    fun resetAddBookStatus() {
//        _addBookStatus.value = Resource.Success(false)
//    }

    fun resetDeleteBookStatus() {
        _deleteBookStatus.value = Resource.Success(false)
    }

    fun resetAddReviewStatus() {
        _addReviewStatus.value = Resource.Success(false)
    }

    fun clearError() {
        _error.value = null
    }
    fun updateTradeStatus(bookId: Int, status: String) {
        viewModelScope.launch {
            try {
                bookRepository.updateBookTradeStatus(bookId, status)
                _toastMessage.value = "Trade status updated"
                // Optional: fetch books again to refresh list
                fetchBooks()
            } catch (e: Exception) {
                _toastMessage.value = "Failed to update trade status: ${e.message}"
            }
        }
    }

}
