package io.vertx.example.foundation

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerRequest
import io.vertx.example.extensions.vertx.toKExpressRequest
import io.vertx.example.extensions.vertx.toKResponse
import io.vertx.example.foundation.KExpress.Companion.dispatcher
import io.vertx.example.foundation.KExpress.Companion.vertx
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import kotlinx.coroutines.experimental.launch

abstract class KRouter {
    val vertxRouter = Router.router(vertx)

    inline fun <T> Route.handleCoroutine(clazz: Class<T>, noinline f: suspend (KRequest<T>, KResponse, () -> Unit) -> Unit): Route {
        return this.handler { ctx ->
            launch(dispatcher) {
                try {
                    f(ctx.toKExpressRequest(clazz),ctx.toKResponse(), ctx::next)
                } catch (e: Throwable) {
                    ctx.fail(e)
                }
            }
        }
    }

    inline fun <T> Route.handleCoroutine(clazz: Class<T>, noinline f: suspend (KRequest<T>, KResponse) -> Unit): Route {
        return handleCoroutine(clazz) { req, res, _ -> f(req, res) }
    }

    inline fun <T> Route.handleCoroutine(clazz: Class<T>, noinline f: suspend (KRequest<T>) -> Unit): Route {
        return handleCoroutine(clazz) { req, _, _ -> f(req) }
    }

    /**
     * GET
     */
    fun get(path: String, f: suspend (KRequest<Unit>, KResponse, () -> Unit) -> Unit): Route {
        return vertxRouter.get(path).handleCoroutine(Unit::class.java) { req, res, next -> f(req, res, next) }
    }

    fun get(path: String, f: suspend (KRequest<Unit>, KResponse) -> Unit): Route {
        return get(path){ req, res, _ -> f(req, res) }
    }

    fun get(path: String, f: suspend (KRequest<Unit>) -> Unit): Route {
        return get(path){ req, _, _ -> f(req) }
    }

    /**
     * POST
     */
    fun post(path: String, f: suspend (KRequest<Buffer>, KResponse, () -> Unit) -> Unit): Route {
        return vertxRouter.post(path).handleCoroutine(Buffer::class.java){ req, res, next -> f(req, res, next) }
    }

    fun post(path: String, f: suspend (KRequest<Buffer>, KResponse) -> Unit): Route {
        return post(path){ req, res, _ -> f(req, res) }
    }

    fun post(path: String, f: suspend (KRequest<Buffer>) -> Unit): Route {
        return post(path){ req, _, _ -> f(req) }
    }

    fun <T: Any> post(path: String, clazz: Class<T>, f: suspend (KRequest<T>, KResponse, () -> Unit) -> Unit): Route {
        return vertxRouter.post(path).handleCoroutine(clazz){ req, res, next -> f(req, res, next) }
    }

    fun <T: Any> post(path: String, clazz: Class<T>, f: suspend (KRequest<T>, KResponse) -> Unit): Route {
        return post(path, clazz){ req, res, _ ->
            f(req, res)
        }
    }

    fun <T: Any> post(path: String, clazz: Class<T>, f: suspend (KRequest<T>) -> Unit): Route {
        return post(path, clazz){ req, _, _ ->
            f(req)
        }
    }

    fun use(path: String, subRouter: KRouter) {
        vertxRouter.mountSubRouter(path, subRouter.vertxRouter)
    }

    fun use(path: String, vararg subRouters: KRouter) {
        subRouters.forEach { use(path, it) }
    }

    fun use(path: String, subRouters: List<KRouter>) {
        subRouters.forEach { use(path, it) }
    }

    fun use(middleware: KMiddleware) {
        use("/", middleware)
    }

    fun use(f: FailureHandler) {
        vertxRouter.route().failureHandler { ctx -> f(ctx.failure(), ctx.request(), ctx.response(), ctx::next) }
    }

    protected fun accept(request: HttpServerRequest) {
        vertxRouter.accept(request)
    }

    fun route(): Route {
        return vertxRouter.route()
    }

    fun route(f: suspend (KRequest<Unit>, KResponse, () -> Unit) -> Unit): Route {
        return vertxRouter.route().handleCoroutine(Unit::class.java){ req, res, next ->
            f(req, res, next)
        }
    }
}
