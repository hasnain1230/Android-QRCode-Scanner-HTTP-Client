package com.jscj.qrcodescanner.http

enum class BodyTypes {
    PLAIN_TEXT,
    JSON,
    XML;

    override fun toString(): String {
        return when (this) {
            PLAIN_TEXT -> "Plain Text"
            JSON -> "JSON"
            XML -> "XML"
        }
    }

    companion object { // We put this in a companion object so we can call it from anywhere
        fun fromString(string: String): BodyTypes {
            return when (string) {
                "Plain Text" -> PLAIN_TEXT
                "JSON" -> JSON
                "XML" -> XML
                else -> PLAIN_TEXT
            }
        }

        fun getListOfBodyTypes(): List<BodyTypes> {
            return listOf(PLAIN_TEXT, JSON, XML)
        }

        fun getListOfBodyTypesAsString(): List<String> {
            return listOf("Plain Text", "JSON", "XML")
        }
    }
}