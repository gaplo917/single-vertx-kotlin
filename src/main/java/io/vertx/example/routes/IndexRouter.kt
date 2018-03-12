package io.vertx.example.routes

import io.vertx.example.foundation.KRouter
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.suspendCoroutine

class IndexRouter : KRouter() {
    init {
        get("/").handleCoroutine { _ ,res ->
            res.send("OK")
        }
        get("/2ms").handleCoroutine { _ ,res ->
            res.send(fakeIOOperation(2))
        }
        get("/5ms").handleCoroutine { _ ,res ->
            res.send(fakeIOOperation(5))
        }
        get("/10ms").handleCoroutine { _ ,res ->
            res.send(fakeIOOperation(10))
        }
    }

    suspend fun fakeIOOperation(duration: Long): String {
        return suspendCoroutine {
            launch {
                delay(duration)
                it.resume("OK")
            }
        }
    }
}
