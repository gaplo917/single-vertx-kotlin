package io.vertx.example.extensions.vertx

import com.github.salomonbrys.kodein.instance
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.example.App
import io.vertx.example.exceptions.PXException
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.*

val dispatcher = App.kodein.instance<Vertx>().dispatcher()


fun <T> Route.json(f: suspend (RoutingContext) -> T){
  this.handler { req ->
    launch(dispatcher) {
      try {
        req.response().end(Json.encode(f(req)))
      } catch (e: Throwable){
        when(e){
          is PXException -> {
            req.response().setStatusCode(e.status).end(
              Json.encode(e)
            )
          }
          else -> {
            req.response().setStatusCode(500).end("internal server error")
          }
        }
      }

    }


  }
}
