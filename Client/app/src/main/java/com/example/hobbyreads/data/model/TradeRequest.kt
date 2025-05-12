package com.example.hobbyreads.data.model

import java.util.Date

data class TradeRequest(
    val id: Int,
    val requesterId: Int,
    val bookId: Int,
    val ownerId: Int,
    val status: String,
    val message: String?,
    val createdAt: Date,
    val updatedAt: Date,
    val book: Book,
    val requester: UserSummary,
    val owner: UserSummary,
    val type: TradeType
)

enum class TradeStatus(val value: String) {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    CANCELLED("cancelled")
}


enum class TradeType {
    INCOMING,
    OUTGOING
}

data class UserSummary(
    val id: Int,
    val username: String,
    val name: String
)

data class TradeRequestCreate(
    val bookId: Int,
    val message: String?
)

data class TradeRequestStatusUpdate(
    val status: String
)
