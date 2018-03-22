package io.vertx.example

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.github.salomonbrys.kodein.singleton
import io.vertx.core.Handler
import io.vertx.example.foundation.KExpress
import io.vertx.example.middlewares.BasicAuth
import io.vertx.example.middlewares.GlobalFailureHandler
import io.vertx.example.middlewares.Logging
import io.vertx.example.repositories.UserRepository
import io.vertx.example.repositories.UserRepositoryImpl
import io.vertx.example.routes.IndexRouter
import io.vertx.example.routes.UserRouter
import io.vertx.example.services.UserService
import io.vertx.example.services.UserServiceImpl
import mu.KotlinLogging
import org.jooq.DSLContext
import org.jooq.impl.DSL

val Injection = Kodein {
    val userModule = Kodein.Module {
        bind<UserService>() with provider { UserServiceImpl() }
        bind<UserRepository>() with provider { UserRepositoryImpl() }
    }

    val jooqModule = Kodein.Module {
        bind<DSLContext>() with singleton {
            DSL.using("jdbc:mysql://localhost:3306/", "root", "")
        }
    }

    import(jooqModule)
    import(userModule)
}

fun main(args: Array<String>) {

    val app = KExpress()

    app.use(Logging())

    //app.use(BasicAuth())

    app.use("/", IndexRouter())

    app.use("/users", UserRouter())

    app.use(GlobalFailureHandler)

    app.listen()
}

