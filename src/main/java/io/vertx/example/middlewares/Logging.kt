package io.vertx.example.middlewares

import io.vertx.example.foundation.KRouter
import io.vertx.example.foundation.Middleware
import mu.KotlinLogging

class Logging : KRouter(), Middleware {
    private val logger = KotlinLogging.logger { }

    init {
        route().handleCoroutine { req, _, next ->
            logger.debug(req.path())
            next()
        }
    }
}
