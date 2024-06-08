package com.grupo10.readshare.ui.theme.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.grupo10.readshare.R
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.model.ChatViewModel
import com.grupo10.readshare.model.User
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.storage.ChatMessage
import com.grupo10.readshare.storage.Conversation
import com.grupo10.readshare.storage.StorageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    navController: NavController,
    viewModel: ChatViewModel,
    authManager: AuthManager,
    storageManager: StorageManager
) {
    val chats by viewModel.chats.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        viewModel.refreshUserChats()
        delay(1000)
    }

    Log.i("chats", chats.toString())

    Scaffold(
        topBar = {
            TopAppBar(colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = colorResource(id = R.color.background2)),
                title = { Text("Chats") },
                )
        },
        containerColor = colorResource(id = R.color.background2)
    ){
        LazyColumn (modifier = Modifier
            .background(colorResource(id = R.color.background2))
            .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            userScrollEnabled = true){
            items(chats) { chat ->
                ChatItem(
                    chat = chat,
                    authManager = authManager,
                    storageManager = storageManager,
                    onClick = {
                        navController.navigate("chat/${chat.id}")
                    },
                    onDelete = {
                        viewModel.deleteChat(chat.id)
                    }
                )
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: Conversation,
    authManager: AuthManager,
    storageManager: StorageManager,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var user by remember { mutableStateOf(User()) }
    var book by remember { mutableStateOf(Book()) }
    var painter = painterResource(id = R.drawable.profile)

    LaunchedEffect(chat) {
        val otherUserId = chat.userIds.find { it != authManager.getUserUid() }
        if (otherUserId != null) {
            authManager.getUserDataByID(otherUserId)?.let { fetchedUser ->
                user = fetchedUser
            }
        }
        storageManager.getBookById(chat.bookId)?.let { fetchedBook ->
            book = fetchedBook
        }
    }

    if (user.image.isNotEmpty()) {
        painter = rememberAsyncImagePainter(user.image)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
            .background(colorResource(id = R.color.label)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painter,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(book.title, style = MaterialTheme.typography.bodyLarge)
            Text(" ${chat.lastTimestamp} ${chat.lastMessage}", style = MaterialTheme.typography.bodySmall)
        }
        if (authManager.getUserUid() == book.user) {
            IconButton(onClick = { onDelete() }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Chat")
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ChatScreen(chatId: String, viewModel: ChatViewModel, context: Context) {
    val authManager = AuthManager(context, context as Activity)
    val messages by viewModel.getChatMessages(chatId).collectAsState(initial = emptyList())
    var chat by remember { mutableStateOf<Conversation?>(null) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var receiverUser by remember { mutableStateOf<User?>(null) }
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        launch {
            val fetchedChat = viewModel.getConversationByChatId(chatId)
            chat = fetchedChat
            val userIds = chat?.userIds as List<*>
            userIds.forEach{id->
                if (id.toString() == authManager.getUserUid()){
                    currentUser = authManager.getUserDataByID(id.toString())
                }else{
                    receiverUser = authManager.getUserDataByID(id.toString())
                }
            }
        }
    }
    Column(modifier = Modifier.background(colorResource(id = R.color.background2)
        )) {
        if (receiverUser != null) {
            ChatRow(userName = receiverUser!!.name, userProfileImageUrl = receiverUser!!.image)
        }
        LazyColumn(modifier = Modifier
            .weight(1f)
            .background(colorResource(id = R.color.label))) {
            items(messages) { message ->
                val isCurrentUserMessage = message.senderId == authManager.getUserUid()
                val arrangement = if (isCurrentUserMessage) {
                    Arrangement.End
                } else {
                    Arrangement.Start
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = arrangement) {
                    MessageItem(message)
                }
            }
        }
        Row(modifier = Modifier.padding(8.dp)) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                if (chat != null) {
                    viewModel.sendMessage(chatId, messageText, receiverUser!!.id)
                    messageText = ""
                }
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
        Text(message.message, style = MaterialTheme.typography.bodyLarge, color = colorResource(id = R.color.white))
        Text(" ${message.timestamp}", style = MaterialTheme.typography.labelSmall, color = colorResource(id = R.color.login))
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
            .background(colorResource(id = R.color.background2))
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



