package io.vertx.example.foundation

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.example.exceptions.RequestPayloadDeserializeException
import io.vertx.ext.web.Cookie
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext

class KRequest<out T>(private val ctx: RoutingContext, private val clazz: Class<T>) {
    val req = ctx.request()

    val app: KExpress
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    val fresh: Boolean
        get() = TODO()
    val hostname: String
        get() = req.host()

    val method: HttpMethod
        get() = req.method()

    val originalUrl: String
        get() = req.absoluteURI()

    val params: Map<String, String>
        get() = req.params().map { it.key to it.value }.toMap()

    val path: String
        get() = req.path()

    val protocol: String
        get() = if(req.isSSL) "https" else "http"

    val query: Map<String, String>
        get() = ctx.queryParams().map { it.key to it.value }.toMap()

    val route: Route
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    val secure: Boolean
        get() = req.isSSL

    val signedCookies: Map<String, Cookie>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    val stale: Boolean
        get() = !fresh

    val subdomains: List<String>
        get() {
            val path = req.connection().remoteAddress().path()

            return if(path != null){
                val xs = path.split(".")
                if(xs.size <= 2) listOf() else xs.dropLast(2)
            } else {
                listOf()
            }
        }

    val xhr: Boolean get() = req.getHeader("X-Requested-With") == "XMLHttpRequest"

    val ip: String get(){
        // TODO: When the trust proxy setting does not evaluate to false, the value of this property is derived from the left-most entry in the X-Forwarded-For header. This header can be set by the client or by the proxy.
        return req.getHeader("X-Forwarded-For").split(",")[0]
    }

    val ips: List<String> get() {
        // TODO: When the trust proxy setting does not evaluate to false, the value of this property is derived from the left-most entry in the X-Forwarded-For header. This header can be set by the client or by the proxy.
        return req.getHeader("X-Forwarded-For").split(",")
    }

    val baseUrl: String get() {
        return ctx.mountPoint()
    }
    val cookies: Map<String, Cookie> get() {
        return ctx.cookies().map { it.name to it }.toMap()
    }

    val body: T get() {
        if(clazz == Unit::class.java) {
            @Suppress("UNCHECKED_CAST")
            return Unit as T
        }
        if(clazz == Buffer::class.java){
            @Suppress("UNCHECKED_CAST")
            return ctx.body as T
        }
        if(clazz == String::class.java){
            @Suppress("UNCHECKED_CAST")
            return ctx.bodyAsString as T
        }
        return try {
            jacksonObjectMapper()
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .convertValue(ctx.bodyAsJson.map, clazz)

        } catch (e: Throwable) {
            throw RequestPayloadDeserializeException(e.message)
        }
    }

    fun accepts(vararg types: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun acceptsCharsets(vararg charset: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun acceptsEncodings(vararg encoding: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun acceptsLanguages(vararg lang: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    operator fun get(field: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun `is`(type: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun range(size: Long, combine: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}