package com.OIC.account

import com.OIC.account.HashedUserTable.digestFunction
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import com.OIC.account.HashedUserTable.userTable

@Serializable
data class signupRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: Int,
    val password: String
)

fun Route.signupRoutes() {
    post("/signup") {
        val signupData = call.receive<signupRequest>()
        val firstName = signupData.firstName
        val password = signupData.password

        if (userTable.containsKey(firstName)) {
            call.respondText("User already exists", status = io.ktor.http.HttpStatusCode.Conflict)
            return@post
        }

        userTable[firstName] = digestFunction(password)
        call.respondText("User $firstName created successfully")
    }
}