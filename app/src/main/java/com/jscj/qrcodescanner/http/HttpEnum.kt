package com.jscj.qrcodescanner.http

enum class HttpEnum {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE;

    override fun toString(): String {
        return when (this) {
            GET -> "GET"
            POST -> "POST"
            PUT -> "PUT"
            PATCH -> "PATCH"
            DELETE -> "DELETE"
        }
    }

    companion object { // We put this in a companion object so we can call it from anywhere
        fun fromString(string: String): HttpEnum {
            return when (string) {
                "GET" -> GET
                "POST" -> POST
                "PUT" -> PUT
                "PATCH" -> PATCH
                "DELETE" -> DELETE
                else -> GET
            }
        }

        fun getListOfHttpMethods(): List<HttpEnum> {
            return listOf(GET, POST, PUT, PATCH, DELETE)
        }

        fun getListOfHttpMethodsAsString(): List<String> {
            return listOf("GET", "POST", "PUT", "PATCH", "DELETE")
        }
    }
}