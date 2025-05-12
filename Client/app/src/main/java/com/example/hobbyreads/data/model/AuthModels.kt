package com.example.hobbyreads.data.model

data class AuthRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val name: String? = null,
    val hobbies: List<String>? = null
)

data class AuthResponse(
    val message: String,
    val token: String,
    val user: User? = null  // Made nullable to handle potential null responses
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val name: String?,
    val bio: String?,
    val profilePicture: String? = null,
    val isAdmin: Boolean,
    val createdAt: String,
    val hobbies: List<Hobby>,
)
data class Hobby(
    val id: Int,
    val name: String,
)