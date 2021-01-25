package com.adil.routes

import auth.JwtService
import auth.MySession
import com.adil.API_VERSION
import com.adil.repository.Repository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

const val USERS = "$API_VERSION/users"
const val USER_LOGIN = "$USERS/login"
const val USER_CREATE = "$USERS/create"
const val USER_LOGOUT = "$USERS/logout"
const val USER_DELETE = "$USERS/delete"

@KtorExperimentalLocationsAPI
@Location(USER_LOGIN)
class UserLoginRoute

@KtorExperimentalLocationsAPI
@Location(USER_LOGOUT)
class UserLogoutRoute

@KtorExperimentalLocationsAPI
@Location(USER_CREATE)
class UserCreateRoute

@KtorExperimentalLocationsAPI
@Location(USER_DELETE)
class UserDeleteRoute

@KtorExperimentalLocationsAPI
fun Route.createUser(
    db: Repository,
    jwt: JwtService,
    hashFunction: (String) -> String
) {
    post<UserCreateRoute> {
        val signupParameters = call.receive<Parameters>() // 3
        val password = signupParameters["password"] // 4
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
        val displayName = signupParameters["displayName"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
        val email = signupParameters["email"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
        val hash = hashFunction(password)
        try {
            val newUser = db.addUser(email, displayName, hash)
            newUser?.userId?.let {
                call.sessions.set(MySession(it))
                call.respondText(jwt.generateToken(newUser), status = HttpStatusCode.Created)
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register", e)
            call.respond(HttpStatusCode.BadRequest, "Problems creating user")
        }
    }

}

@KtorExperimentalLocationsAPI
fun Route.loginUser(
    db: Repository,
    jwt: JwtService,
    hashFunction: (String) -> String
){
    post<UserLoginRoute> {
        val signinParameters = call.receive<Parameters>()
        val password = signinParameters["password"]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing Fields")
        val email = signinParameters["email"]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing Fields")
        val hash = hashFunction(password)
        try {
            val currentUser = db.findUserByEmail(email)
            currentUser?.userId?.let {
                if (currentUser.passwordHash == hash) {
                    call.sessions.set(MySession(it))
                    call.respondText(jwt.generateToken(currentUser))
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest, "Problems retrieving User"
                    )
                }
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register", e)
            call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
        }
    }
}

@KtorExperimentalLocationsAPI
fun Route.logoutUser(
    db: Repository
){
    post<UserLogoutRoute> {
        val signinParameters = call.receive<Parameters>()
        val email = signinParameters["email"] ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing Fields")

        try {
            val currentUser = db.findUserByEmail(email)
            currentUser?.userId?.let {
                call.sessions.clear(call.sessions.findName(MySession::class))
                call.respond(HttpStatusCode.OK)
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
        }
    }
}

@KtorExperimentalLocationsAPI
fun Route.deleteUser(
    db: Repository
){
    delete<UserDeleteRoute> {
        val signinParameters = call.receive<Parameters>()
        val email =
            signinParameters["email"] ?: return@delete call.respond(HttpStatusCode.Unauthorized, "Missing Fields")

        try {
            val currentUser = db.findUserByEmail(email)
            currentUser?.userId?.let {
                db.deleteUser(it)
                call.sessions.clear(call.sessions.findName(MySession::class))
                call.respond(HttpStatusCode.OK)
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
        }
    }
}

@KtorExperimentalLocationsAPI
fun Application.registerUserRoute(
    db: Repository,
    jwt: JwtService,
    hashFunction: (String) -> String
){
    routing {
        deleteUser(db)
        loginUser(db, jwt, hashFunction)
        logoutUser(db)
        createUser(db, jwt, hashFunction)
    }
}