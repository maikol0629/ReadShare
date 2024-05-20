package com.grupo10.readshare.storage

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.grupo10.readshare.R
import com.grupo10.readshare.model.User
import com.grupo10.readshare.navigation.AppScreens
import com.grupo10.readshare.ui.theme.screens.showToast
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context, private val launcher: ActivityResultLauncher<Intent>) {

    private val auth = FirebaseAuth.getInstance()


    private lateinit var googleSignInClient: GoogleSignInClient

    fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

     fun handleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            // Manejar error
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast("Ha iniciado sesion", context)
            } else {
                showToast("Hubo un error", context)
            }
        }
    }
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





