package com.grupo10.readshare.ui.theme.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.grupo10.readshare.R
import com.grupo10.readshare.navigation.AppScreens
import com.google.firebase.auth.FirebaseAuth
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.storage.StorageManager
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun Main(
    token : String,
    navController: NavController,
    flag:(Boolean)->Unit
){
    var bookMenuFlag by remember { mutableStateOf(false) }
    var name by remember {
        mutableStateOf("")
    }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val storage = StorageManager(context)
    Scaffold(
        bottomBar = {
            
            Row (horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp)) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(painter = painterResource(id = R.drawable.main),
                        contentDescription = "", tint = colorResource(id = R.color.black))
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(painter = painterResource(id = R.drawable.search),
                        contentDescription = "")
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(painter = painterResource(id = R.drawable.menu),
                        contentDescription = "", tint = colorResource(id = R.color.black))
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(painter = painterResource(id = R.drawable.acount),
                        contentDescription = "", tint = colorResource(id = R.color.black))
                }
            }
            
        },
        containerColor = colorResource(id = R.color.background1),
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = { bookMenuFlag = true},
                modifier = Modifier
                    .size(60.dp),
                containerColor = colorResource(id = R.color.background1)
                                   ) {
                Box {
                    if (bookMenuFlag) {
                        addBook(bol = bookMenuFlag, navController = navController, bookFlag = {flag(it)}, rt = {bookMenuFlag=it})

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

        ) {innerPadding ->
        Column(modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = name)

            Button(onClick = { FirebaseAuth.getInstance().signOut()
                    navController.navigate(route = AppScreens.Login.route){
                        popUpTo(AppScreens.Main.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }


            }) {

                Text(text = "Cerrar sesión")
            }


            var sale by remember {
                mutableStateOf<List<Book>>(emptyList())
            }
            var exchange by remember {
                mutableStateOf<List<Book>>(emptyList())
            }

            LaunchedEffect(Unit) {
                launch {

                    storage.getBooks().collect{

                        sale = it.filter { book -> book.precio.isNotEmpty() }
                        exchange = it.filter { book -> book.precio.isEmpty() }
                    }




                }


            }


            if (sale.isEmpty()||exchange.isEmpty()) {
                Text(text = "Cargando libros...")
            } else {

                RowWithCards(books = sale, title = "Venta")
                Spacer(modifier = Modifier.height(8.dp))
                RowWithCards(books = exchange, title = "Intercambio")

            }




        }
        
    }
    
}




@Composable
fun Card(book: Book) {
    val painter = rememberImagePainter(
        data = book.images[0]
    )
    Surface(
        modifier = Modifier
            .padding(8.dp)
            .width(150.dp)
            .height(180.dp),
        shape = RectangleShape,
        color = colorResource(id = R.color.background1),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = book.title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Image(painter = painter, contentDescription = null)
        }
    }
}


@Composable
fun RowWithCards(books:List<Book>, title:String) {
    Column(modifier = Modifier.background(color = colorResource(id = R.color.background1)), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)

        LazyRow(modifier = Modifier.fillMaxWidth()) {
            items(items = books) { item ->
                Card(item)
            }
        }
    }
}

@Composable
@Preview
fun Prow(){
    val book=Book(title = "title", images = listOf("https://firebasestorage.googleapis.com/v0/b/readshare-a4dcf.appspot.com/o/books%2Fnayi123%40gmail.com%2Fnayi123%40gmail.comt%C3%ADtulo%20dos%2F0?alt=media&token=29adb173-66ed-48c9-9295-96f3852550da"))
    val list = listOf(book
    ,book,book)
RowWithCards(books = list,"Venta")


}
@Composable
fun addBook(bol:Boolean,
            navController: NavController,
            rt:(Boolean)->Unit,
            bookFlag :(Boolean)->Unit
            ) {
    val menuItems = stringArrayResource(id = R.array.books)
    val selectedItem = remember { mutableStateOf("") }
    val expanded = remember { mutableStateOf(bol) }
    Column(modifier = Modifier.padding(16.dp)) {
        
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false
                               rt(false)},
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
}