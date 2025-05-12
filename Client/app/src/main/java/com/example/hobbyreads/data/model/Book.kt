package com.example.hobbyreads.data.model

import java.util.Date

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val coverImage: String? = null,
    val description: String? = null,
    val ownerId: Int,
    val ownerName: String,
    val ownerUsername: String,
    val status: String, // "Available for Trade" or "Not for Trade"
    val genre: String? = null,
    val bookCondition: String? = null,
    val averageRating: Float = 0f,
    val reviewCount: Int = 0,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val reviews: List<Review>? = null,
    val isbn: String? = null,
    val publishYear: Int? = null,
    val pageCount: Int? = null
)

data class StatusUpdateRequest(val status: String)