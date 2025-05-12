package com.example.hobbyreads.data.model

import java.util.Date

data class Review(
    val id: Int,
    val bookId: Int,
    val userId: Int,
    val username: String,
    val name: String,
    val rating: Int,
    val comment: String?,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

