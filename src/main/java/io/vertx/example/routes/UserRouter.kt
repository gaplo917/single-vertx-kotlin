package io.vertx.example.routes

import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import io.vertx.example.Injection
import io.vertx.example.exceptions.MissParamException
import io.vertx.example.foundation.KRequest
import io.vertx.example.foundation.KRouter
import io.vertx.example.repositories.UserRepository
import io.vertx.example.services.User
import io.vertx.example.services.UserService
import mu.KotlinLogging

class UserRouter : KRouter() {
    private val logger = KotlinLogging.logger {  }

    private val userRepository: UserRepository by Injection.lazy.instance()

    private val userService: UserService by Injection.lazy.instance()

    init {
        get("/"){ req, res ->
            res.json(listUsers())
        }

        get("/:id"){ req, res ->
            res.json(getUserById(req))
        }

        post("/test"){ req ->
            logger.info { req.body }
        }

    }
    private suspend fun getUserById(req: KRequest<*>): User {
        val id = req.params["id"]?.toLong() ?: throw MissParamException(missingKey = "id")
        return userService.findUserById(id)
    }

    private fun listUsers(): List<User> {
        return userRepository.findUsers()
    }
}
