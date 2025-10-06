package com.oic.myapplication.services.auth

fun validateLoginInput(username: String,
                       password: String,
                       minUserNameLength: Int = 3,
                       minPasswordLength: Int = 6): String?{
    return when {
        username.isBlank() || password.isBlank() -> "Please enter username and password"
        username.length < minUserNameLength -> "Username must be at least $minUserNameLength characters long"
        password.length < minPasswordLength -> "Password must be at least $minPasswordLength characters long"
        else -> null
    }
}

//TODO: make this more detailed
fun validateSignUpInput(firstName: String,
                        lastName: String,
                        contact: String,
                        password: String): String? {
    return when {
        firstName.isBlank() || lastName.isBlank() || contact.isBlank() || password.isBlank() -> "Please fill out the fields"
        else -> null
    }
}