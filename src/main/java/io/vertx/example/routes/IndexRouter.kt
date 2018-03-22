package io.vertx.example.routes

import io.vertx.example.foundation.KRouter
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import mu.KotlinLogging

class IndexRouter : KRouter() {
    private val logger = KotlinLogging.logger {  }

    init {
        get("/"){ _ ,res ->
            res.send("OK")
        }

        get("/10ms"){ _ ,res ->
            res.send(fakeIOOperation(10))
        }
    }

    private suspend fun fakeIOOperation(duration: Long): String {
        return async {
          delay(duration)
          "OK"
        }.await()
    }
}
