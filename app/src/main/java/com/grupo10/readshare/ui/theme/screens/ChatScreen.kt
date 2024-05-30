package com.grupo10.readshare.ui.theme.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.model.ChatViewModel
import com.grupo10.readshare.model.User
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.storage.ChatManager
import com.grupo10.readshare.storage.ChatMessage
import com.grupo10.readshare.storage.Conversation
import com.grupo10.readshare.storage.StorageManager
import java.text.SimpleDateFormat

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(navController: NavController, viewModel: ChatViewModel) {
    val chats by viewModel.chats.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Chats") })
        },
        content = {
            LazyColumn {
                items(chats) { chat ->
                    ChatItem(chat, onClick = {
                        navController.navigate("chat/${chat.id}")
                    })
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Handle new chat creation here */ }) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
            }
        }
    )
}

@Composable
fun ChatItem(chat: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Text(chat.lastMessage, style = MaterialTheme.typography.bodyLarge)
            Text("Last updated: ${SimpleDateFormat.getInstance().format(chat.lastTimestamp.toDate())}", style = MaterialTheme.typography.bodySmall)
            Text("Book ID: ${chat.bookId}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(16.dp)) {
        Column {
            Text(text = conversation.lastMessage, style = MaterialTheme.typography.bodyLarge)
            Text(text = SimpleDateFormat.getInstance().format(conversation.lastTimestamp.toDate()), style = MaterialTheme.typography.bodySmall)
            Text(text = "Book ID: ${conversation.bookId}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ChatScreen(chatId: String, viewModel: ChatViewModel, context: Context) {
    val chatManager = ChatManager(context)
    val storageManager = StorageManager(context)
    val authManager = AuthManager(context, context as Activity)
    val messages by viewModel.getChatMessages(chatId).collectAsState(initial = emptyList())
    var chat by remember { mutableStateOf<Conversation?>(null) }
    var book by remember { mutableStateOf<Book?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val retrievedChat = chatManager.getConversationByChatId(chatId)
        chat = retrievedChat
        if (retrievedChat != null) {
            Log.i("chat", chat.toString())
            val retrievedBook = storageManager.getBookById(retrievedChat.bookId)
            book = retrievedBook
            if (retrievedBook != null) {
                Log.i("book", book.toString())
                user = authManager.getUserDataByID(retrievedBook.user)
                Log.i("user", user.toString())
                snapshotFlow { user }.collect{}            }
        }
    }

    Column {
        if (user != null) {
            ChatRow(userName = user!!.name, userProfileImageUrl = user!!.image)
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                MessageItem(message)
            }
        }
        Row(modifier = Modifier.padding(8.dp)) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                viewModel.sendMessage(chatId, messageText)
                messageText = ""
            }) {
                Text("Send")
            }
        }
    }
}

@Composable
fun ChatRow(userName: String, userProfileImageUrl: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(8.dp)
    ) {
        Image(
            painter = rememberImagePainter(userProfileImageUrl),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = userName,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun MessageItem(message: ChatMessage) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(message.message, style = MaterialTheme.typography.titleSmall)
        Text("Sent by: ${message.senderId}", style = MaterialTheme.typography.bodySmall)
    }
}
@Composable
fun NewChatScreen(bookId: String, bookUserId: String, viewModel: ChatViewModel, navController: NavController) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var user by remember { mutableStateOf(User()) }
    val authManager = remember { AuthManager(context, context as Activity) }

    // Fetch user details
    LaunchedEffect(bookUserId) {
        user = authManager.getUserDataByID(bookUserId) ?: User()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberImagePainter(user.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(4.dp)
                    .size(100.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = user.name, style = MaterialTheme.typography.bodyMedium)
                Text(text = user.email, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = messageText,
            onValueChange = { messageText = it },
            label = { Text("Mensaje") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Button(
            onClick = {
                viewModel.createChatFromBook(bookId, bookUserId, messageText) { chatId ->
                    navController.navigate("chat/$chatId")
                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(8.dp)
        ) {
            Text("Iniciar Chat")
        }
    }
}

