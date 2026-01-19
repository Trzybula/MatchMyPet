package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Long? = null,
    val petId: Long,
    val shelterId: Long,
    val userId: Long,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val messageText: String,
    val createdAt: String? = null,
    val isRead: Boolean = false
)

@Serializable
data class MessageRequest(
    val petId: Long,
    val shelterId: Long,
    val userId: Long,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val messageText: String
)

@Serializable
data class MarkAsReadRequest(
    val messageId: Long,
    val isRead: Boolean
)

@Serializable
data class SendMessageResponse(
    val success: Boolean,
    val messageId: Long
)
