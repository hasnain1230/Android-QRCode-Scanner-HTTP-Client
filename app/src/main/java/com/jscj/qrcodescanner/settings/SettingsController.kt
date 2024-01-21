package com.jscj.qrcodescanner.settings

import androidx.compose.runtime.mutableStateOf

class SettingsController {
    private val possibleModes = listOf("read_mode", "http_mode")
    private var currentMode = mutableStateOf("read_mode")
    private val possibleHttpMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE")
    private var selectedHttpMethod = mutableStateOf("GET")
    private var url = mutableStateOf("")
    private val possibleRequestTypes = listOf("concatenate", "body_request")
    private var requestType = mutableStateOf("concatenate")

    fun getCurrentMode() = currentMode
    fun getSelectedHttpMethod() = selectedHttpMethod
    fun getUrl() = url
    fun getRequestType() = requestType

    fun setCurrentMode(mode: String) {
        if (!possibleModes.contains(mode)) {
            currentMode.value = "read_mode"
            return
        }

        currentMode.value = mode
    }

    fun setSelectedHttpMethod(method: String) {
        if (!possibleHttpMethods.contains(method)) {
            selectedHttpMethod.value = "GET"
            return
        }
        selectedHttpMethod.value = method
    }

    fun setUrl(url: String) {
        this.url.value = url
    }

    fun setRequestType(requestType: String) {
        if (!possibleRequestTypes.contains(requestType)) {
            this.requestType.value = "body_request"
            return
        }
        this.requestType.value = requestType
    }
}
