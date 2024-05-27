package com.grupo10.readshare.model

data class Book (
                    var user:String ="",
                    var title:String="",
                    var email:String = "",
                    var description:String="",
                    var genero:String = "",
                    var ubication:String = "",
                    var images:List<String> = emptyList(),
                    var precio:String="")
