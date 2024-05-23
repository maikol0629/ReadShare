package com.grupo10.readshare.ui.theme.screens

import android.graphics.Canvas
import android.location.Geocoder
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.grupo10.readshare.model.MapViewModel
import com.grupo10.readshare.navigation.AppScreens
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.util.Locale

@Composable
fun MapScreen(viewModel: MapViewModel, navController: NavController, address: (String) -> Unit) {
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()

    AndroidView(factory = { context ->
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)

            // Enable built-in zoom controls
            setMultiTouchControls(true)
            controller.setZoom(20.0)

            // Center the map on the user's location if available
            userLocation?.let {
                controller.setCenter(it)
                val userMarker = Marker(this).apply {
                    position = it
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    // Set your user marker icon
                }
                overlays.add(userMarker)
            }

            // Add touch listener to detect clicks
            overlays.add(object : Overlay() {
                override fun draw(canvas: Canvas?, mapView: MapView?, shadow: Boolean) {
                    // No drawing required, this is just for touch handling
                }

                override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                    e?.let {
                        val geoPoint = mapView?.projection?.fromPixels(e.x.toInt(), e.y.toInt()) as? GeoPoint
                        geoPoint?.let { point ->
                            viewModel.updateSelectedLocation(point)
                            Log.i("MapScreen", "Clicked at: ${point.latitude}, ${point.longitude}")
                        }
                    }
                    return true
                }
            })
        }
    }, modifier = Modifier.fillMaxSize())

    LaunchedEffect(selectedLocation) {
        Log.i("location", "Selected Location: $selectedLocation")
        selectedLocation?.let { location ->
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.let { address ->
                val addressText = address.getAddressLine(0) ?: "Address not found"
                address(addressText)
                Log.i("address", "Address: $addressText")
                navController.navigate(AppScreens.Welcome.route)
            }
        }
    }
}