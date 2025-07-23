package com.OIC.plugins

import com.OIC.models.Account
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.request.receive

fun Application.configureSerialization() {
    install(ContentNegotiation){
        json()
    }

    val accounts = mutableListOf<Account>()

    routing {
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }

        post("/account"){
            val requestBody = call.receive<Account>()
            accounts.add(requestBody)
            call.respond(requestBody)
        }

        get("/accounts"){
            call.respond(accounts)

        }
    }
}
