package com.grupo10.readshare.storage

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
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
import kotlin.coroutines.suspendCoroutine

class StorageManager(private val context: Context) {
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private val dbUser = FirebaseFirestore.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().reference.child("books")
    private val dbBooks = db.getReference("books")
    private val userId = FirebaseAuth.getInstance().currentUser
    private val  email = userId?.email

    private fun getEmail():String{
        return email.toString()
    }

    private fun getStorageReference(path:String):StorageReference{
        return storageRef.child(path).child(email?:"")
    }

    suspend fun uploadImages(book: Book, filePaths:List<Uri>
                             ): List<String> {
        val images: MutableList<String> = mutableListOf()
        var cont = 0
        filePaths.forEach {
            val fileRef = getStorageReference("books").child(email+book.title).child(cont.toString())
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
    suspend fun uploadImageFromUrl(fileUrl: String, context: Context): String {
        var image = ""
        val fileRef = email?.let { getStorageReference("users").child(it) }
Log.i("Entrada", fileUrl)
        try {
            // Descargar el archivo desde la URL en un hilo de fondo
            val tempFile = withContext(Dispatchers.IO) {
                val url = URL(fileUrl)
                val connection = url.openConnection()
                connection.connect()
                val inputStream: InputStream = connection.getInputStream()
                val tempFile = File.createTempFile("tempImage", ".jpg")
                val outputStream: OutputStream = FileOutputStream(tempFile)

                // Guardar el archivo descargado en un archivo temporal
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                inputStream.close()
                outputStream.close()
                Log.i("tempFile", tempFile.toString())
                tempFile

            }

            // Subir el archivo a Firebase Storage y obtener la URL de descarga
            Log.i("Imagen In",image)
            val fileUri = Uri.fromFile(tempFile)
            image = uploadFileAndGetDownloadUrl(fileRef, fileUri, context)

            // Borrar el archivo temporal
            tempFile.delete()
        } catch (e: Exception) {
            showToast("Error al procesar la imagen: ${e.message}", context)
        }
        Log.i("Imagen",image)
        return image
    }

    private suspend fun uploadFileAndGetDownloadUrl(fileRef: StorageReference?, fileUri: Uri, context: Context): String {
        return suspendCoroutine { continuation ->
            fileRef?.putFile(fileUri)
                ?.addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        continuation.resume(downloadUrl)
                    }.addOnFailureListener { exception ->
                        showToast("Error al obtener URL de la imagen: ${exception.message}", context)
                        continuation.resume("")
                    }
                }
                ?.addOnFailureListener { exception ->
                    showToast("Error al subir imagen: ${exception.message}", context)
                    continuation.resume("")
                }
        }
    }


    private suspend fun uploadImageFromUri(filePath: Uri): String = suspendCancellableCoroutine { continuation ->
        val fileRef = userId.let { getStorageReference("users").child(it.toString()) }
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
                book.user= userId?.uid.toString()
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
             Log.i("TAG","flow.toString()")
         }
         Log.i("Flow", flow.toString())

         return flow


    }
    suspend fun getBooksSale(): Flow<List<Book>> {
        return getBooks().map { books ->
            books.filter { book ->
                book.price.isNotEmpty()
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
                    trySend(books.filter { book -> book.user == userId?.uid.toString() }).isSuccess
                }
                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })
            awaitClose { dbRef.removeEventListener(listener) }
            Log.i("TAG","flow.toString()")
        }
        Log.i("Flow", flow.toString())
        return flow


    }





}


