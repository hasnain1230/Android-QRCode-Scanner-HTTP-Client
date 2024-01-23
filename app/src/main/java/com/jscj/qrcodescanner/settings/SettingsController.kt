package com.jscj.qrcodescanner.settings

import androidx.compose.runtime.mutableStateOf

class SettingsController {
    private val possibleModes = listOf(SettingsEnums.READ_MODE, SettingsEnums.HTTP_MODE)
    private var currentMode = mutableStateOf(SettingsEnums.READ_MODE)
    private val possibleHttpMethods = listOf(
        SettingsEnums.GET,
        SettingsEnums.POST,
        SettingsEnums.PUT,
        SettingsEnums.PATCH,
        SettingsEnums.DELETE
    )
    private var selectedHttpMethod = mutableStateOf(SettingsEnums.GET)
    private var url = mutableStateOf("")
    private val possibleRequestTypes = listOf(SettingsEnums.CONCATENATE, SettingsEnums.BODY_REQUEST)
    private var requestType = mutableStateOf(SettingsEnums.CONCATENATE)


    fun getCurrentMode() = currentMode
    fun getSelectedHttpMethod() = selectedHttpMethod
    fun getUrl() = url
    fun getRequestType() = requestType

    fun setCurrentMode(mode: SettingsEnums) {
        if (!possibleModes.contains(mode)) {
            currentMode.value = SettingsEnums.READ_MODE
            return
        }

        currentMode.value = mode
    }

    fun setSelectedHttpMethod(method: SettingsEnums) {
        if (!possibleHttpMethods.contains(method)) {
            selectedHttpMethod.value = SettingsEnums.GET
            return
        }
        selectedHttpMethod.value = method
    }

    fun setUrl(url: String) {
        this.url.value = url
    }

    fun setRequestType(requestType: SettingsEnums) {
        if (!possibleRequestTypes.contains(requestType)) {
            this.requestType.value = SettingsEnums.CONCATENATE
            return
        }
        this.requestType.value = requestType
    }
}
