@file:Suppress("NAM E_SHADOWING", "NAME_SHADOWING")

package com.grupo10.readshare.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.model.ChatViewModel
import com.grupo10.readshare.model.MapViewModel
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.storage.ChatManager
import com.grupo10.readshare.storage.StorageManager
import com.grupo10.readshare.ui.theme.screens.BookScreen
import com.grupo10.readshare.ui.theme.screens.Charge
import com.grupo10.readshare.ui.theme.screens.ChatScreen
import com.grupo10.readshare.ui.theme.screens.ConversationsScreen
import com.grupo10.readshare.ui.theme.screens.Login
import com.grupo10.readshare.ui.theme.screens.Main
import com.grupo10.readshare.ui.theme.screens.MapScreen
import com.grupo10.readshare.ui.theme.screens.NewChatScreen
import com.grupo10.readshare.ui.theme.screens.Sigin
import com.grupo10.readshare.ui.theme.screens.UploadBook
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(mapViewModel: MapViewModel, chatViewModel: ChatViewModel,chatManager: ChatManager, authManager: AuthManager, storageManager: StorageManager, context: Context) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val flagBook = remember {
        mutableStateOf(false)
    }
    var currentUser = authManager.getCurrentUser()
    var book: Book  = Book()

    NavHost(navController = navController, startDestination = AppScreens.Charge.route) {
        composable(route = AppScreens.Charge.route) {
            Charge()
            viewModel.setUserId(null.toString())
            scope.launch {
                delay(1800)
                if (currentUser?.uid?.isNotEmpty() == true) {
                    // Si hay un usuario autenticado, navegar directamente a la pantalla principal
                    currentUser!!.uid.let { viewModel.setUserId(it) }
                    navController.navigate(AppScreens.Main.route) {
                        popUpTo(AppScreens.Charge.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                } else {
                    // Si no hay usuario autenticado, navegar a la pantalla de carga
                    navController.navigate(AppScreens.Login.route) {
                        popUpTo(AppScreens.Charge.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }

        }
        composable(route = AppScreens.Login.route) {
            Login(navController,authManager)
        }
        composable(route = AppScreens.Sigin.route) {
            Sigin(navController, authManager) { id ->
                viewModel.setUserId(id)
            }
        }
        composable(route = AppScreens.Main.route) {
            Main( authManager, navController, storageManager){
                flagBook.value=it
            }
        }
        composable(route = AppScreens.Upload.route) {
            UploadBook(navController,flagBook.value){
                book = it
            }
        }
        composable(route = AppScreens.Map.route) {
            MapScreen(viewModel = mapViewModel, navController, book, storageManager)
        }
        composable("book/{bookId}") { backStackEntry ->
            LaunchedEffect (Unit){
                launch { storageManager.getBooks().collect{
                    books = it
                } }
            }
            val bookId = backStackEntry.arguments?.getString("bookId")
            val book = books.find { it.id == bookId }  // Reemplaza `allBooks` con tu lista de libros
            book?.let {
                authManager.getCurrentUser()?.reload()
                currentUser = authManager.getCurrentUser()
                if (currentUser != null) {
                    BookScreen(book = it, currentUser!!.uid,storageManager,authManager, chatManager, navController, context)
                }
            }
        }

        composable("conversations") {
            ConversationsScreen(navController, chatViewModel, authManager, storageManager)
        }
        composable("chat/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId")
            if (chatId != null) {
                ChatScreen(chatId, chatViewModel, context)
            }
        }
        composable("newChat/{bookId}/{bookUserId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            val bookUserId = backStackEntry.arguments?.getString("bookUserId")
            if (bookId != null && bookUserId != null) {
                NewChatScreen(bookId, bookUserId, chatViewModel, navController)
            }
        }
    }
}

class MainViewModel : ViewModel() {
    val userId = mutableStateOf("")

    fun setUserId(id: String) {
        userId.value = id
    }
}

