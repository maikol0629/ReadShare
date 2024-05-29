package com.grupo10.readshare.ui.theme.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import coil.compose.rememberImagePainter
import com.grupo10.readshare.R
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.storage.StorageManager
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun BookScreen(
    book: Book,
    uid: String,
    storageManager: StorageManager,
    navController: NavController
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var showMapDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(book.title) }
    var description by remember { mutableStateOf(book.description) }
    var genero by remember { mutableStateOf(book.genero) }
    var address by remember { mutableStateOf(book.address) }
    var price by remember { mutableStateOf(book.price) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            readOnly = !isEditing
        )

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            readOnly = !isEditing
        )

        GeneroLiteraturaDropdown(
            generos = stringArrayResource(id = R.array.generos),
            onGeneroSelected = { genero = it }
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            items(book.images.size) { index ->
                BookImage(imageUrl = book.images[index])
            }
        }

        TextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Dirección de encuentro") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            readOnly = !isEditing
        )

        if (book.price.isNotEmpty()) {
            TextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
                            // Guardar cambios
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
                }
            }

            Button(
                onClick = { /* Handle delete book */ },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(text = "Eliminar")
            }
        }
    }

    if (showMapDialog) {
        MapDialog(location = book.ubication, onDismiss = { showMapDialog = false })
    }
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
fun MapDialog(location: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                MapViewComponent(location = location)
            }
        }
    }
}

@Composable
fun MapViewComponent(location: String) {
    AndroidView(factory = { context ->
        val mapView = MapView(context)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val coords = location.split(",")
        val latitude = coords[0].toDouble()
        val longitude = coords[1].toDouble()
        val startPoint = GeoPoint(latitude, longitude)

        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(startPoint)

        val marker = Marker(mapView)
        marker.position = startPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)

        mapView
    }, modifier = Modifier.fillMaxSize())
}
