package com.grupo10.readshare

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.grupo10.readshare.model.MapViewModel
import com.grupo10.readshare.navigation.AppNavigation
import com.grupo10.readshare.ui.theme.ReadShareTheme
import org.osmdroid.config.Configuration
import org.osmdroid.wms.BuildConfig

class MainActivity : ComponentActivity() {

    private val mapViewModel: MapViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        setContent {
            ReadShareTheme {
                AppNavigation(mapViewModel)
            }
        }
        requestLocationPermission()
    }
     private fun requestLocationPermission() {
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                mapViewModel.fetchUserLocation()
            }
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            mapViewModel.fetchUserLocation()
        }
    }
}
class ReadShare : Application() {
    override fun onCreate() {
        super.onCreate()
        // Set the user agent to prevent getting banned from the OSM servers
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }
}
