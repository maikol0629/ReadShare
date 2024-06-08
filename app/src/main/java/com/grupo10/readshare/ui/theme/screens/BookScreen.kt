package com.grupo10.readshare.ui.theme.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.net.Uri
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.grupo10.readshare.R
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.model.User
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.storage.ChatManager
import com.grupo10.readshare.storage.ChatMessage
import com.grupo10.readshare.storage.StorageManager
import com.grupo10.readshare.ui.theme.ProfileScreen
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("CoroutineCreationDuringComposition", "SuspiciousIndentation",
    "MutableCollectionMutableState"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    book: Book,
    uid: String,
    storageManager: StorageManager,
    authManager: AuthManager,
    chatManager: ChatManager,
    navController: NavController,
    context: Context
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
    var showGallery by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    var images by remember { mutableStateOf(book.images.toMutableList()) }
    var newImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var deleteImages by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(book.user) {
        val fetchedUser = authManager.getUserDataByID(book.user)
        user = fetchedUser
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            newImages = newImages + it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = colorResource(id = R.color.background2)),
                title = { Image(painter = painterResource(id = R.drawable.rs), contentDescription = "") },
                modifier = Modifier.padding(16.dp))
        },
        containerColor = colorResource(id = R.color.background2),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(color = colorResource(id = R.color.login)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(user != null && (uid != book.user)) {
                ProfileScreen(user!!.image, "${user!!.name} ${user!!.lastName}", user!!.email)
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
                items(images.size) { index ->
                    Box(modifier = Modifier.padding(4.dp)) {
                        BookImage(imageUrl = images[index]) {
                            selectedImageIndex = index
                            showGallery = true
                        }
                        if (isEditing) {
                            IconButton(
                                onClick = {
                                    deleteImages = deleteImages.toMutableList().apply { add(images[index]) }
                                    images = images.toMutableList().apply { removeAt(index) }

                                },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Image")
                            }
                        }
                    }
                }
                items(newImages.size) { index ->
                    Box(modifier = Modifier.padding(4.dp)) {
                        Image(
                            painter = rememberImagePainter(newImages[index]),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(150.dp)
                        )
                        IconButton(
                            onClick = {
                                newImages = newImages.toMutableList().apply { removeAt(index) }
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete New Image")
                        }
                    }

                }
                if (isEditing) {
                    item {
                    IconButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.size(150.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Image")
                    }
                }
                }
            }

            if (showGallery) {
                ImageGalleryDialog(
                    images = images,
                    selectedImageIndex = selectedImageIndex,
                    onDismiss = { showGallery = false }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Direccion de Encuentro") },
                    modifier = Modifier
                        .padding(8.dp)
                        .background(color = Color.LightGray, shape = RoundedCornerShape(4.dp))
                        .fillMaxWidth(0.9f),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            showMapDialog = true
                        }) {
                            Icon(
                                painter = painterResource(id = if (isEditing) R.drawable.map else R.drawable.img_1),
                                contentDescription = "Ubicación",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
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
                                    price = price,
                                    images = images
                                )
                                scope.launch {
                                    deleteImages.forEach { url ->
                                        storageManager.deleteImage(url).await()
                                    }
                                    newImages.forEach { uri ->
                                        val imageUrl = storageManager.uploadImageFromUri(uri)
                                        images = images.toMutableList().apply { add(imageUrl) }
                                    }
                                    updatedBook.images = images
                                    newImages = emptyList()
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
                                    receiverId = book.user,
                                    senderId = uid,
                                    timestamp = SimpleDateFormat.getInstance().format(Date()),
                                    message = "Hola, estoy interesado en tu libro."
                                ))
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
                if (uid == book.user) {
                    selectedLocation = newLocation
                    address = getAddressFromLocation(newLocation, context)
                    book.address = address
                    book.ubication = newLocation.toString()
                }
                showMapDialog = false
            },
            onDismiss = { showMapDialog = false },
            readOnly = uid != book.user
        )
    }
}

@Composable
fun BookImage(imageUrl: String, onClick: () -> Unit) {
    Image(
        painter = rememberImagePainter(imageUrl),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .padding(4.dp)
            .size(150.dp)
            .clickable { onClick() }
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImageGalleryDialog(images: List<String>, selectedImageIndex: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            val pagerState = rememberPagerState(initialPage = selectedImageIndex)
            Column {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Gallery")
                }
                HorizontalPager(
                    state = pagerState,
                    count = images.size,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Image(
                        painter = rememberImagePainter(images[page]),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}




@Composable
fun MapDialog(
    location: String,
    selectedLocation: GeoPoint?,
    onLocationSelected: (GeoPoint) -> Unit,
    onDismiss: () -> Unit,
    readOnly: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            MapViewComponent(location = location, selectedLocation = selectedLocation, onLocationSelected = onLocationSelected, readOnly = readOnly)
        }
    }
}

@Composable
fun MapViewComponent(
    location: String,
    selectedLocation: GeoPoint?,
    onLocationSelected: (GeoPoint) -> Unit,
    readOnly: Boolean
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

        if (!readOnly) {
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
        }

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





