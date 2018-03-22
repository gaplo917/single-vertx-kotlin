package io.vertx.example.routes

import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import io.vertx.core.http.HttpServerRequest
import io.vertx.example.Injection
import io.vertx.example.exceptions.MissParamException
import io.vertx.example.exceptions.ResourceNotFoundExcpetion
import io.vertx.example.foundation.KRouter
import io.vertx.example.jooq.tables.pojos.VertxUser
import io.vertx.example.repositories.UserRepository
import io.vertx.example.services.UserService

class UserRouter : KRouter() {
    private val userRepository: UserRepository by Injection.lazy.instance()

    private val userService: UserService by Injection.lazy.instance()

    init {
        get("/"){ req, res ->
            res.json(listUsers())
        }

        get("/:id"){ req, res ->
            res.json(getUserById(req))
        }

    }
    private suspend fun getUserById(req: HttpServerRequest): VertxUser {
        val id = req.params()["id"]?.toInt() ?: throw MissParamException(missingKey = "id")
        return userService.findUserById(id) ?: throw ResourceNotFoundExcpetion("user not found")
    }

    private fun listUsers(): List<VertxUser> {
        return userRepository.findUsers()
    }
}
