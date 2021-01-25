package com.adil.repository

import com.adil.models.Todo
import com.adil.models.User

interface Repository {
    suspend fun addUser(
        email: String,
        displayName: String,
        passwordHash: String
    ): User?

    suspend fun deleteUser(userId: Int)
    suspend fun findUser(userId: Int): User?
    suspend fun findUserByEmail(email: String): User?

    suspend fun addTodo(userId: Int, todo: String, done: Boolean): Todo?
    suspend fun getTodos(userId: Int): List<Todo>
    suspend fun deleteTodo(userId: Int, todoId: Int)
    suspend fun findTodo(userId: Int, todoId: Int): Todo?
    suspend fun getTodos(userId: Int, offset: Int, limit: Int = 100): List<Todo>
}