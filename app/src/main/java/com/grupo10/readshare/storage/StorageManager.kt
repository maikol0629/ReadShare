package com.grupo10.readshare.storage

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.model.User
import com.grupo10.readshare.ui.theme.showToast
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class StorageManager(private val context: Context, private val authManager: AuthManager) {
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private val dbUser = FirebaseFirestore.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().reference.child("books")
    private val dbBooks = db.getReference("books")


    private fun getStorageReference(path:String):StorageReference{
        return storageRef.child(path)
    }

    suspend fun uploadImages(book: Book, filePaths:List<Uri>
                             ): List<String> {
        val images: MutableList<String> = mutableListOf()
        var cont = 0
        filePaths.forEach {
            val fileRef = getStorageReference("books").child(authManager.getUserEmail()+book.title).child(cont.toString())
        val uploadTask = fileRef.putFile(it).addOnSuccessListener { taskSnapshot ->
            // Éxito al subir la imagen
            // Puedes obtener la URL de descarga de la imagen subida si la necesitas
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                // Aquí puedes obtener la URI de descarga de la imagen subida
                val downloadUrl = uri.toString()
                images.add(downloadUrl)
            }.addOnFailureListener { exception ->
                showToast("Error al obtener utl Imagenes", context = this.context)
            }
        }
            .addOnFailureListener { exception ->
                showToast("Error al subir Imagenes", context = this.context)
            }
        uploadTask.await()
            cont+=1
    }
        return images

    }

    suspend fun uploadImageFromUrl(fileUrl: String, email: String, context: Context): String {
        var image = ""
        return withContext(Dispatchers.IO) {
            try {
                if (email.isNotEmpty()) { // Verificar que el email no esté vacío
                    val tempFile = downloadFileFromUrl(fileUrl)
                    val fileUri = Uri.fromFile(tempFile)
                    Log.i("fileUri", fileUri.toString())

                    val fileRef = getStorageReference("users").child(email)
                    Log.e("fileRef", fileRef.toString())

                    fileRef.putFile(fileUri).await()
                    image = fileRef.downloadUrl.await().toString()
                    tempFile.delete()
                } else {
                    throw IllegalArgumentException("Email cannot be empty")
                }
            } catch (e: Exception) {
                Log.e("uploadImageFromUrl", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            Log.e("image", image)
            image
        }
    }

    private suspend fun downloadFileFromUrl(fileUrl: String): File {
        return withContext(Dispatchers.IO) {
            val url = URL(fileUrl)
            val connection = url.openConnection()
            connection.connect()

            val inputStream: InputStream = connection.getInputStream()
            val tempFile = File.createTempFile("tempImage", ".jpg")
            val outputStream: OutputStream = FileOutputStream(tempFile)

            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            inputStream.close()
            outputStream.close()

            Log.i("tempFile", tempFile.absolutePath)
            tempFile
        }
    }

    private suspend fun uploadFileAndGetDownloadUrl(fileRef: StorageReference, fileUri: Uri, context: Context): String {
        return withContext(Dispatchers.IO) {
            try {
                fileRef.putFile(fileUri).await()
                val downloadUrl = fileRef.downloadUrl.await().toString()
                downloadUrl
            } catch (e: Exception) {
                Log.e("uploadFile", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    showToast("Failed to upload file: ${e.message}", context)
                }
                ""
            }
        }

    }


    private suspend fun uploadImageFromUri(filePath: Uri): String = suspendCancellableCoroutine { continuation ->
        val fileRef = authManager.getUserUid().let { getStorageReference("users").child(it.toString()) }
        val uploadTask = fileRef.putFile(filePath)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                continuation.resume(downloadUrl)
                showToast("Imagen subida actualizada correctamente", context = this.context)
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)

            }
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)

        }

        continuation.invokeOnCancellation {
            uploadTask.cancel()
        }
    }
    private fun deleteImage(imageUrl: String): Deferred<Unit> {
        val deferred = CompletableDeferred<Unit>()
        try {
            if (imageUrl.isEmpty()) {
                deferred.complete(Unit)
                return deferred
            }
            val storageRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            if (storageRef.path.isNotEmpty()) {
                storageRef.getMetadata().addOnSuccessListener {
                    // La imagen existe, procede a eliminarla
                    val deleteTask = storageRef.delete()
                    deleteTask.addOnSuccessListener {
                        deferred.complete(Unit)
                    }.addOnFailureListener { exception ->
                        deferred.completeExceptionally(exception)
                    }
                }.addOnFailureListener { exception ->
                    if (exception is StorageException && exception.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        // La imagen no existe
                        deferred.complete(Unit)
                    } else {
                        // Otro error ocurrió al intentar obtener los metadatos
                        deferred.completeExceptionally(exception)
                    }
                }
            } else {
                deferred.complete(Unit)
            }
        } catch (e: Exception) {
            Log.e("Error borrando imagen", e.toString())
            deferred.completeExceptionally(e)
        }
        return deferred
    }


    @SuppressLint("SuspiciousIndentation")
    suspend fun updateUserDetails(user: User) {
        val userRef = dbUser.collection("users").document(user.id)
            userRef.set(user).await()
    }
    suspend fun updateProfile(uri: Uri, user: User) {

        CoroutineScope(Dispatchers.IO).launch {
            deleteImage(user.image).await()
            val newImageUri = uploadImageFromUri(uri)
            user.image = newImageUri
            updateUserDetails(user)
        }

    }

    suspend fun updateBook(book: Book) {
        dbBooks.child(book.id).setValue(book).await()
    }

    suspend fun deleteBook(book: Book) {
        try {
            // Borrar las imágenes asociadas al libro
            deleteBookImages(book.images)
            // Borrar el libro de la base de datos
            dbBooks.child(book.id).removeValue().await()
            Log.d("Firebase", "Libro eliminado con éxito")
        } catch (e: Exception) {
            Log.e("Error al eliminar libro", e.toString())
        }
    }

    private suspend fun deleteBookImages(imageUrls: List<String>) {
        imageUrls.forEach { imageUrl ->
            try {
                deleteImage(imageUrl).await()
                Log.d("Firebase", "Imagen eliminada con éxito: $imageUrl")
            } catch (e: Exception) {
                Log.e("Error al eliminar imagen", e.toString())
            }
        }
    }


    suspend fun addBook(book: Book,){
        try {
            val key = dbBooks.push().key
            if (key!=null){

                book.user= authManager.getUserUid().toString()
                book.id = key
                book.uris = emptyList()
                dbBooks.child(key).setValue(book)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            Log.d("Firebase", "Datos guardados con éxito")
                        }
                    }.await()
            }
        }catch (e:Exception){
            Log.e("No agregó", e.toString())
        }


    }


    suspend fun getBookById(bookId: String): Book? {
        return try {
            val snapshot = dbBooks.child(bookId).get().await()
            if (snapshot.exists()) {
                snapshot.getValue(Book::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("Error al obtener libro", e.toString())
            null
        }
    }

     @SuppressLint("SuspiciousIndentation")
      suspend fun getBooks(): Flow<List<Book>> {
         val flow = callbackFlow {
             val listener = dbRef.addValueEventListener(object : ValueEventListener {
                 override fun onDataChange(snapshot: DataSnapshot) {
                     val books = snapshot.children.mapNotNull {  snapshot ->
                         val book = snapshot.getValue(Book::class.java)
                         Log.i("TAG",book.toString())
                         snapshot.key?.let { book?.copy() }
                     }
                     trySend(books).isSuccess
                 }
                 override fun onCancelled(error: DatabaseError) {
                     close(error.toException())
                 }
             })
             awaitClose { dbRef.removeEventListener(listener) }
         }
         return flow


    }
    suspend fun getBooksCategory(category: String): Flow<List<Book>> {
        return getBooks().map { books ->
            books.filter { book ->
                book.genero == category
            }
        }
    }
    suspend fun getBooksExchange(): Flow<List<Book>> {
        return getBooks().map { books ->
            books.filter { book ->
                book.price.isEmpty()
            }
        }
    }

    suspend fun getBooksUser(): Flow<List<Book>> {
        val flow = callbackFlow {
            val listener = dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val books = snapshot.children.mapNotNull {  snapshot ->
                        val book = snapshot.getValue(Book::class.java)
                        Log.i("TAG",book.toString())
                        snapshot.key?.let { book?.copy() }
                    }
                    trySend(books.filter { book -> book.user == authManager.getUserUid() }).isSuccess
                }
                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })
            awaitClose { dbRef.removeEventListener(listener) }
        }
        return flow


    }

    @SuppressLint("SuspiciousIndentation")
    suspend fun searchBooksByTitle(query: String): Flow<List<Book>> {
        return callbackFlow {
            val listener = dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val books = snapshot.children.mapNotNull { snapshot ->
                        val book = snapshot.getValue(Book::class.java)
                        snapshot.key?.let { book?.copy() }
                    }.filter { book ->
                        book?.title?.contains(query, ignoreCase = true) == true
                    }
                    trySend(books).isSuccess
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })
            awaitClose { dbRef.removeEventListener(listener) }
        }
    }




}


