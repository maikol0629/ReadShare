package com.grupo10.readshare.ui.theme

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.grupo10.readshare.R
import java.text.NumberFormat
import java.util.Locale

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
    var len = 30

    var visualTransformation = VisualTransformation.None
    var passwordVisible by remember { mutableStateOf(false) }
    if (type == "pass" && !passwordVisible) {
        visualTransformation = PasswordVisualTransformation()
    } else if (type=="price"){
        pri=true
    }
    if(name=="Descripcion"){
        len = 100
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

    TextField(
        value = text,
        onValueChange = {
            if (it.length <= len) {
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
            fontSize = 16.sp,
            color = colorResource(id = R.color.black)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = flag,
            imeAction = ImeAction.Default
        ),
        //placeholder = { Text(text = name, color = colorResource(id = R.color.black))},
        readOnly = false,
        colors = OutlinedTextFieldDefaults.colors(
            colorResource(id = R.color.background1), focusedBorderColor = colorResource(
            id = R.color.black
        ), focusedLabelColor = colorResource(id = R.color.black), unfocusedBorderColor = colorResource(
            id = R.color.label
        )
        ),
        textStyle = TextStyle(color = colorResource(id = R.color.black), fontSize = 18.sp, textAlign = TextAlign.Justify),
        visualTransformation = visualTransformation,
        trailingIcon = {
            if (type == "pass") {
                val image = painterResource(id = R.drawable.img_1)
                IconButton(onClick = {
                    passwordVisible = !passwordVisible
                }) {
                    Icon(painter= image, contentDescription = null)
                }
            }
        })

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
@Composable
fun ConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("Sí")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("No")
            }
        },
        title = { Text("Confirmación") },
        text = { Text("¿Estas seguro de crear el libro y asignar este punto de encuentro??") }
    )
}

@Composable
fun ProfileScreen(userImage:String, userName:String, userEmail:String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(8.dp)
    ) {
        Image(
            painter = rememberImagePainter(userImage),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(4.dp)
                .size(100.dp)
                .clip(CircleShape)
        )
        Column {
            Text(text = userName)
            Spacer(modifier = Modifier.padding(8.dp))
            if(userEmail!=null){
                Text(text = userEmail)
            }

        }
    }
}