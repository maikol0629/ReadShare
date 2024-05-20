package com.grupo10.readshare.storage

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.grupo10.readshare.model.Book
import com.grupo10.readshare.ui.theme.screens.showToast
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StorageManager(private val context: Context) {
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private val db = FirebaseDatabase.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().reference.child("books")
    private val dbBooks = db.getReference("books")
    private val userId = FirebaseAuth.getInstance().currentUser
    private val  email = userId?.email

    fun getEmail():String{
        return email.toString()
    }

    private fun getStorageReference():StorageReference{
        return storageRef.child("books").child(email?:"")
    }

    private suspend fun uploadimages(book: Book, filePaths:List<Uri>
                             ): List<String> {

        book.user=userId.toString()
        val images: MutableList<String> = mutableListOf()
        var cont = 0
        filePaths.forEach {
            val fileRef = getStorageReference().child(email+book.title).child(cont.toString())
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

    private suspend fun addBook(book: Book,){

        try {

            val key = dbBooks.push().key
            if (key!=null){
                dbBooks.child(key).setValue(book)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            Log.d("Firebase", "Datos guardados con éxito")
                        }
                    }
                    .await()



            }



        }catch (e:Exception){

            Log.e("No agregó", e.toString())
        }


    }

    @OptIn(DelicateCoroutinesApi::class)
    fun uploadBook(book: Book, filePaths:List<Uri>){
       GlobalScope.launch {
           val list = uploadimages(book,filePaths)
           book.images= list
           addBook(book)


       }

    }


     @SuppressLint("SuspiciousIndentation")
      suspend fun getBooks(): Flow<List<Book>> {
         val flow = callbackFlow {
             val listener = dbRef.addValueEventListener(object : ValueEventListener {
                 override fun onDataChange(snapshot: DataSnapshot) {
                     val books = snapshot.children.mapNotNull {  snapshot ->
                         val book = snapshot.getValue(Book::class.java)
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

         return flow


    }
    suspend fun getBooksSale(): Flow<List<Book>> {
        return getBooks().map { books ->
            books.filter { book ->
                book.precio.isNotEmpty()
            }
        }
    }
    suspend fun getBooksExchange(): Flow<List<Book>> {
        return getBooks().map { books ->
            books.filter { book ->
                book.precio.isEmpty()
            }
        }
    }





}


