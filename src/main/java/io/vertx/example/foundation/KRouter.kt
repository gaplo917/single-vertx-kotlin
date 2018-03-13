package io.vertx.example.foundation

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import io.vertx.example.foundation.KExpress.Companion.dispatcher
import io.vertx.example.foundation.KExpress.Companion.vertx
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.experimental.launch

@Suppress("NOTHING_TO_INLINE")
abstract class KRouter : Router by Router.router(vertx) {
    inline fun <T> Route.handleCoroutine(noinline f: suspend (RoutingContext) -> T): Route {
        return this.handler { req ->
            launch(dispatcher) {
                try {
                    f(req)
                } catch (e: Throwable) {
                    e.printStackTrace()

                    KExpress.globalErrorHandler?.handle(req)

                    if (!req.response().ended()) {
                        req.response().end()
                    }
                }
            }
        }
    }

    inline fun <T> Route.handleCoroutine(noinline f: suspend (HttpServerRequest, HttpServerResponse) -> T): Route {
        return this.handleCoroutine { req -> f(req.request(), req.response()) }
    }

    inline fun <T> Route.handleCoroutine(noinline f: suspend (HttpServerRequest, HttpServerResponse, () -> Unit) -> T): Route {
        return this.handleCoroutine { req -> f(req.request(), req.response(), req::next) }
    }

    inline fun HttpServerResponse.send(chunk: String) {
        this.end(chunk)
    }

    inline fun HttpServerResponse.send(chunk: Buffer) {
        this.end(chunk)
    }

    inline fun HttpServerResponse.send(chunk: String, enc: String) {
        this.end(chunk, enc)
    }

    inline fun HttpServerResponse.json(obj: Any) {
        this.putHeader("Content-Type", "application/json").end(Json.encode(obj))
    }
}
