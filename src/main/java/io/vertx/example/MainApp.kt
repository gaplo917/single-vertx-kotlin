package io.vertx.example

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import javax.inject.Inject
import kotlin.reflect.KClass

class MainApp {

  @Inject
  lateinit var router: Router

  companion object {
    lateinit var injector: ApplicationComponent private set
  }

  val vertx: Vertx = Vertx.vertx()

  init {
    injector = DaggerApplicationComponent.builder()
      .userModule(UserModule())
      .routerModule(RouterModule(vertx))
      .build()

    injector.inject(this)

    registerControllers(UserController::class)
  }

  fun runHttpServer(){
    vertx.createHttpServer()
      .requestHandler { router.accept(it) }
      .listen(8080){ result ->
        if (result.succeeded()) {
          println("Server up and running")
        } else {
          println(result.cause())
        }
      }
  }

  fun registerControllers(vararg clazz: KClass<*>){
    clazz.forEach { it.constructors.first().call() }
  }
}
