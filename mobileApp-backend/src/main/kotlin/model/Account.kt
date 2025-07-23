package com.OIC.models

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val password: String
)
