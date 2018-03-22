package io.vertx.example.foundation

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.CoroutineDispatcher
import mu.KotlinLogging

typealias FailureHandler = (Throwable, HttpServerRequest, HttpServerResponse, () -> Unit) -> Unit

open class KExpress : KRouter() {
    private val logger = KotlinLogging.logger { }


    companion object {
        val vertx: Vertx = Vertx.vertx()
        val dispatcher: CoroutineDispatcher = vertx.dispatcher()
        val config: Config = ConfigFactory.load()
    }

    fun listen(port: Int = config.getInt("http.server.port")) {
        vertx.createHttpServer()
                .requestHandler { accept(it) }
                .listen(port) { result ->
                    if (result.succeeded()) {
                        logger.debug { "KExpress is listening to port: $port" }
                    } else {
                        logger.error { result.cause() }
                    }
                }
    }

}
