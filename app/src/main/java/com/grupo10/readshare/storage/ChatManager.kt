package com.grupo10.readshare.storage

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date

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
    val lastTimestamp: String = "",
    val bookId: String = ""
)

class ChatManager() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("chats")
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUserChats(userId: String): List<Conversation> {
        val chats = mutableListOf<Conversation>()
        val snapshot = database.get().await()
        Log.i("ChatManager", "Snapshot: $snapshot")
        for (childSnapshot in snapshot.children) {
            val chat = childSnapshot.getValue(Conversation::class.java)
            chat?.let {
                if (it.userIds.contains(userId)) {
                    chats.add(it)
                }
            }
        }
        Log.i("ChatManager", "Chats: $chats")
        return chats
    }



    suspend fun getChatMessages(chatId: String): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        val snapshot = database.child(chatId).child("messages").orderByChild("timestamp").get().await()

        for (childSnapshot in snapshot.children) {
            val message = childSnapshot.getValue(ChatMessage::class.java)
            message?.let {
                messages.add(it)
            }
        }
        return messages
    }

    suspend fun sendMessage(chatId: String, message: ChatMessage) {
        val messageRef = database.child(chatId).child("messages").push()
        messageRef.setValue(message).await()

        // Actualizar el último mensaje y timestamp en el documento del chat
        val timestamp = SimpleDateFormat.getInstance().format(Date())
        database.child(chatId).updateChildren(
            mapOf(
                "lastMessage" to message.message,
                "lastTimestamp" to timestamp
            )
        ).await()
    }

    suspend fun createChatFromBook(bookId: String, bookUserId: String, initialMessage: ChatMessage): String {
        val currentUser = auth.currentUser?.uid ?: ""
        val chatId = database.push().key ?: ""

        val chat = Conversation(
            id = chatId,
            userIds = listOf(currentUser, bookUserId),
            lastMessage = initialMessage.message,
            lastTimestamp = SimpleDateFormat.getInstance().format(Date()),
            bookId = bookId
        )

        database.child(chatId).setValue(chat).await()
        sendMessage(chatId, initialMessage)
        return chatId
    }

    suspend fun getExistingChat(bookId: String, userId: String, bookUserId: String): Conversation? {
        val snapshot = database.orderByChild("bookId").equalTo(bookId).get().await()

        for (childSnapshot in snapshot.children) {
            val chat = childSnapshot.getValue(Conversation::class.java)
            if (chat != null && chat.userIds.contains(userId) && chat.userIds.contains(bookUserId)) {
                return chat
            }
        }
        return null
    }

    suspend fun getConversationByChatId(chatId: String): Conversation? {
        val snapshot = database.child(chatId).get().await()
        return snapshot.getValue(Conversation::class.java)
    }

    suspend fun deleteChat(chatId: String, userId: String) {
        // Obtener los detalles del chat
        val chatSnapshot = database.child(chatId).get().await()
        val chat = chatSnapshot.getValue(Conversation::class.java)

        // Verificar que el chat exista y que el usuario sea el propietario del libro
        if (chat != null && chat.userIds.contains(userId)) {
            database.child(chatId).removeValue().await()
        } else {
            throw IllegalArgumentException("No tienes permiso para eliminar este chat.")
        }
    }
    suspend fun deleteAllUserChats(userId: String) {
            val userChats = getUserChats(userId)
            if (userChats.isNotEmpty()) {
                for (chat in userChats) {
                    deleteChat(chat.id, userId)
                }
            } else {
                Log.d("ChatManager", "El usuario no participa en ningún chat")
            }
    }



}