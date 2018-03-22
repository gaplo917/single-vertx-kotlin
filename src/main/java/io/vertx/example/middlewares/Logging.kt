package io.vertx.example.middlewares

import io.vertx.example.foundation.KRouter
import io.vertx.example.foundation.KMiddleware
import mu.KotlinLogging

class Logging : KMiddleware() {
    private val logger = KotlinLogging.logger { }

    init {
        route { req, _, next ->
            logger.debug(req.path)
            next()
        }
    }
}
