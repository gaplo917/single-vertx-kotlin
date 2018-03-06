import io.vertx.core.Vertx
import io.vertx.ext.web.Router

val vertx: Vertx = Vertx.vertx()

fun main(args: Array<String>){
    vertx.createHttpServer()
            .requestHandler { createRouter().accept(it) }
            .listen(8080){ result ->
                if (result.succeeded()) {
                    println("Server up and running")
                } else {
                    println(result.cause())
                }
            }
}

fun createRouter(): Router = Router.router(vertx).apply {
    get("/").handler({ req ->
        req.response().end("Hello world!")
    })
}
