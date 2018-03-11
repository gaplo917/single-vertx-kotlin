package io.vertx.example.routes

import io.vertx.example.foundation.KRouter
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.CompletableFuture

class IndexRouter : KRouter() {
    init {
        get("/").handleCoroutine { req ->
            req.response().end("OK")
        }
        get("/2ms").handleCoroutine { req ->
            delay(2)
            req.response().end("OK")
        }
        get("/5ms").handleCoroutine { req ->
            delay(5)
            req.response().end("OK")
        }
        get("/10ms").handleCoroutine { req ->
            delay(10)
            req.response().end("OK")
        }
    }
}
