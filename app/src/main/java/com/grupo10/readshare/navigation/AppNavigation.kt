package com.grupo10.readshare.navigation

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.grupo10.readshare.ui.theme.screens.Charge
import com.grupo10.readshare.ui.theme.screens.Login
import com.grupo10.readshare.ui.theme.screens.Main
import com.grupo10.readshare.ui.theme.screens.Sigin
import com.grupo10.readshare.ui.theme.screens.UploadBook
import com.grupo10.readshare.ui.theme.screens.Welcome
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val current = LocalContext.current
    val isConnected = remember { mutableStateOf(false) }
    val flagBook = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        delay(1500)
        if (currentUser != null) {
            // Si hay un usuario autenticado, navegar directamente a la pantalla principal
            currentUser.email?.let { viewModel.setUserEmail(it) }
            navController.navigate(AppScreens.Main.route) {
                popUpTo(AppScreens.Charge.route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } else {
            // Si no hay usuario autenticado, navegar a la pantalla de carga
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.Charge.route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = AppScreens.Charge.route) {
        composable(route = AppScreens.Charge.route) {
            Charge()
        }
        composable(route = AppScreens.Welcome.route) {
            Welcome(navController)
        }
        composable(route = AppScreens.Login.route) {
            Login(navController)
        }
        composable(route = AppScreens.Sigin.route) {
            Sigin(navController) { email ->
                viewModel.setUserEmail(email)
            }
        }
        composable(route = AppScreens.Main.route) {
            val userEmail = viewModel.userEmail.value
            Main(userEmail, navController){
                flagBook.value=it
            }
        }
        composable(route = AppScreens.Upload.route) {
            UploadBook(navController,flagBook.value)
        }
    }
}

class MainViewModel : ViewModel() {
    val userEmail = mutableStateOf("")

    fun setUserEmail(email: String) {
        userEmail.value = email
    }
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}
