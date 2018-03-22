package io.vertx.example.middlewares

import io.vertx.example.foundation.KRouter
import io.vertx.example.foundation.KMiddleware
import io.vertx.ext.web.handler.BodyHandler

class BodyParser: KMiddleware() {
    init {
        route().handler(BodyHandler.create())
    }
}
