package com.grupo10.readshare.navigation

sealed class AppScreens(val route: String) {
    object Charge : AppScreens("Charge")
    object Welcome : AppScreens("Welcome")
    object Login : AppScreens("Login")
    object Sigin : AppScreens("Sigin")
    object Main : AppScreens("Main")

    object Upload : AppScreens("UploadBook")
    object Account : AppScreens("Account")
    object Map:AppScreens("MapView")
}