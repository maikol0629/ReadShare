package com.grupo10.readshare.model

class User (){
    var name:String = ""
    var email:String = ""
    var pass:String = ""
    var address:String = ""
    var phone:String = ""

    constructor(name: String, email: String, pass: String, address: String, phone: String) : this() {
        this.name = name
        this.email = email
        this.pass = pass
        this.address = address
        this.phone = phone
    }

    fun isNotBlank(): Boolean {
        return name.isNotBlank() && email.isNotBlank() && pass.isNotBlank() && address.isNotBlank() && phone.isNotBlank()
    }
}