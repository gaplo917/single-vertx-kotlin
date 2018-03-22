package io.vertx.example.middlewares

import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.example.exceptions.KExpressException
import io.vertx.example.extensions.vertx.json
import io.vertx.example.foundation.FailureHandler
import io.vertx.example.foundation.KExpress.Companion.config
import mu.KotlinLogging

object GlobalFailureHandler: FailureHandler {
    private val logger = KotlinLogging.logger { }

    override fun invoke(e: Throwable, req: HttpServerRequest, res: HttpServerResponse, next: () -> Unit) {
        logger.error(e) {
            "${req.path()} encounter error "
        }

        when(e){
            is KExpressException -> {
                // api exception
                if(config.getBoolean("app.debug")){
                    res.setStatusCode(e.statusCode).json(mapOf("message" to e.prodMessage, "debugMessage" to e.debugMessage))
                } else {
                    res.setStatusCode(e.statusCode).json(mapOf("message" to e.prodMessage))
                }
            }
            else -> {
                // unknown exception
                res.setStatusCode(500).json(mapOf("message" to "internal server error"))
            }
        }
    }
}
