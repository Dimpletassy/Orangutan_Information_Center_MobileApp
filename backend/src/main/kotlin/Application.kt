package com.OIC

import com.OIC.plugins.configureHTTP
import com.OIC.plugins.configureMonitoring
import com.OIC.plugins.configureRouting
import com.OIC.plugins.configureSecurity
import com.OIC.plugins.configureSerialization
import io.ktor.server.application.*

/* MAIN ENTRY POINT */

fun main(args: Array<String>) {
    io.ktor.server.jetty.jakarta.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSecurity()
    configureSerialization()
    configureMonitoring()
    configureRouting()
}
