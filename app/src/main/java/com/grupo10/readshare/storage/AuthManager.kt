package com.grupo10.readshare.storage

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.NavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.grupo10.readshare.R
import com.grupo10.readshare.model.User
import com.grupo10.readshare.navigation.AppScreens
import com.grupo10.readshare.ui.theme.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


sealed class AuthRes<out T> {
    data class Success<T>(val data: T): AuthRes<T>()
    data class Error(val errorMessage: String): AuthRes<Nothing>()
}
class AuthManager(
    private val context: Context,
    private val activity: Activity
) {

    private val auth = FirebaseAuth.getInstance()
    private val storage = StorageManager(context, this)
    private val db = FirebaseFirestore.getInstance()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    internal val callbackManager = CallbackManager.Factory.create()

    fun getUserUid(): String? {
        return auth.currentUser?.uid
    }
    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    internal fun getCurrentUser(): FirebaseUser? {
        auth.currentUser?.reload()
        return auth.currentUser
    }

    fun signInWithFacebook() {
        val loginManager = LoginManager.getInstance()
        loginManager.logInWithReadPermissions(activity, listOf("email", "public_profile"))
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val token = result.accessToken
                val credential = FacebookAuthProvider.getCredential(token.token)
                CoroutineScope(Dispatchers.IO).launch {
                    signInWithFacebookCredential(credential)
                }
            }

            override fun onCancel() {
                showToast("Login cancelled", context)
            }

            override fun onError(error: FacebookException) {
                showToast("Error during Facebook login: ${error.message}", context)
            }
        })
    }

    private suspend fun signInWithFacebookCredential(credential: AuthCredential): AuthRes<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Sign in with Facebook failed.")
            AuthRes.Success(user)
        } catch (e: Exception) {
            AuthRes.Error(e.message ?: "Sign in with Facebook failed.")
        }
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>): AuthRes<GoogleSignInAccount>? {
        return try {
            val account = task.getResult(ApiException::class.java)
            AuthRes.Success(account)
        } catch (e: ApiException) {
            AuthRes.Error(e.message ?: "Google sign-in failed.")
        }
    }

    suspend fun signInWithGoogleCredential(credential: AuthCredential): AuthRes<FirebaseUser> {
        return try {
            val userResult = auth.signInWithCredential(credential).await()
            val firebaseUser = userResult.user ?: throw Exception("Sign in with Google failed.")
            val profile = userResult.additionalUserInfo?.profile

            val documentSnapshot = db.collection("users").document(firebaseUser.uid).get().await()

            if (!documentSnapshot.exists()) {
                val imageUrl = profile?.get("picture") as? String ?: ""
                val email = (profile?.get("email") as? String).orEmpty()
                val uploadedImageUrl = storage.uploadImageFromUrl(imageUrl,email, context)
                delay(1000)// Asegúrate de que la imagen se sube antes de crear el usuario
                val user = User(
                    id = firebaseUser.uid,
                    name = (profile?.get("given_name") as? String).orEmpty(),
                    lastName = (profile?.get("family_name") as? String).orEmpty(),
                    email = (profile?.get("email") as? String).orEmpty(),
                    image = uploadedImageUrl
                )
                if (createUser(user)) {
                    AuthRes.Success(firebaseUser)
                } else {
                    AuthRes.Error("Error al crear el usuario")
                }
            } else {
                AuthRes.Success(firebaseUser) // Usuario ya existe
            }
        } catch (e: Exception) {
            AuthRes.Error(e.message ?: "Sign in with Google failed.")
        }
    }

    fun signInWithGoogle(googleSignInLauncher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }


    private suspend fun createUser(user: User): Boolean {
        return try {
            db.collection("users").document(user.id).set(user).await()
            true
        } catch (e: Exception) {
            showToast( "Error: ${e.message}",context)
            false
        }
    }

    suspend fun login(email: String, pass: String, navController: NavController) {
        try {
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (checkEmailVerification()) {
                            navController.navigate(AppScreens.Main.route) {
                                popUpTo(AppScreens.Login.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        } else {
                            showToast("Email no verificado", context)
                            auth.signOut()
                        }
                    }
                }
                .addOnFailureListener {
                    showToast("Error desconocido", context)
                }
                .await()
        } catch (e: Exception) {
            showToast("Correo o contraseña incorrecta", context)
        }
    }

    suspend fun signInWithEmailAndPass(
        user: User, navController: NavController,
        current: Context
    ) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, user.pass).await()
            val u = authResult.user
            if (u != null) {
                u.sendEmailVerification().await()
                user.id = u.uid
                db.collection("users").document(user.id).set(user).await()
                navController.navigate(AppScreens.Login.route) {
                    popUpTo(AppScreens.Sigin.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
                showToast("Correo de verificación enviado a: ${user.email}", current)
            } else {
                showToast("Error al enviar el correo de verificación", current)
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            showToast("El correo electrónico ya está en uso", current)
        } catch (e: Exception) {
            showToast("Problemas para registrar: ${e.message}", current)
        }
    }

    fun checkEmailVerification(): Boolean {
        val user = getCurrentUser()
        return if (user != null && user.isEmailVerified) {
            true
        } else {
            auth.signOut()
            false
        }
    }
    suspend fun resetPassword(email: String): AuthRes<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthRes.Success(Unit)
        } catch(e: Exception) {
            AuthRes.Error(e.message ?: "Error al restablecer la contraseña")
        }
    }

    suspend fun getUserData(): User? {
        return try {
            val documentSnapshot = db.collection("users").document(getUserUid().toString()).get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getUserDataByID(uid: String): User? {
        return try {
            val documentSnapshot = db.collection("users").document(uid).get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteUser(navController: NavController) {
        try {
            val chatManager = ChatManager()
            val user = this.getUserDataByID(this.getUserUid().toString())
            val books = user?.books as MutableList<String>
            if (books.isNotEmpty()) {
                books.forEach {
                    storage.getBookById(it)?.let { it1 -> storage.deleteBook(it1) }
                }
            }
            storage.deleteImage(user.image).await()
            chatManager.deleteAllUserChats(user.id)
            db.collection("users").document(getUserUid().toString()).delete().await()
            getCurrentUser()?.delete()?.await()
            signOut()
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
            showToast("Cuenta eliminada exitosamente", context)
        } catch (e: Exception) {
            showToast("Error al eliminar la cuenta: ${e.message}", context)
        }
    }


    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }

}







