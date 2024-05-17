package com.grupo10.readshare.ui.theme.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grupo10.readshare.R
import com.grupo10.readshare.navigation.AppScreens
import com.google.firebase.auth.FirebaseAuth
import com.grupo10.readshare.storage.AuthManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

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
                        IconButton(onClick = { /*TODO*/ }) {
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
                        }},
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




@Composable
fun CampText(
    type: String,
    name: String,
    endText: (String) -> Unit
) {
    val current = LocalContext.current

    var text by remember {
        mutableStateOf("")
    }

    var flag by remember {
        mutableStateOf(KeyboardType.Text)
    }

    var pri by remember {
        mutableStateOf(false)
    }

    var visualTransformation = VisualTransformation.None

    if (type == "pass") {
      visualTransformation = PasswordVisualTransformation()
    } else if (type=="price"){
        pri=true

    }

    when (type) {
        "pass" -> {
            flag = KeyboardType.Password
        }
        "email" -> {
            flag = KeyboardType.Email
        }
        "phone" -> {
            flag = KeyboardType.Phone
        }
        "price" -> {
            flag = KeyboardType.Number
        }
            }

    OutlinedTextField(
        value = text,
        onValueChange = {
            if (it.length <= 30) {
                if(pri){

                    text = formatCurrency(text)
                    endText(text)

                }

                text = it
                endText(text)
            } else {
                showToast("Demasiados caracteres", current)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(70.dp),
        label = { Text(text = name,
            fontSize = 14.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = flag,
            imeAction = ImeAction.Default
        ),
        readOnly = false,
        colors = OutlinedTextFieldDefaults.colors(colorResource(id =R.color.background1), focusedBorderColor = colorResource(
            id = R.color.black
        ), focusedLabelColor = colorResource(id = R.color.black), unfocusedBorderColor = colorResource(
            id = R.color.label
        )
        ),
        textStyle = TextStyle(color = colorResource(id = R.color.black), fontSize = 14.sp),
        visualTransformation = visualTransformation )
}
fun showToast(message: String, context: Context) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
fun formatCurrency(input: String):  String{
    val unformattedString = input.replace(",", "").trim()
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    val formattedString = try {
        val parsedNumber = unformattedString.toDouble()
        numberFormat.format(parsedNumber)
    } catch (e: NumberFormatException) {
        ""
    }

    val resultString = if (formattedString.isNotEmpty()) {
        "$formattedString $"
    } else {
        ""
    }

    return resultString
}