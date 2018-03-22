package io.vertx.example.extensions.vertx

import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json

@Suppress("NOTHING_TO_INLINE")

inline fun HttpServerResponse.json(obj: Any) {
    this.putHeader("Content-Type", "application/json").end(Json.encode(obj))
}
