package io.vertx.example.exceptions

abstract class KExpressException(
        val statusCode: Int,
        val debugMessage: String,
        val prodMessage: String,
        override val message: String = debugMessage
): Throwable()

object UnauthorizedException: KExpressException (
        statusCode = 401,
        debugMessage = "uauthorized",
        prodMessage = "uauthorized"
)

data class MissParamException(
        private val missingKey: String
): KExpressException (
        statusCode = 400,
        debugMessage = "missing params($missingKey).",
        prodMessage = "missing params($missingKey)."
)

data class RequestPayloadDeserializeException(
        private val errorMessage: String? = null
): KExpressException (
        statusCode = 400,
        debugMessage = "$errorMessage",
        prodMessage = "Invalid request payload."
)

data class ResourceNotFoundExcpetion(
        private val errorMessage: String
): KExpressException (
        statusCode = 400,
        debugMessage = errorMessage,
        prodMessage = errorMessage
)