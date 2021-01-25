package com.adil.routes

import auth.MySession
import com.adil.API_VERSION
import com.adil.repository.Repository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

const val TODOS = "$API_VERSION/todos"

@KtorExperimentalLocationsAPI
@Location(TODOS)
class TodoRoute

@KtorExperimentalLocationsAPI
fun Route.todos(db: Repository) {
    authenticate("jwt") {
        post<TodoRoute> {
            val todosParameters = call.receive<Parameters>()
            val todo = todosParameters["todo"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing todo")
            val done = todosParameters["done"] ?: "false"
            val user = call.sessions.get<MySession>()?.let {
                db.findUser(it.userId)
            } ?: return@post call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")

            try {
                val currentTodo = db.addTodo(user.userId, todo, done.toBoolean())
                currentTodo?.id?.let {
                    call.respond(HttpStatusCode.OK, currentTodo)
                }
            } catch (e: Throwable) {
                application.log.error("Failed to add todo", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Saving Todo")
            }
        }

        get<TodoRoute> {
            val user = call.sessions.get<MySession>()?.let { db.findUser(it.userId) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")

            val todosParameters = call.request.queryParameters
            val limit = if (todosParameters.contains("limit")) todosParameters["limit"] else null
            val offset = if (todosParameters.contains("offset")) todosParameters["offset"] else null
            try {
                if (limit != null && offset != null) {
                    val todos = db.getTodos(user.userId, offset.toInt(), limit.toInt())
                    call.respond(todos)
                } else {
                    val todos = db.getTodos(user.userId)
                    call.respond(todos)
                }
            } catch (e: Throwable) {
                application.log.error("Failed to get Todos", e)
                call.respond(HttpStatusCode.BadRequest, "Problems getting Todos")
            }
        }

        delete<TodoRoute> {
            val todosParameters = call.receive<Parameters>()
            val todoId = todosParameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing Todo Id")
            val user = call.sessions.get<MySession>()?.let { db.findUser(it.userId) }
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")

            try {
                db.deleteTodo(user.userId, todoId.toInt())
                call.respond(HttpStatusCode.OK)
            } catch (e: Throwable) {
                application.log.error("Failed to delete todo", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Deleting Todo")
            }
        }
    }
}

@KtorExperimentalLocationsAPI
fun Application.registerTodoRoutes(db: Repository){
    routing {
        todos(db)
    }
}