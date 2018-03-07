package io.vertx.example.routes

import com.github.salomonbrys.kodein.instance
import io.vertx.example.App
import io.vertx.ext.web.Router

class IndexRouter : Router by Router.router(App.kodein.instance()) {
  init {
    get("/").handler { req ->
      req.response().end("index page")
    }
  }
}
