package com.OIC.account

import kotlinx.serialization.Serializable

@Serializable
data class user(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
)