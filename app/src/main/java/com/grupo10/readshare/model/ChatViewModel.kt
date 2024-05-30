package com.grupo10.readshare.model

import android.content.Context
import android.icu.text.SimpleDateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.grupo10.readshare.storage.ChatManager
import com.grupo10.readshare.storage.ChatMessage
import com.grupo10.readshare.storage.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ChatViewModel(ctx:Context) : ViewModel() {
    private val repository: ChatManager = ChatManager(ctx)
    private val _chats = MutableStateFlow<List<Conversation>>(emptyList())
    val chats: StateFlow<List<Conversation>> = _chats

    init {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                _chats.value = repository.getUserChats(userId)
            }
        }
    }

    fun getChatMessages(chatId: String): Flow<List<ChatMessage>> {
        return flow {
            emit(repository.getChatMessages(chatId))
        }
    }

    fun sendMessage(chatId: String, messageText: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val message = ChatMessage(
                senderId = userId,
                message = messageText,
                timestamp = SimpleDateFormat.getInstance().format(Timestamp.now().toDate())
            )
            viewModelScope.launch {
                repository.sendMessage(chatId, message)
            }
        }
    }

    // Crear chat basado en libro
    fun createChatFromBook(bookId: String, bookUserId: String, initialMessage: String, onComplete: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val message = ChatMessage(
                senderId = userId,
                message = initialMessage,
                timestamp = SimpleDateFormat.getInstance().format(Timestamp.now().toDate())
            )
            viewModelScope.launch {
                val chatId = repository.createChatFromBook(bookId, bookUserId, message)
                onComplete(chatId)
            }
        }
    }
}
