package com.grupo10.readshare.storage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.grupo10.readshare.R
import com.grupo10.readshare.model.User
import com.grupo10.readshare.navigation.AppScreens
import com.grupo10.readshare.ui.theme.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


sealed class AuthRes<out T> {
    data class Success<T>(val data: T): AuthRes<T>()
    data class Error(val errorMessage: String): AuthRes<Nothing>()
}
class AuthManager(private val context:Context,
    private val activity: Activity) {

    private val auth = FirebaseAuth.getInstance()
    private val storage = StorageManager(context)


    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    // Add a callbackManager instance
    internal val callbackManager = CallbackManager.Factory.create()



    fun handleFacebookLoginResult(data: Intent) {
        callbackManager.onActivityResult(Activity.RESULT_OK, Activity.RESULT_OK, data)
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
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun signInWithGoogleCredential(credential: AuthCredential): AuthRes<FirebaseUser>? {
        return try {
            val user = User()
            val firebaseUser = auth.signInWithCredential(credential).await()
            firebaseUser.user?.let {
                val profile = firebaseUser.additionalUserInfo?.profile
                if(!this.doesUserExist(profile?.get("email") as String)) {

                    user.name = (profile["given_name"] as? String).toString()
                    user.lastName = (profile["family_name"] as? String).toString()
                    user.email = (profile["email"] as? String).toString()
                    val photo = (profile["picture"] as? String).toString()
                    Log.i("photo", photo)
                    val image = storage.uploadImageFromUrl(photo, context)
                    user.image = image
                    createUser(user)
                    Log.i("User image", user.image)
                    AuthRes.Success(it)
                }else{
                    AuthRes.Success(it)
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

    private suspend fun createUser(user: User) {
        val db =FirebaseFirestore.getInstance()
        db.collection("users").document(user.email).set(
            hashMapOf(
                "name" to user.name,
                "lastName" to user.lastName,
                "image" to user.image,
            )
        ).await()

    }

    suspend private fun doesUserExist(email: String): Boolean {
        val db = FirebaseFirestore.getInstance()
        return try {
            val documentSnapshot = db.collection("users").document(email).get().await()
            documentSnapshot.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()

    }

    private fun getCurrentUser() = auth.currentUser

    suspend fun login(email: String, pass: String, navController: NavController) {
        try {

            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if(checkEmailVerification()){

                            navController.navigate(AppScreens.Main.route) {
                                popUpTo(AppScreens.Login.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }else{
                            showToast("Email no verificado",context)
                            auth.signOut()
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
                    val u = auth.currentUser
                    user.let {
                        u?.sendEmailVerification()
                            ?.addOnCompleteListener(){
                                if(it.isSuccessful){

                                    db.collection("users").document(user.email).set(
                                        hashMapOf(
                                            "name" to user.name,
                                            "lastName" to user.lastName,
                                        )
                                    )

                                    navController.navigate(AppScreens.Login.route) {
                                        popUpTo(AppScreens.Sigin.route) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                    showToast("Correo de verificación enviado a: "+user.email,context)
                                }else{
                                    showToast("Error al enviar el correo de verificación",context)
                                }
                            }
                    }




                } else {

                    showToast("Problemas para registrar", current)
                }

            }
            .await()


    }
    fun checkEmailVerification():Boolean {
        val user = getCurrentUser()
        if (user != null && user.isEmailVerified) {
            return true
        } else {
            auth.signOut()
            return false
        }
    }

    fun getUserUid(): String? {
    return auth.currentUser?.uid
    }
    suspend fun getUserData(): User? {

        val db = FirebaseFirestore.getInstance()
        return try {
            val documentSnapshot = db.collection("users").document(getCurrentUser()?.email.toString()).get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(User::class.java)!!
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



}





