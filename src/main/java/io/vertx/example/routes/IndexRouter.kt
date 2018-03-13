package io.vertx.example.routes

import io.vertx.example.foundation.KRouter
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay

class IndexRouter : KRouter() {
    init {
        get("/"){ _ ,res ->
            res.send("OK")
        }.failure { err, req ->
            println("exception = e=${err}")
        }

        get("/10ms"){ _ ,res ->
            res.send(fakeIOOperation(10))
        }.failure { err, req, res ->
            println("exception = e=${err.message}")
            res.send("error = ${err.message}")
        }
    }

    suspend fun fakeIOOperation(duration: Long): String {
        return async {
            throw IllegalArgumentException("test")
          delay(duration)
          "OK"
        }.await()
    }
}
