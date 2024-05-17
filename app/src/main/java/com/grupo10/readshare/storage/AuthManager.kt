package com.grupo10.readshare.storage

import android.content.Context
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.grupo10.readshare.model.User
import com.grupo10.readshare.navigation.AppScreens
import com.grupo10.readshare.ui.theme.screens.showToast
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()

    suspend fun login(email: String, pass: String, navController: NavController) {

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    navController.navigate(AppScreens.Main.route) {
                        popUpTo(AppScreens.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }


                } else {
                    showToast("Correo o contraseña incorrecta", context = this.context)
                }

            }
            .await()
    }


    suspend fun siginWithEmail(
        user: User, navController: NavController,
        current: Context
    ) {
        val db = FirebaseFirestore.getInstance()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.email, user.pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    db.collection("users").document(user.email).set(
                        hashMapOf(
                            "name" to user.name,
                            "address" to user.address,
                            "phone" to user.phone
                        )
                    )

                    navController.navigate(AppScreens.Main.route) {
                        popUpTo(AppScreens.Sigin.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }


                } else {

                    showToast("Problemas para registrar", current)
                }

            }
            .await()


    }
}