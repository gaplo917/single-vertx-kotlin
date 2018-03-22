package io.vertx.example.extensions.vertx

import io.vertx.example.foundation.KRequest
import io.vertx.example.foundation.KResponse
import io.vertx.ext.web.RoutingContext

fun <T> RoutingContext.toKExpressRequest(clazz: Class<T>): KRequest<T> {
    return KRequest(this, clazz)
}

fun RoutingContext.toKResponse(): KResponse {

    return KResponse(this)
}