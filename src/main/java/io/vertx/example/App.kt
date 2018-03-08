package io.vertx.example

import com.github.salomonbrys.kodein.*
import io.vertx.core.Vertx
import io.vertx.example.repositories.UserRepository
import io.vertx.example.repositories.UserRepositoryImpl
import io.vertx.example.services.UserService
import io.vertx.example.services.UserServiceImpl
import io.vertx.ext.web.Router
import org.jetbrains.annotations.TestOnly

class App: Router by App.kodein.instance() {

  private val vertx: Vertx = App.kodein.instance()

  companion object {
    private val vertxModule = Kodein.Module {
      bind<Vertx>() with eagerSingleton { Vertx.vertx() }
      bind<Router>() with eagerSingleton {
        Router.router(instance())
      }
    }

    private val userModule = Kodein.Module {
      bind<UserService>() with provider { UserServiceImpl() }
      bind<UserRepository>() with provider { UserRepositoryImpl() }
    }

    var kodein: Kodein = Kodein {
      import(userModule)
      import(vertxModule)
    }
    @TestOnly set
  }

  fun start(){
    vertx.createHttpServer()
      .requestHandler(::accept)
      .listen(8080){ result ->
        if (result.succeeded()) {
          println("Server up and running")
        } else {
          println(result.cause())
        }
      }

    App.kodein = Kodein{}
  }

  /**
   * express liked API
   */
  fun use(path: String, subRouter: Router){
    mountSubRouter(path, subRouter)
  }
}
