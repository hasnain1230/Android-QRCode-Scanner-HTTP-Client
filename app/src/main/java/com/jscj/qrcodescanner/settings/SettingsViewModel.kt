package com.jscj.qrcodescanner.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.jscj.qrcodescanner.http.BodyTypes
import com.jscj.qrcodescanner.http.HttpEnum

class SettingsViewModel(context: Context) : ViewModel() {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE) // Private mode because we don't want other apps to access this data

    private val possibleModes = SettingsEnums.getPossibleModes()
    private var currentMode = mutableStateOf(SettingsEnums.READ_MODE)
    private val possibleHttpMethods = HttpEnum.getListOfHttpMethods()
    private var selectedHttpMethod = mutableStateOf(HttpEnum.GET)
    private var url = mutableStateOf("")
    private val possibleRequestTypes = SettingsEnums.getPossibleRequestTypes()
    private val _showUrlEmptyDialog = mutableStateOf(false)
    private var requestType = mutableStateOf(SettingsEnums.CONCATENATE)
    private var possibleBodyTypes = BodyTypes.getListOfBodyTypesAsString()
    private var selectedBodyType = mutableStateOf(BodyTypes.PLAIN_TEXT)

    val showUrlEmptyDialog: State<Boolean> = _showUrlEmptyDialog

    init {
        loadSettings()
    }

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
        sharedPreferences.edit().putString("mode", mode.toString()).apply()
    }

    fun setSelectedHttpMethod(method: String) {
        if (!possibleHttpMethods.contains(HttpEnum.fromString(method))) {
            selectedHttpMethod.value = HttpEnum.GET
            return
        }

        selectedHttpMethod.value = HttpEnum.fromString(method)
        sharedPreferences.edit().putString("httpMethod", method).apply()
    }

    fun setUrl(url: String) {
        this.url.value = url
        sharedPreferences.edit().putString("url", url).apply()
    }

    fun setRequestType(requestType: SettingsEnums) {
        if (!possibleRequestTypes.contains(requestType)) {
            this.requestType.value = SettingsEnums.CONCATENATE
            return
        }

        this.requestType.value = requestType
        sharedPreferences.edit().putString("requestType", requestType.toString()).apply()
    }

    fun setSelectedBodyType(bodyType: String) {
        if (!possibleBodyTypes.contains(bodyType)) {
            selectedBodyType.value = BodyTypes.PLAIN_TEXT
            return
        }

        selectedBodyType.value = BodyTypes.fromString(bodyType)
        sharedPreferences.edit().putString("bodyType", bodyType).apply()
    }

    fun showDialog() {
        _showUrlEmptyDialog.value = true
    }

    fun dismissDialog() {
        _showUrlEmptyDialog.value = false
    }

    private fun loadSettings() {
        val mode: String? = sharedPreferences.getString("mode", "Read Mode")
        val httpMethod: String? = sharedPreferences.getString("httpMethod", null)
        val url: String? = sharedPreferences.getString("url", null)
        val requestType: String? = sharedPreferences.getString("requestType", null)
        val bodyType: String? = sharedPreferences.getString("bodyType", null)

        setCurrentMode(SettingsEnums.fromString(mode!!))

        if (httpMethod != null) {
            setSelectedHttpMethod(httpMethod)
        }

        if (url != null) {
            setUrl(url)
        }

        if (requestType != null) {
            setRequestType(SettingsEnums.fromString(requestType))
        }

        if (bodyType != null) {
            setSelectedBodyType(bodyType)
        }
    }
}
