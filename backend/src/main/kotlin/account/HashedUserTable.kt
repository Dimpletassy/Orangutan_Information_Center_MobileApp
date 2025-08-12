package com.OIC.account

import io.ktor.server.auth.UserHashedTableAuth
import io.ktor.util.getDigestFunction

object HashedUserTable {
    val digestFunction = getDigestFunction("SHA-256") { "ktor${it.length}" }

    val userTable = mutableMapOf(
        "admin" to digestFunction("admin")
    )

    val hashedUserTable = UserHashedTableAuth(
        table = userTable,
        digester = digestFunction
    )
}
