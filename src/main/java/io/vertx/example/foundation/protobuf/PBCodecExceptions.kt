package io.vertx.example.foundation.protobuf

data class PBCodecDecodeException(override val message: String): Exception(message)

data class PBCodecEncodeException(override val message: String): Exception(message)
