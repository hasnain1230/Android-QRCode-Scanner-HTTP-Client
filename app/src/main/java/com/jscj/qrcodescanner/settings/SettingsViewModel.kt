package com.jscj.qrcodescanner.settings

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.jscj.qrcodescanner.http.BodyTypes
import com.jscj.qrcodescanner.http.HttpEnum

class SettingsViewModel : ViewModel() {
    private val possibleModes = SettingsEnums.getPossibleModes()
    private var currentMode = mutableStateOf(SettingsEnums.READ_MODE)
    private val possibleHttpMethods = HttpEnum.getListOfHttpMethods()
    private var selectedHttpMethod = mutableStateOf(HttpEnum.GET)
    private var url = mutableStateOf("")
    private val possibleRequestTypes = SettingsEnums.getPossibleRequestTypes()
    private var requestType = mutableStateOf(SettingsEnums.CONCATENATE)
    private var possibleBodyTypes = BodyTypes.getListOfBodyTypesAsString()
    private var selectedBodyType = mutableStateOf(BodyTypes.PLAIN_TEXT)


    fun getCurrentMode() = currentMode
    fun getSelectedHttpMethod() = selectedHttpMethod
    fun getUrl() = url
    fun getRequestType() = requestType
    fun getSelectedBodyType() = selectedBodyType


    fun setCurrentMode(mode: SettingsEnums) {
        if (!possibleModes.contains(mode)) {
            currentMode.value = SettingsEnums.READ_MODE
            return
        }

        currentMode.value = mode
    }

    fun setSelectedHttpMethod(method: String) {
        if (!possibleHttpMethods.contains(HttpEnum.fromString(method))) {
            selectedHttpMethod.value = HttpEnum.GET
            return
        }

        selectedHttpMethod.value = HttpEnum.fromString(method)
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

    fun setSelectedBodyType(bodyType: String) {
        if (!possibleBodyTypes.contains(bodyType)) {
            selectedBodyType.value = BodyTypes.PLAIN_TEXT
            return
        }

        selectedBodyType.value = BodyTypes.fromString(bodyType)
    }
}
