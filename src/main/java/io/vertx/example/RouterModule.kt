package io.vertx.example

import dagger.Module
import dagger.Provides
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import javax.inject.Singleton

@Module
class RouterModule(private val vertx: Vertx) {

  @Provides
  @Singleton
  fun provideRouter(): Router {
    return Router.router(vertx).apply {
      get("/").handler({ req ->
        req.response().end("Hello world!")
      })
    }
  }
}
