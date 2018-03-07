package io.vertx.example

import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class UserController {
  @Inject
  lateinit var router: Router

  @Inject
  lateinit var userRepository: UserRepository

  @Inject
  lateinit var userService: UserService

  init {
    MainApp.injector.inject(this)

    println("init user controller")
    router.apply {
      get("/users").handler { listUsers(it) }
      get("/users/:id").handler { getUserById(it) }
    }
  }

  private fun getUserById(req: RoutingContext){
    val id = req.pathParam("id")?.toLong() ?: throw IllegalArgumentException()
    req.response().end(
      Json.encode(
        userService.findUserById(id)
      )
    )
  }

  private fun listUsers(req: RoutingContext){
    req.response().end(
     Json.encode(
        userRepository.findUsers()
      )
    )
  }
}
