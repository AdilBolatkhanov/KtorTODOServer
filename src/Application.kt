package com.adil

import auth.JwtService
import auth.MySession
import auth.hash
import com.adil.repository.DatabaseFactory
import com.adil.repository.ToDoRepository
import com.adil.routes.registerTodoRoutes
import com.adil.routes.registerUserRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.locations.*
import io.ktor.sessions.*
import io.ktor.util.*
import kotlin.collections.set

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations) {
    }

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    DatabaseFactory.init()
    val db = ToDoRepository()
    val jwtService = JwtService()
    val hashFunction = { s: String -> hash(s) }

    install(Authentication) {
        jwt("jwt") {
            verifier(jwtService.verifier)
            realm = "Todo Server"
            validate {
                val payload = it.payload
                val claim = payload.getClaim("id")
                val user = db.findUser(claim.asInt())
                user
            }
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    registerUserRoute(db, jwtService, hashFunction)
    registerTodoRoutes(db)
}

const val API_VERSION = "/v1"

