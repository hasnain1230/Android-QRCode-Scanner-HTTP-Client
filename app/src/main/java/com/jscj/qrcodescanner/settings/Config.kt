package com.jscj.qrcodescanner.settings

import com.jscj.qrcodescanner.http.BodyTypes
import com.jscj.qrcodescanner.http.HttpEnum
import org.json.JSONObject

class Config(
    private val _configName: String,
    val currentMode: SettingsEnums,
    val selectedHttpMethod: HttpEnum,
    val url: String,
    val requestType: SettingsEnums,
    val bodyTypes: BodyTypes? = null
) {
    // toString method to display the configuration
    override fun toString(): String {
        return JSONObject().apply {
            put("configName", _configName)
            put("currentMode", currentMode)
            put("selectedHttpMethod", selectedHttpMethod)
            put("url", url)
            put("requestType", requestType)
            put("bodyTypes", bodyTypes)
        }.toString(4)
    }
}