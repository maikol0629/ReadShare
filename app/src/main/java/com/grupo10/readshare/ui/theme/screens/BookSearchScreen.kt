package com.grupo10.readshare.ui.theme.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.storage.StorageManager
import kotlinx.coroutines.launch

@Composable
fun BookSearchScreen(storageManager: StorageManager, navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val books = remember { mutableStateListOf<Book>() }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
                coroutineScope.launch {
                    storageManager.searchBooksByTitle(newQuery).collect { result ->
                        books.clear()
                        books.addAll(result)
                    }
                }
            },
            label = { Text("Search Books") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RowWithCards(books = books, title = "Search Results", navController = navController)
    }
}