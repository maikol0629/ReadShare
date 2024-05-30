package com.grupo10.readshare.storage

import android.app.Activity
import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: String = ""
)

data class Conversation(
    val id: String = "",
    val userIds: List<String?> = emptyList(),
    val lastMessage: String = "",
    val lastTimestamp: Timestamp = Timestamp.now(),
    val bookId: String = ""
)

class ChatManager(ctx: Context) {
    private val auth = AuthManager(ctx, ctx as Activity)
    private val db = FirebaseFirestore.getInstance()

    // Obtener lista de chats del usuario actual
    suspend fun getUserChats(userId: String): List<Conversation> {
        return db.collection("chats")
            .whereArrayContains("userIds", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Conversation::class.java)
    }

    // Obtener mensajes de un chat
    suspend fun getChatMessages(chatId: String): List<ChatMessage> {
        return db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get().await()
            .toObjects(ChatMessage::class.java)
    }

    // Enviar un mensaje
    suspend fun sendMessage(chatId: String, message: ChatMessage) {
        val messageRef = db.collection("chats").document(chatId).collection("messages").document()
        messageRef.set(message).await()

        // Actualizar el último mensaje y timestamp en el documento del chat
        db.collection("chats").document(chatId).update(
            "lastMessage", message.message,
            "timestamp", FieldValue.serverTimestamp()
        ).await()
    }

    // Crear un nuevo chat a partir de un bookId y bookUserId
    suspend fun createChatFromBook(bookId: String, bookUserId: String, initialMessage: ChatMessage): String {
        val currentUser = auth.getUserUid()
        val chatRef = db.collection("chats").document()
        val chat = Conversation(
            id = chatRef.id,
            userIds = listOf(currentUser, bookUserId),
            lastMessage = initialMessage.message,
            lastTimestamp = Timestamp.now(),
            bookId = bookId
        )
        chatRef.set(chat).await()

        sendMessage(chatRef.id, initialMessage)
        return chatRef.id
    }

    // Obtener un chat existente entre dos usuarios y un libro específico
    suspend fun getExistingChat(bookId: String, userId: String, bookUserId: String): Conversation? {
        val result = db.collection("chats")
            .whereEqualTo("bookId", bookId)
            .whereArrayContainsAny("userIds", listOf(userId, bookUserId))
            .get()
            .await()

        return result.documents.firstOrNull()?.toObject(Conversation::class.java)
    }
    suspend fun getConversationByChatId(chatId: String): Conversation? {
        val doc = db.collection("chats").document(chatId).get().await()
        return doc.toObject(Conversation::class.java)
    }
}
