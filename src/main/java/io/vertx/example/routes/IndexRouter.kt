package io.vertx.example.routes

import io.vertx.example.foundation.KRouter
import io.vertx.example.services.User
import kotlinx.coroutines.experimental.delay

class IndexRouter : KRouter() {
    init {
        get("/index.html").renderHTML("/templates/index.peb") { context ->
            context.data()["user"] = User(id = 2, name = "Gary")
        }

        get("/").handleCoroutine { req, res ->
            res.end("OK")
        }
        get("/2ms").handleCoroutine { req, res ->
            delay(2)
            res.end("OK")
        }
        get("/5ms").handleCoroutine { req, res ->
            delay(5)
            res.end("OK")
        }
        get("/10ms").handleCoroutine { req, res ->
            delay(10)
            res.end("OK")
        }
    }
}
