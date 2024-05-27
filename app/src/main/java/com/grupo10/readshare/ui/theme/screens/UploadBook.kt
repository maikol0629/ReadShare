package com.grupo10.readshare.ui.theme.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.grupo10.readshare.R
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.navigation.AppScreens
import com.grupo10.readshare.storage.StorageManager
import com.grupo10.readshare.ui.theme.CampText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun UploadBook(
    navController: NavController,
    sale:Boolean,
    onBook: (Book) -> Unit
){

    var selectedImageBitmaps by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val book = Book()
    val context = LocalContext.current
    val storage = StorageManager(context)
    var title by remember {
        mutableStateOf("")
    }
    var genero by remember {
        mutableStateOf("Género")
    }
    var description by remember {
        mutableStateOf("")
    }
    var address by remember {
        mutableStateOf("")
    }
    var add by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()

    var price by remember {
        mutableStateOf("")
    }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    Column (modifier = Modifier
        .fillMaxSize()
        .background(
            colorResource(id = R.color.background2)

        )
        .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,) {

        Text(text = "Compartir libro", fontSize = 24.sp, fontFamily = FontFamily.SansSerif, color = colorResource(
            id = R.color.black
        ))
        Spacer(modifier = Modifier.height(28.dp))
        SelectedImagesPreview(selectedImageUris = selectedImageBitmaps)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    colorResource(id = R.color.login),
                    shape = RoundedCornerShape(50.dp)
                )
                .padding(10.dp),
            contentAlignment = Alignment.TopCenter
        ){
            Column (verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(12.dp)
                    .background(color = colorResource(id = R.color.login))){


                CampText(type = "txt", name = "Título") {
                    title = it
                }
                GeneroLiteraturaDropdown(
                    generos = stringArrayResource(id = R.array.generos),
                    onGeneroSelected = { genero = it }
                )
                CampText(type = "txt", name = "Descripcion") {
                    description = it
                }
                if (sale) {
                    CampText(type = "price", name = "Precio") {
                        price = it
                    }
                }

               /* Row(horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = address, onValueChange = {}, readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(70.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            colorResource(id = R.color.background1),
                            focusedBorderColor = colorResource(
                                id = R.color.black
                            ),
                            focusedLabelColor = colorResource(id = R.color.black),
                            unfocusedBorderColor = colorResource(
                                id = R.color.label
                            ),

                            ),
                        textStyle = TextStyle(
                            color = colorResource(id = R.color.black),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Justify
                        ),
                        placeholder =  {
                            Text(
                                text = "Punto de encuentro",
                                fontSize = 16.sp,
                                color = colorResource(id = R.color.black)
                            )
                        }
                    )
                    IconButton(onClick = { add=true }) {
                        Icon(painter = painterResource(id = R.drawable.map), contentDescription = "Punto de encuentro")
                    }
                } */




                GalleryButton { uris ->
                    selectedImages = uris
                    selectedImageBitmaps= uris
                }

                if (selectedImages.isNotEmpty() and title.isNotEmpty() and description.isNotEmpty() and genero.isNotEmpty()) {

                    Button(
                        onClick = {
                            book.title = title
                            book.description = description
                            book.genero = genero
                            book.precio = price
                            scope.launch { book.images=storage.uploadImages(book,selectedImages)
                                onBook(book)
                                delay(1000)
                                navController.navigate(AppScreens.Map.route)
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
                        Text(text = "Continuar", fontSize = 18.sp)
                    }
                }

            }
        }
    }






    }



@Composable
fun GalleryButton(onImagesSelected: (imageBitmaps: List<Uri>) -> Unit) {

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris ->
        onImagesSelected(uris)
    }

    IconButton(
        onClick = { launcher.launch("image/*") },
        modifier = Modifier
            .padding(16.dp)
            .size(50.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.add_mage),
            contentDescription = "Agregar imágenes",
            tint = LocalContentColor.current
        )
    }
}

@Composable
fun SelectedImagesPreview(selectedImageUris: List<Uri>) {
    LazyRow {
        items(selectedImageUris) { uri ->
            Image(
                painter = rememberImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
                    .clip(shape = RoundedCornerShape(8.dp))
            )
        }
    }
}






@Composable
fun GeneroLiteraturaDropdown(
    generos: Array<String>,
    onGeneroSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var itemSelected by remember {
        mutableStateOf("")
    }

    Column {

       Row (horizontalArrangement = Arrangement.SpaceAround,
           verticalAlignment = Alignment.CenterVertically){



            TextField(
                value = itemSelected, onValueChange = {}, readOnly = true,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    colorResource(id = R.color.background1),
                    focusedBorderColor = colorResource(
                        id = R.color.black
                    ),
                    focusedLabelColor = colorResource(id = R.color.black),
                    unfocusedBorderColor = colorResource(
                        id = R.color.label
                    ),

                    ),
                label = {
                    Text(
                        text = "Género",
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.black)
                    )
                },
                textStyle = TextStyle(color = colorResource(id = R.color.black), fontSize = 18.sp, textAlign = TextAlign.Justify),
            )
            IconButton(onClick = { expanded = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.deploy),
                    contentDescription = "Generos"
                )


        }
       }
        if(expanded) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                generos.forEach { genero ->
                    DropdownMenuItem(
                        onClick = {
                            onGeneroSelected(genero)
                            itemSelected = genero
                            expanded = false
                        }, text = { Text(text = genero, fontSize = 14.sp) })


                }
            }
        }
    }

}

