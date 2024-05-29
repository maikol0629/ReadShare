package com.grupo10.readshare.model

class User (){
    var name:String = ""
    var lastName:String=""
    var email:String = ""
    var pass:String = ""
    var image:String = ""
    var id:String = ""



    constructor(name: String, lastName:String, email: String, pass: String, books: List<Book>) : this() {
        this.name = name
        this.lastName= lastName
        this.email = email
        this.pass = pass
    }

    fun isNotBlank(): Boolean {
        return name.isNotBlank() && email.isNotBlank() && pass.isNotBlank() && lastName.isNotBlank()
    }
    fun isNotBlank2(): Boolean {
        return name.isNotBlank() && lastName.isNotBlank()
    }
}