package com.grupo10.readshare

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.grupo10.readshare.model.MapViewModel
import com.grupo10.readshare.navigation.AppNavigation
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.storage.StorageManager
import com.grupo10.readshare.ui.theme.ReadShareTheme
import org.osmdroid.config.Configuration
import org.osmdroid.wms.BuildConfig

class MainActivity : ComponentActivity() {

    private val mapViewModel: MapViewModel by viewModels()
    private lateinit var authManager: AuthManager
    private lateinit var storageManager: StorageManager
    private lateinit var facebookLoginLauncher: ActivityResultLauncher<Intent>
    private lateinit var callbackManager: CallbackManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackManager = CallbackManager.Factory.create()
        Configuration.getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))
        authManager = AuthManager(this,this@MainActivity)
        storageManager = StorageManager(this)
        // Registrar el lanzador para la actividad de inicio de sesión de Facebook
        setContent {
            ReadShareTheme {
                AppNavigation(mapViewModel,authManager, storageManager)
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
        FacebookSdk.sdkInitialize(applicationContext)
        FacebookSdk.setIsDebugEnabled(true)
        FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        // Set the user agent to prevent getting banned from the OSM servers
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance() // O SafetyNetAppCheckProviderFactory.getInstance()
        )
    }
}
