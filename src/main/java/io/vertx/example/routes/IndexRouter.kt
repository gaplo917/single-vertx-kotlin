package io.vertx.example.routes

import io.vertx.example.foundation.KRouter
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay

class IndexRouter : KRouter() {
    init {
        get("/").handleCoroutine { _ ,res ->
            res.send("OK")
        }

        get("/10ms").handleCoroutine { _ ,res ->
            res.send(fakeIOOperation(10))
        }
    }

    suspend fun fakeIOOperation(duration: Long): String {
        return async {
          delay(duration)
          "OK"
        }.await()
    }
}
