package com.example.hobbyreads.data.model

data class Connection(
    val id: Int,
    val userId: Int,
    val connectedUserId: Int,
    val status: ConnectionStatus,
    val name: String,
    val username: String,
    val bio: String,
    val hobbies: List<String>,
    val matchPercentage: Int
)

enum class ConnectionStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
