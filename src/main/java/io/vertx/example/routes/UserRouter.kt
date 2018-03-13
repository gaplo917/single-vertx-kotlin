package io.vertx.example.routes

import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.Json
import io.vertx.example.Injection
import io.vertx.example.foundation.KRouter
import io.vertx.example.repositories.UserRepository
import io.vertx.example.services.User
import io.vertx.example.services.UserService
import io.vertx.ext.web.RoutingContext

class UserRouter : KRouter() {

    private val userRepository: UserRepository by Injection.lazy.instance()

    private val userService: UserService by Injection.lazy.instance()

    init {
        get("/").handleCoroutine { req, res ->
            res.json(listUsers())
        }

        get("/:id").handleCoroutine { req, res ->
            res.json(getUserById(req))
        }

    }
    private suspend fun getUserById(req: HttpServerRequest): User {
        val id = req.params()["id"]?.toLong() ?: throw IllegalArgumentException()
        return userService.findUserById(id)
    }

    private fun listUsers(): List<User> {
        return userRepository.findUsers()
    }
}
