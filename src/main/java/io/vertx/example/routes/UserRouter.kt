package io.vertx.example.routes

import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import io.vertx.example.*
import io.vertx.example.extensions.vertx.json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class UserRouter : Router by Router.router(App.kodein.instance()) {

  private val userRepository: UserRepository by App.kodein.lazy.instance()

  private val userService: UserService by App.kodein.lazy.instance()

  init {
    get("/").json { listUsers(it) }

    get("/:id").json { getUserById(it) }
  }

  private fun getUserById(req: RoutingContext): User {
    val id = req.pathParam("id")?.toLong() ?: throw IllegalArgumentException()
    return userService.findUserById(id)
  }

  private fun listUsers(req: RoutingContext): List<User>{
    return userRepository.findUsers()
  }
}
