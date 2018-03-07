package io.vertx.example

import com.github.salomonbrys.kodein.Kodein
import io.vertx.example.routes.IndexRouter
import io.vertx.example.routes.UserRouter

fun main(args: Array<String>){
    val app = App()

    app.use("/", IndexRouter())

    app.use("/users", UserRouter())

    app.start()
}

