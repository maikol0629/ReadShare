package com.grupo10.readshare.ui.theme

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        colors = OutlinedTextFieldDefaults.colors(
            colorResource(id = R.color.background1), focusedBorderColor = colorResource(
            id = R.color.black
        ), focusedLabelColor = colorResource(id = R.color.black), unfocusedBorderColor = colorResource(
            id = R.color.label
        )
        ),
        textStyle = TextStyle(color = colorResource(id = R.color.black), fontSize = 14.sp),
        visualTransformation = visualTransformation )
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