package io.vertx.example.foundation

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import io.vertx.example.extensions.vertx.json
import io.vertx.ext.web.Cookie
import io.vertx.ext.web.RoutingContext

class KResponse(ctx: RoutingContext) {
    private val res = ctx.response()
    val app: KExpress get() = TODO()
    val headersSent: Boolean get() = TODO()
    val locals: Any get() = TODO()

    fun append(field: String, values: List<Any>){

    }

    fun attachment(filenames: List<String>){}

    fun cookie(name: String, value: Cookie){}

    fun clearCookie(name: String, options: Any){}

    fun download(path: String, filename: String?, options: Any, fn: (Throwable) -> Unit){}

    fun end(){
        res.end()
    }

    fun end(data: String){
        res.end(data)
    }

    fun end(data: String, encoding: String) {
        res.end(data, encoding)
    }

    fun format(formats: Map<String, () -> Unit>){}

    operator fun get(field: String){}

    fun json(body: Any){
        res.putHeader("Content-Type", "application/json").end(Json.encode(body))
    }

    fun jsonnp(body: Any?){}

    fun links(links: Map<String, String>){}

    fun location(path: String){}

    fun redirect(path: String){}

    fun redirect(status: Int, path: String){}

    fun render(view: String, locals: Map<String, Any>, cb: (Throwable, Any) -> Unit){}

    fun send(chunk: String) {
        res.end(chunk)
    }

    fun send(chunk: Buffer) {
        res.end(chunk)
    }

    fun send(chunk: String, enc: String) {
        res.end(chunk, enc)
    }

    fun sendFile(path: String, options: Any, fn: (Throwable) -> Unit){}

    fun sendStatus(statusCode: Int){}

    operator fun set(field: String, value: Any){}

    fun status(code: Int){
        res.statusCode = code
    }

    fun type(type: String){}

    fun vary(field: String){}

}