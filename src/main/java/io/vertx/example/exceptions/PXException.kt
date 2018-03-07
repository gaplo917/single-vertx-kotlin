package io.vertx.example.exceptions

data class PXException(val status: Int, val errorCode: String) : Throwable()
