package com.grupo10.readshare.ui.theme.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.model.User
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.storage.StorageManager
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Account(user: User, authManager: AuthManager, navController: NavController, storage:StorageManager){
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {




            LaunchedEffect(Unit) {
                launch {
                    storage.getBooksUser().collect {
                        books = it
                        Log.i("books", it.toString())
                    }
                }
            }
            ProfileHeader(user)
            Spacer(modifier = Modifier.height(16.dp))

            if (books.isEmpty()) {
                Text(text = "Cargando libros...")
            } else {
                Log.i("If", user.toString())

                RowWithCards(books,"My Books")
            }
        }
    }
}

@Composable
fun ProfileHeader(user: User) {

    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = rememberImagePainter(user.image),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "${user.name} ${user.lastName}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val user = User()
        user.image = "https://example.com/profile.jpg"
        user.name = "John"
        user.lastName = "Doe"


    Account(user, AuthManager( LocalContext.current, LocalContext.current as Activity), NavController(LocalContext.current), StorageManager(LocalContext.current))
}