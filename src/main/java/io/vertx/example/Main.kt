package io.vertx.example

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.vertx.core.Handler
import io.vertx.example.foundation.KExpress
import io.vertx.example.middlewares.BasicAuth
import io.vertx.example.middlewares.Logging
import io.vertx.example.repositories.UserRepository
import io.vertx.example.repositories.UserRepositoryImpl
import io.vertx.example.routes.IndexRouter
import io.vertx.example.routes.UserRouter
import io.vertx.example.services.UserService
import io.vertx.example.services.UserServiceImpl
import mu.KotlinLogging

val Injection = Kodein {
    val userModule = Kodein.Module {
        bind<UserService>() with provider { UserServiceImpl() }
        bind<UserRepository>() with provider { UserRepositoryImpl() }
    }

    import(userModule)
}

fun main(args: Array<String>) {
    val logger = KotlinLogging.logger { }

    val app = KExpress()

    app.use(Logging())

    //app.use(BasicAuth())

    app.use("/", IndexRouter())

    app.use("/users", UserRouter())

    app.use(Handler { e: Throwable ->
        logger.error { "exception handler, e=${e.message}" }
    })

    app.listen(8080)
}

