package com.grupo10.readshare.storage

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.NavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.grupo10.readshare.R
import com.grupo10.readshare.model.User
import com.grupo10.readshare.navigation.AppScreens
import com.grupo10.readshare.ui.theme.showToast
import kotlinx.coroutines.tasks.await

sealed class AuthRes<out T> {
    data class Success<T>(val data: T): AuthRes<T>()
    data class Error(val errorMessage: String): AuthRes<Nothing>()
}
class AuthManager(private val context:Context) {

    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val signInClient = Identity.getSignInClient(context)


    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    fun handleSignInResult(task: Task<GoogleSignInAccount>): AuthRes<GoogleSignInAccount>? {
        return try {
            val account = task.getResult(ApiException::class.java)
            AuthRes.Success(account)
        } catch (e: ApiException) {
            AuthRes.Error(e.message ?: "Google sign-in failed.")
        }
    }
    suspend fun signInWithGoogleCredential(credential: AuthCredential): AuthRes<FirebaseUser>? {
        return try {
            val firebaseUser = auth.signInWithCredential(credential).await()
            firebaseUser.user?.let {
                firebaseUser.additionalUserInfo?.profile?.forEach{
                    Log.i("User",it.toString())
                }
                AuthRes.Success(it)
            } ?: throw Exception("Sign in with Google failed.")
        } catch (e: Exception) {
            AuthRes.Error(e.message ?: "Sign in with Google failed.")
        }
    }
    fun signInWithGoogle(googleSignInLauncher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }




    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }

    fun getCurrentUser() = auth.currentUser

    suspend fun login(email: String, pass: String, navController: NavController) {
        try {
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        navController.navigate(AppScreens.Main.route) {
                            popUpTo(AppScreens.Login.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }


                    }



                }
                .addOnFailureListener{
                    showToast("Error desconocido", context)
                }
                .await()
        }catch (e:Exception){
            showToast("Correo o contraseña incorrecta", context = this.context)
        }

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





