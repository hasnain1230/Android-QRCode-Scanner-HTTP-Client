package com.jscj.qrcodescanner.settings

enum class SettingsEnums {
    READ_MODE,
    HTTP_MODE,
    CONCATENATE,
    BODY_REQUEST;

    override fun toString(): String {
        return when (this) {
            READ_MODE -> "Read Mode"
            HTTP_MODE -> "HTTP Mode"
            CONCATENATE -> "Concatenate"
            BODY_REQUEST -> "Body Request"
        }
    }

    companion object {
        fun getPossibleModes(): List<SettingsEnums> {
            return listOf(READ_MODE, HTTP_MODE)
        }

        fun getPossibleModesAsString(): List<String> {
            return listOf("Read Mode", "HTTP Mode")
        }

        fun getPossibleRequestTypes(): List<SettingsEnums> {
            return listOf(CONCATENATE, BODY_REQUEST)
        }

        fun getPossibleRequestTypesAsString(): List<String> {
            return listOf("Concatenate", "Body Request")
        }
    }

}