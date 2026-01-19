package org.example.project.database

import org.example.project.db.AppDatabase
import org.example.project.models.Message as ModelMessage
import org.example.project.models.MessageRequest
import java.time.LocalDateTime

class MessageRepository(private val db: AppDatabase) {

    private val queries = db.messageQueries

    fun createMessage(request: MessageRequest): Long {
        val createdAt = LocalDateTime.now().toString()
        db.transaction {
            queries.insertMessage(
                petId = request.petId,
                shelterId = request.shelterId,
                userId = request.userId,
                userName = request.userName,
                userEmail = request.userEmail,
                userPhone = request.userPhone,
                messageText = request.messageText,
                createdAt = createdAt,
                isRead = 0
            )
        }
        return queries.lastInsertId().executeAsOne()

    }

    fun getMessagesByShelter(shelterId: Long): List<ModelMessage> {
        return queries.getMessagesByShelter(shelterId)
            .executeAsList()
            .map { row ->
                ModelMessage(
                    id = row.id,
                    petId = row.petId,
                    shelterId = row.shelterId,
                    userId = row.userId,
                    userName = row.userName,
                    userEmail = row.userEmail,
                    userPhone = row.userPhone,
                    messageText = row.messageText,
                    createdAt = row.createdAt,
                    isRead = row.isRead == 1L
                )
            }
    }

    fun getMessagesByUser(userId: Long): List<ModelMessage> {
        return queries.getMessagesByUser(userId)
            .executeAsList()
            .map { row ->
                ModelMessage(
                    id = row.id,
                    petId = row.petId,
                    shelterId = row.shelterId,
                    userId = row.userId,
                    userName = row.userName,
                    userEmail = row.userEmail,
                    userPhone = row.userPhone,
                    messageText = row.messageText,
                    createdAt = row.createdAt,
                    isRead = row.isRead == 1L
                )
            }
    }

    fun getUnreadCount(shelterId: Long): Long {
        return queries.getUnreadCount(shelterId).executeAsOne()
    }

    fun markAsRead(messageId: Long, isRead: Boolean) {
        queries.markAsRead(if (isRead) 1 else 0, messageId)
    }

    fun deleteMessage(messageId: Long) {
        queries.deleteMessage(messageId)
    }
}
