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
    inline fun Route.handleCoroutine(noinline f: suspend (RoutingContext) -> Unit): Route {
        return this.handler { ctx ->
            launch(dispatcher) {
                try {
                    f(ctx)
                } catch (e: Throwable) {
                    ctx.fail(e)
                }
            }
        }
    }

    inline fun Route.handleCoroutine(noinline f: suspend (HttpServerRequest, HttpServerResponse) -> Unit): Route {
        return this.handleCoroutine { ctx -> f(ctx.request(), ctx.response()) }
    }

    inline fun Route.handleCoroutine(noinline f: suspend (HttpServerRequest, HttpServerResponse, () -> Unit) -> Unit): Route {
        return this.handleCoroutine { ctx -> f(ctx.request(), ctx.response(), ctx::next) }
    }

    inline fun Router.get(path: String, noinline f: suspend (RoutingContext) -> Unit): Route {
        return this.get(path).handleCoroutine(f)
    }

    inline fun Router.get(path: String, noinline f: suspend (HttpServerRequest, HttpServerResponse) -> Unit): Route {
        return this.get(path).handleCoroutine(f)
    }

    inline fun Router.get(path: String, noinline f: suspend (HttpServerRequest, HttpServerResponse, () -> Unit) -> Unit): Route {
        return this.get(path).handleCoroutine(f)
    }

    inline fun Route.failure(crossinline f: (Throwable) -> Unit): Route {
        return this.failureHandler { ctx -> f(ctx.failure())  }
    }

    inline fun Route.failure(crossinline f: (Throwable, RoutingContext) -> Unit): Route {
        return this.failureHandler { ctx -> f(ctx.failure(), ctx)  }
    }

    inline fun Route.failure(crossinline f: (Throwable, HttpServerRequest, HttpServerResponse) -> Unit): Route {
        return this.failureHandler { ctx -> f(ctx.failure(), ctx.request(), ctx.response())  }
    }

    inline fun Route.failure(crossinline f: (Throwable, HttpServerRequest, HttpServerResponse, () -> Unit) -> Unit): Route {
        return this.failureHandler { ctx -> f(ctx.failure(), ctx.request(), ctx.response(), ctx::next)  }
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
