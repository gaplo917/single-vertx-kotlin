package io.vertx.example.middlewares

import io.vertx.example.exceptions.UnauthorizedException
import io.vertx.example.foundation.KRouter
import io.vertx.example.foundation.KMiddleware
import mu.KotlinLogging

class BasicAuth : KMiddleware() {
    private val logger = KotlinLogging.logger { }

    init {
        route { req, res, next ->
            if(req.get("Basic") == "testing") {
                next()
            } else {
                logger.info("unauthorized access")
                throw UnauthorizedException
            }
        }
    }
}