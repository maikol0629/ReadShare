package com.grupo10.readshare.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.verticalScroll
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
import com.grupo10.readshare.R
import com.grupo10.readshare.model.User
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.ui.theme.CampText
import com.grupo10.readshare.ui.theme.showToast
import kotlinx.coroutines.launch

@Composable
fun Sigin(navController: NavController,
          auth: AuthManager,
          endId: (String) -> Unit){

    val user = User()
    val current = LocalContext.current
    val scope = rememberCoroutineScope()


    var cPass by remember {
        mutableStateOf("")
    }
    Scaffold(
        modifier = Modifier

            .fillMaxSize()
            .padding(2.dp),
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
        }) {
            innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(color = colorResource(id = R.color.background2))
                .verticalScroll(ScrollState(1), enabled = true)
               ,
            Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colorResource(id = R.color.background2),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.TopCenter
            ) {


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            colorResource(id = R.color.login),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(12.dp)
                        ,
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {

                    Text(text = "Regístrese con:", fontSize = 18.sp)


                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .height(50.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {



                        IconButton(onClick = { }) {
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
                    CampText(type = "", name = "Nombres") {
                        user.name = it
                    }
                    CampText(type = "", name = "Apellidos") {
                        user.lastName = it
                    }
                    CampText(type = "email", name = "Correo") {
                        user.email = it
                    }
                    CampText(type = "pass", name = "Contraseña") {
                        user.pass = it
                    }
                    CampText(type = "pass", name = "Confirmar Contraseña") {
                        cPass = it
                    }

                    Row(modifier = Modifier.padding(10.dp)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Button(
                                onClick = {

                                    if (UserisNotBlank(user)){
                                        if((user.pass.length>6)){
                                            if(user.pass == cPass) {

                                                scope.launch {
                                                    auth.signInWithEmailAndPass(user,navController,current)
                                                    auth.getUserUid()?.let { endId(it) }
                                                }

                                            }else{
                                                showToast("Las contraseñas no son iguales", context = current)
                                            }

                                        }else{
                                                showToast("La Contraseña debe tener mínimo 6 caracteres", current)
                                        }
                                    }else{

                                        showToast("Campos vacíos",current)
                                    }
                                },
                                modifier = Modifier
                                    .padding(10.dp)
                                    .width(200.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(
                                        id = R.color.background1
                                    )
                                )
                            ) {
                                Text(text = "Enviar", fontSize = 18.sp)
                            }

                        }


                    }
                }
            }
        }
        }
    }
fun UserisNotBlank(user: User): Boolean {
    return user.name.isNotBlank() && user.email.isNotBlank() && user.pass.isNotBlank() && user.lastName.isNotBlank()
}