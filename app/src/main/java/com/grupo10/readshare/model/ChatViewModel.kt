package com.grupo10.readshare.model


import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.storage.ChatManager
import com.grupo10.readshare.storage.ChatMessage
import com.grupo10.readshare.storage.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.Date


class ChatViewModel(private val authManager: AuthManager) : ViewModel() {
    private val repository: ChatManager = ChatManager()
    private val _chats = MutableStateFlow<List<Conversation>>(emptyList())
    val chats: StateFlow<List<Conversation>> = _chats

    init {
        viewModelScope.launch {
            refreshUserChats()
        }
    }

    fun refreshUserChats() {
        viewModelScope.launch {
            authManager.getCurrentUser()?.reload()
            val userId = authManager.getUserUid()
            Log.i("ChatViewModel", "User ID: $userId")
            if (userId != null) {
                _chats.value = repository.getUserChats(userId)
                Log.i("ChatViewModel", "Chats: ${_chats.value}")
            }
        }
    }

    fun getChatMessages(chatId: String): Flow<List<ChatMessage>> {
        return flow {
            emit(repository.getChatMessages(chatId))
        }
    }

    fun sendMessage(chatId: String, messageText: String, receiverId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val message = ChatMessage(
                receiverId = receiverId,
                senderId = userId,
                message = messageText,
                timestamp = SimpleDateFormat.getInstance().format(Date())
            )
            viewModelScope.launch {
                repository.sendMessage(chatId, message)
                refreshUserChats()
            }
        }
    }

    fun createChatFromBook(bookId: String, bookUserId: String, initialMessage: String, onComplete: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val message = ChatMessage(
                senderId = userId,
                message = initialMessage,
                timestamp = SimpleDateFormat.getInstance().format(Date())
            )
            viewModelScope.launch {
                val chatId = repository.createChatFromBook(bookId, bookUserId, message)
                onComplete(chatId)
                refreshUserChats()
            }
        }
    }

    suspend fun getConversationByChatId(chatId: String): Conversation? {
        return repository.getConversationByChatId(chatId)
    }
    fun deleteChat(chatId: String) {
        val userId = authManager.getUserUid()
        if (userId != null) {
            viewModelScope.launch {
                try {
                    repository.deleteChat(chatId, userId)
                    refreshUserChats() // Actualizar la lista de chats después de eliminar un chat
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error deleting chat: ${e.message}")
                }
            }
        }
    }
}
