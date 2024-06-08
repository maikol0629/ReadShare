package com.grupo10.readshare.ui.theme.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.grupo10.readshare.R
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.model.User
import com.grupo10.readshare.navigation.AppScreens
import com.grupo10.readshare.storage.AuthManager
import com.grupo10.readshare.storage.StorageManager
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Account(user: User, authManager: AuthManager, navController: NavController, storage: StorageManager) {
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var name by remember { mutableStateOf(user.name) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var profileImage by remember { mutableStateOf(user.image) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()

    var flag by remember { mutableStateOf(false) }
    var updateComplete by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSignInDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        newImageUri = uri
        flag = true
    }

    Scaffold(
        containerColor = colorResource(id = R.color.background2),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = colorResource(id = R.color.background2)),
                title = { Text("User Profile") },
                actions = {
                    IconButton(onClick = { showSignInDialog = true }) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Black)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Account", tint = Color.Red)
                    }
                }
            )
        },
        contentColor = colorResource(id = R.color.background2),
        floatingActionButton = {
            if (flag) {
                FloatingActionButton(onClick = {
                    user.name = name
                    user.lastName = lastName
                    scope.launch {
                        if (newImageUri != null) {
                            storage.updateProfile(newImageUri!!, user)
                        } else {
                            storage.updateUserDetails(user)
                        }
                        // Marca que la actualización está completa
                        updateComplete = true
                        flag = false // Resetear el flag después de guardar
                    }
                }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "")
                }
            }
        },
        bottomBar = {
            BottomAppBar( containerColor = colorResource(id = R.color.background2)) {
                Button(onClick = { navController.navigate("conversations")}) {
                    Text("Chats")
                }
            }
        }
    ) { padding ->
        if (updateComplete) {
            // Resetear updateComplete para evitar múltiples recargas
            updateComplete = false
            // Volver a cargar los datos del usuario y libros
            LaunchedEffect(Unit) {
                launch {
                    storage.getBooksUser().collect {
                        books = it
                    }
                }
                profileImage = user.image // Asegurarse de que la imagen de perfil se actualice
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(color = colorResource(id = R.color.background2))
        ) {
            LaunchedEffect(Unit) {
                launch {
                    storage.getBooksUser().collect {
                        books = it
                    }
                }
            }
            ProfileHeader(
                profileImage = profileImage,
                onImageClick = { showImageDialog = true },
                onEditImageClick = { imagePickerLauncher.launch("image/*") },
                name = name,
                onNameChange = { name = it; flag = true },
                lastName = lastName,
                onLastNameChange = { lastName = it; flag = true }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (books.isEmpty()) {
                Text(text = "No hay libros para mostrar")
            } else {
                RowWithCards(books, "My Books", navController)
            }
        }
    }

    if (showImageDialog) {
        Dialog(onDismissRequest = { showImageDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000)) // Fondo transparente oscuro
                    .clickable { showImageDialog = false }, // Cerrar el diálogo al hacer clic en el fondo
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Image(
                    painter = rememberImagePainter(profileImage),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        authManager.deleteUser(navController) // Llama a la función de suspensión para eliminar la cuenta
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    showDeleteDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showSignInDialog) {
        AlertDialog(
            onDismissRequest = { showSignInDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    authManager.signOut()
                    navController.navigate(AppScreens.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                    showSignInDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignInDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


}



@Composable
fun ProfileHeader(
    profileImage: String,
    onImageClick: () -> Unit,
    onEditImageClick: () -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit
) {
    var painter = painterResource(id = R.drawable.profile)
    if (profileImage.isNotEmpty()) {
        painter = rememberImagePainter(profileImage)
    }
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = androidx.compose.ui.Alignment.BottomEnd,
            modifier = Modifier.size(100.dp)
        ) {
            Image(
                painter = painter,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { onImageClick() },
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onEditImageClick,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(4.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Change Image", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            label = { Text("Last Name") }
        )
    }
}
