package com.grupo10.readshare.ui.theme.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.view.MotionEvent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import coil.compose.rememberImagePainter
import com.grupo10.readshare.R
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.model.User
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.storage.ChatManager
import com.grupo10.readshare.storage.ChatMessage
import com.grupo10.readshare.storage.StorageManager
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.util.Locale

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    book: Book,
    uid: String,
    storageManager: StorageManager,
    authManager: AuthManager,
    chatManager: ChatManager,
    navController: NavController, context: Context
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showMapDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(book.title) }
    var description by remember { mutableStateOf(book.description) }
    var genero by remember { mutableStateOf(book.genero) }
    var address by remember { mutableStateOf(book.address) }
    var price by remember { mutableStateOf(book.price) }
    var user by remember { mutableStateOf<User?>(null) }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }

    LaunchedEffect(book.user) {
        val fetchedUser = authManager.getUserDataByID(book.user)
        user = fetchedUser
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Image(painter = painterResource(id = R.drawable.rs), contentDescription = "") }, modifier = Modifier.background(color = colorResource(id = R.color.background2)))
        },
        containerColor = colorResource(id = R.color.login),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(color = colorResource(id = R.color.background2)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            user?.let {
                ProfileScreen(user = it)
            }

            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(8.dp),
                readOnly = !isEditing
            )

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(8.dp),
                readOnly = !isEditing
            )

            if (isEditing) {
                GeneroLiteraturaDropdown(
                    generos = stringArrayResource(id = R.array.generos), book.genero,
                    onGeneroSelected = { genero = it }
                )
            } else {
                TextField(
                    value = genero,
                    onValueChange = { genero = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(8.dp),
                    readOnly = !isEditing
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(200.dp)
            ) {
                items(book.images.size) { index ->
                    BookImage(imageUrl = book.images[index])
                }
            }

            Row(modifier = Modifier.clickable {
                if (isEditing) {
                    showMapDialog = true
                }
            }) {
                TextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Direccion de Encuentro") },
                    modifier = Modifier
                        .padding(8.dp)
                        .background(color = Color.LightGray, shape = RoundedCornerShape(4.dp))
                        .fillMaxWidth(0.9f),
                    readOnly = true
                )
            }

            if (book.price.isNotEmpty()) {
                TextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    readOnly = !isEditing
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uid == book.user) {
                    if (isEditing) {
                        Button(
                            onClick = {
                                val updatedBook = book.copy(
                                    title = title,
                                    description = description,
                                    genero = genero,
                                    address = address,
                                    price = price
                                )
                                scope.launch {
                                    storageManager.updateBook(updatedBook)
                                    isEditing = false
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(text = "Guardar")
                        }
                    } else {
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(text = "Editar")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    storageManager.deleteBook(book)
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(text = "Eliminar")
                        }
                    }
                } else {
                    IconButton(onClick = {
                        scope.launch {
                            val existingChat = chatManager.getExistingChat(book.id, uid, book.user)
                            if (existingChat != null) {
                                navController.navigate("chat/${existingChat.id}")
                            } else {
                                val newChatId = chatManager.createChatFromBook(book.id, book.user, ChatMessage(
                                    senderId = uid,
                                    message = "Hola, estoy interesado en tu libro."
                                )
                                )
                                navController.navigate("chat/$newChatId")
                            }
                        }
                    }) {
                        Icon(Icons.Default.MailOutline, contentDescription = "New Chat")
                    }
                }
            }
        }
    }

    if (showMapDialog) {
        MapDialog(
            location = book.ubication,
            selectedLocation = selectedLocation,
            onLocationSelected = { newLocation ->
                selectedLocation = newLocation
                address = getAddressFromLocation(newLocation, context)
                book.address = address
                book.ubication = newLocation.toString()
                showMapDialog = false
            },
            onDismiss = { showMapDialog = false }
        )
    }
}

fun getAddressFromLocation(newLocation: GeoPoint, context: Context): String {
    var addressText: String = ""
    newLocation.let { location ->
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        addresses?.firstOrNull()?.let { address ->
            addressText = address.getAddressLine(0) ?: "Address not found"
        }
    }
    return addressText
}

@Composable
fun BookImage(imageUrl: String) {
    Image(
        painter = rememberImagePainter(imageUrl),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .padding(4.dp)
            .size(150.dp)
    )
}

@Composable
fun MapDialog(
    location: String,
    selectedLocation: GeoPoint?,
    onLocationSelected: (GeoPoint) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            MapViewComponent(location = location, selectedLocation = selectedLocation, onLocationSelected = onLocationSelected)
        }
    }
}

@Composable
fun MapViewComponent(
    location: String,
    selectedLocation: GeoPoint?,
    onLocationSelected: (GeoPoint) -> Unit
) {
    val coords = location.split(",")
    val latitude = coords[0].toDouble()
    val longitude = coords[1].toDouble()
    val bookLocation = GeoPoint(latitude, longitude)
    AndroidView(factory = { context ->
        val mapView = MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(bookLocation)
        }

        val bookMarker = Marker(mapView).apply {
            position = bookLocation
            icon = resizeDrawable(context, R.drawable.alfiler, 25, 25)
            setOnMarkerClickListener { _, _ ->
                true
            }
        }
        mapView.overlays.add(bookMarker)

        selectedLocation?.let {
            val selectedMarker = Marker(mapView).apply {
                position = it
                icon = resizeDrawable(context, R.drawable.ubicacion, 25, 25)
            }
            mapView.overlays.add(selectedMarker)
        }

        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                e?.let {
                    val geoPoint = mapView?.projection?.fromPixels(e.x.toInt(), e.y.toInt()) as? GeoPoint
                    geoPoint?.let { point ->
                        onLocationSelected(point)
                    }
                }
                return true
            }
        })

        mapView
    }, modifier = Modifier.fillMaxSize())
}

fun resizeDrawable(context: Context, drawableId: Int, width: Int, height: Int): Drawable {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    drawable?.let {
        val bitmap = Bitmap.createScaledBitmap(
            (it as BitmapDrawable).bitmap,
            width,
            height,
            false
        )
        return BitmapDrawable(context.resources, bitmap)
    }
    throw IllegalArgumentException("Drawable not found")
}

@Composable
fun ProfileScreen(user: User) {
    Row {
        Image(
            painter = rememberImagePainter(user.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(4.dp)
                .size(100.dp)
        )
        Column {
            Text(text = user.name)
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = user.email)
        }
    }
}


