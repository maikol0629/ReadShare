package com.grupo10.readshare.ui.theme.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.grupo10.readshare.R
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.model.User
import com.grupo10.readshare.navigation.AppScreens
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.storage.StorageManager
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun Main(
    auth: AuthManager,
    navController: NavController,
    storage: StorageManager,
    flag: (Boolean) -> Unit
) {
    var currentScreen by remember { mutableStateOf(BottomBarScreen.Home) }
    var bookMenuFlag by remember { mutableStateOf(false) }
    val name by remember { mutableStateOf("") }
    var user by remember { mutableStateOf(User()) }
    LaunchedEffect(Unit) {
        launch {
            auth.getUserData()?.let {
                user= it
            }
        }
    }
    Scaffold(
        bottomBar = {
            BottomBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        },
        containerColor = colorResource(id = R.color.login),
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (currentScreen == BottomBarScreen.Home || currentScreen == BottomBarScreen.Account) {
                FloatingActionButton(
                    onClick = { bookMenuFlag = true },
                    modifier = Modifier.size(60.dp),
                    containerColor = colorResource(id = R.color.background1)
                ) {
                    Box {
                        if (bookMenuFlag) {
                            BookType(
                                bol = bookMenuFlag,
                                bookFlag = { flag(it) },
                                rt = { bookMenuFlag = it }, navController = navController
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.add_book),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(5.dp)
                                .fillMaxSize(),
                            tint = colorResource(id = R.color.white)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentScreen) {
                BottomBarScreen.Home -> HomeScreen(storage = storage, navController)
                BottomBarScreen.Search -> SearchScreen()
                BottomBarScreen.Menu -> MenuScreen()
                BottomBarScreen.Account ->  AccountScreen(user, authManager = auth, navController = navController, storage = storage)
            }
        }
    }
}

@Composable
fun BottomBar(currentScreen: BottomBarScreen, onScreenSelected: (BottomBarScreen) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        BottomBarIcon(
            screen = BottomBarScreen.Home,
            currentScreen = currentScreen,
            onScreenSelected = onScreenSelected
        )
        BottomBarIcon(
            screen = BottomBarScreen.Search,
            currentScreen = currentScreen,
            onScreenSelected = onScreenSelected
        )
        BottomBarIcon(
            screen = BottomBarScreen.Menu,
            currentScreen = currentScreen,
            onScreenSelected = onScreenSelected
        )
        BottomBarIcon(
            screen = BottomBarScreen.Account,
            currentScreen = currentScreen,
            onScreenSelected = onScreenSelected
        )
    }
}

@Composable
fun BottomBarIcon(
    screen: BottomBarScreen,
    currentScreen: BottomBarScreen,
    onScreenSelected: (BottomBarScreen) -> Unit
) {
    val icon = when (screen) {
        BottomBarScreen.Home -> R.drawable.main
        BottomBarScreen.Search -> R.drawable.search
        BottomBarScreen.Menu -> R.drawable.menu
        BottomBarScreen.Account -> R.drawable.acount
    }
    val tint = if (screen == currentScreen) Color.Black else Color.Gray

    IconButton(onClick = { onScreenSelected(screen) }) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "",
            tint = tint
        )
    }
}

enum class BottomBarScreen {
    Home, Search, Menu, Account
}

@Composable
fun HomeScreen(storage: StorageManager, navController: NavController) {
    var sale by remember { mutableStateOf<List<Book>>(emptyList()) }
    var exchange by remember { mutableStateOf<List<Book>>(emptyList()) }
    var all by remember { mutableStateOf<List<Book>>(emptyList()) }

    LaunchedEffect(Unit) {
        launch {
            storage.getBooks().collect {
                sale = it.filter { book -> book.price.isNotEmpty() }
                exchange = it.filter { book -> book.price.isEmpty() }
                all = it
                Log.i("books", all.toString())
            }
        }
    }

    if (all.isEmpty()) {
        Text(text = "Cargando libros...")
    } else {
        RowWithCards(books = sale, title = "Venta", navController = navController)
        Spacer(modifier = Modifier.height(8.dp))
        RowWithCards(books = exchange, title = "Intercambio", navController = navController)
    }
}

@Composable
fun SearchScreen() {
    Text("Search Screen", fontWeight = FontWeight.Bold, color = Color.Black)
}

@Composable
fun MenuScreen() {
    Text("Menu Screen", fontWeight = FontWeight.Bold, color = Color.Black)
}

@Composable
fun AccountScreen(user: User, authManager: AuthManager, navController: NavController, storage: StorageManager) {
    Account(user, authManager, navController =navController , storage = storage)
}


@Composable
fun Card(book: Book, navController: NavController) {
    val painter = rememberImagePainter(data = book.images[0])
    Surface(
        modifier = Modifier
            .padding(8.dp)
            .width(160.dp)
            .height(240.dp)
            .clickable {
                navController.navigate("book/${book.id}")
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(painter = painter, contentDescription = book.title, modifier = Modifier.size(120.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = book.title, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = book.user, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
fun RowWithCards(books: List<Book>, title: String, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(modifier = Modifier.fillMaxWidth()) {
            items(items = books) { book ->
                Card(book, navController)
            }
        }
    }
}



@Composable
fun BookType(bol:Boolean,
             navController: NavController,
             rt:(Boolean)->Unit,
             bookFlag :(Boolean)->Unit
            ) {
    val menuItems = stringArrayResource(id = R.array.v_i)
    val expanded = remember { mutableStateOf(bol) }
   // Column(modifier = Modifier.padding(16.dp)) {
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = {
                expanded.value = false
                rt(false)
                               },
        ) {
            menuItems.forEach { item ->
                DropdownMenuItem(onClick = {
                    if (item =="Vender") {
                        bookFlag(true)
                        navController.navigate(AppScreens.Upload.route)
                    }else{
                        navController.navigate(AppScreens.Upload.route)
                    }
                },
                    text = {Text(text = item)} )
            }

    }
}