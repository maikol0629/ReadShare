package com.grupo10.readshare.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grupo10.readshare.R
import com.grupo10.readshare.navigation.AppScreens

@Composable
fun Welcome(navController: NavController){
    Column(modifier = Modifier
        .background(colorResource(id = R.color.background2))
        .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally){

        /*Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
            horizontalArrangement = Arrangement.Center){
            Text(text= stringResource(id = R.string.app_name),
                fontSize = 30.sp,
                color = colorResource(id = R.color.black)
            )
        }*/
        Image(
            painter = painterResource(id = R.drawable.read_share),
            contentDescription = "Read Share",
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
                .alpha(0.8f)
        )
        Column(verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally){


        Button(onClick = { navController.navigate(AppScreens.Login.route)},
            modifier = Modifier
                .padding(20.dp)
                .width(230.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.background1))
                ) {
            Text(text = "Ingresar",
                fontSize = 20.sp,
                color = colorResource(id = R.color.white))

        }
        //Spacer(modifier = Modifier.height(20.dp))
    Row(modifier= Modifier.padding(horizontal = 10.dp)
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center) {
        Text(text = "¿No tienes una cuenta?",
            fontSize = 15.sp,
            color = colorResource(id = R.color.black))
        Spacer(modifier = Modifier.width(8.dp))
        LinkText (txt = "Registrate"){
navController.navigate(AppScreens.Sigin.route)
        }
    }


    }
    }
}
@Composable
fun LinkText( txt:String,
    onClick: () -> Unit) {
    val text = AnnotatedString.Builder()
        .apply {
            append(txt)
            addStyle(
                style = SpanStyle(
                    color = Color.White,
                    textDecoration = TextDecoration.Underline,
                    fontSize = 16.sp,
                ),
                start = 0,
                end = length
            )
            addStringAnnotation("LINK", "more_info", 0, length)
        }
        .toAnnotatedString()

    ClickableText(
        text = text,
        onClick = { offset ->
            text.getStringAnnotations("LINK", offset, offset)
                .firstOrNull()?.let {
                    onClick()
                }
        }
    )
}

