package io.vertx.example.middlewares

import io.vertx.example.exceptions.UnauthorizedException
import io.vertx.example.foundation.KRouter
import io.vertx.example.foundation.Middleware
import mu.KotlinLogging

class BasicAuth : KRouter(), Middleware {
    private val logger = KotlinLogging.logger { }

    init {
        route().handleCoroutine { req, res, next ->
            if(req.headers()["Basic"] == "testing") {
                next()
            } else {
                logger.info("unauthorized access")
                throw UnauthorizedException
            }
        }
    }
}