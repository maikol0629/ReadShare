@file:Suppress("UNUSED_EXPRESSION")

package com.grupo10.readshare.ui.theme.screens

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.GoogleAuthProvider
import com.grupo10.readshare.R
import com.grupo10.readshare.navigation.AppScreens
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.storage.AuthRes
import com.grupo10.readshare.ui.theme.CampText
import com.grupo10.readshare.ui.theme.LinkText
import com.grupo10.readshare.ui.theme.showToast
import kotlinx.coroutines.launch

@Composable
fun Login(navController: NavController){
    var email by remember {
        mutableStateOf("")
    }
    var pass by remember {
        mutableStateOf("")
    }
    val current = LocalContext.current
    val auth = AuthManager(current)
    val scope = rememberCoroutineScope()
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intent = result.data
        if (result.resultCode == Activity.RESULT_OK && intent != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            when (val account = auth.handleSignInResult(task)) {
                is AuthRes.Success -> {
                    val credential = GoogleAuthProvider.getCredential(account.data.idToken, null)
                    scope.launch {
                        val fireUser = auth.signInWithGoogleCredential(credential)
                        if (fireUser is AuthRes.Success) {
                            Log.i("Tag2",fireUser.data.toString())
                            Toast.makeText(current, "Bienvenidx", Toast.LENGTH_SHORT).show()
                            navController.navigate(AppScreens.Main.route) {
                                popUpTo(AppScreens.Login.route) {
                                    inclusive = true
                                }
                            }
                        } else if (fireUser is AuthRes.Error) {
                            Toast.makeText(current, "Error: ${fireUser.errorMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is AuthRes.Error -> {
                    Toast.makeText(current, "Error: ${account.errorMessage}", Toast.LENGTH_SHORT).show()
                }

                null -> TODO()
            }
        } else {
            Toast.makeText(current, intent.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {

            Row (modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(color = colorResource(id = R.color.background2)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center){


                Image(
                    painter = painterResource(id = R.drawable.read_share),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(130.dp)
                        .padding(12.dp)
                )
            }

        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(color = colorResource(id = R.color.background2)),
            Arrangement.Center
        ) {


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colorResource(id = R.color.login),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(30.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            colorResource(id = R.color.login),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {

                    Text(text = "Ingresar con:", fontSize = 18.sp)


                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .height(50.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(onClick = { auth.signInWithGoogle(googleSignInLauncher)}) {
                            Image(
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = ""
                            )
                        }
                        Text(
                            text = "ó",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { /*TODO*/ }) {
                            Image(
                                painter = painterResource(id = R.drawable.facebook),
                                contentDescription = ""
                            )
                        }

                    }
                    CampText(type = "email", name = "Correo") {
                        email = it
                    }
                    CampText(type = "pass", name = "Contraseña") {
                        pass = it
                    }

                    Button(
                        onClick = {
                            if (email.isNotEmpty()  and pass.isNotEmpty()){

                                scope.launch  {
                                    auth.login(email,pass,navController)
                                }

                        }else{

                            showToast("Campos vacíos",current)
                        }


                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .width(200.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.background1))
                    ) {
                        Text(text = "Ingresar", fontSize = 18.sp, color = colorResource(id = R.color.white))
                    }
                    LinkText("¿Olvidaste tu contraseña?") {

                    }



                }
            }

        }
    }

}









