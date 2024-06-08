package com.grupo10.readshare.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo10.readshare.R
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.storage.StorageManager
import kotlinx.coroutines.launch

@Composable
fun BookCategoryScreen(storageManager: StorageManager, navController: NavController) {
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("All") }

    Column(modifier = Modifier
        .padding(16.dp)
        .background(color = colorResource(id = R.color.background2)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
        GeneroLiteraturaDropdown(stringArrayResource(id = R.array.generos),"Seleccionar Categoría") { category ->
            selectedCategory = category
        }
        Spacer(modifier = Modifier.height(16.dp))

        LaunchedEffect(selectedCategory) {
            launch {
                storageManager.getBooksCategory(selectedCategory).collect{
                    books = it
                }
            }
        }
        RowWithCards(books,selectedCategory,navController )

    }
}

