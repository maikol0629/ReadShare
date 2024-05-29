package com.grupo10.readshare.model

import android.net.Uri

data class Book (
    var user:String ="",
    var id:String="",
    var title:String="",
    var description:String="",
    var genero:String = "",
    var ubication:String = "",
    var images:List<String> = emptyList(),
    var uris:List<Uri> = emptyList(),
    var price:String="",
    var address:String="")

