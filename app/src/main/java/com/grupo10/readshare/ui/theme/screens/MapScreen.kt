package com.grupo10.readshare.ui.theme.screens

import android.graphics.Canvas
import android.location.Geocoder
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.grupo10.readshare.R
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.model.MapViewModel
import com.grupo10.readshare.navigation.AppScreens
import com.grupo10.readshare.storage.StorageManager
import com.grupo10.readshare.ui.theme.ConfirmDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.util.Locale

@Composable
fun MapScreen(viewModel: MapViewModel, navController: NavController, book: Book, storageManager: StorageManager) {
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    var addressText by remember { mutableStateOf("") }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isMapReady by remember { mutableStateOf(false) }
    var mapFlag by remember { mutableStateOf(false) }
    var alertFlag by remember { mutableStateOf(false) }
    var upFlag by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(20.0)
                        mapView = this
                        isMapReady = true
                    }
                },
                update = { mapView ->
                    if (isMapReady) {
                        userLocation?.let {
                            if (marker == null) {
                                mapView.controller.setCenter(it)
                            }
                            val userMarker = Marker(mapView).apply {
                                position = it
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                // Set your user marker icon if needed
                            }
                            mapView.overlays.add(userMarker)
                        }

                        mapView.overlays.add(object : Overlay() {
                            override fun draw(canvas: Canvas?, mapView: MapView?, shadow: Boolean) {
                                // No drawing required, this is just for touch handling
                            }
                            override fun onSingleTapConfirmed(
                                e: MotionEvent?,
                                mapView: MapView?
                            ): Boolean {
                                e?.let {
                                    val geoPoint = mapView?.projection?.fromPixels(
                                        e.x.toInt(),
                                        e.y.toInt()
                                    ) as? GeoPoint
                                    geoPoint?.let { point ->
                                        viewModel.updateSelectedLocation(point)
                                        Log.i(
                                            "MapScreen",
                                            "Clicked at: ${point.latitude}, ${point.longitude}"
                                        )
                                        // Update marker position
                                        if (marker == null) {
                                            marker = Marker(mapView).apply {
                                                setAnchor(
                                                    Marker.ANCHOR_CENTER,
                                                    Marker.ANCHOR_BOTTOM
                                                )
                                                mapView.overlays.add(this)
                                            }
                                        }
                                        marker?.position = point
                                        mapView.invalidate()
                                    }
                                }
                                return true
                            }
                        })
                    }
                },
                modifier = Modifier.height(500.dp)
            )
        }

        selectedLocation?.let { location ->
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.let { address ->
                addressText = address.getAddressLine(0) ?: "Address not found"
                book.address = addressText
                book.ubication = location.toString()
                mapFlag = true
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Seleccionar ubicación de encuentro",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = addressText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (mapFlag) {
                    alertFlag = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Subir libro")
        }

        if (alertFlag) {
            ConfirmDialog(
                onConfirm = {
                    upFlag = true
                    alertFlag = false

                },
                onDismiss = { alertFlag = false }
            )
        }


        if(upFlag){
            LaunchedEffect(Unit) {
                launch {
                    book.images = storageManager.uploadImages(book, book.uris)
                    delay(500)
                    storageManager.addBook(book)
                    delay(100)
                    upFlag= false
                    navController.navigate(AppScreens.Main.route)

                }

            }
        }
    }
}
