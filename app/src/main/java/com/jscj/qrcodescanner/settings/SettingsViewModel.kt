package com.jscj.qrcodescanner.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jscj.qrcodescanner.http.BodyTypes
import com.jscj.qrcodescanner.http.HttpEnum
import com.jscj.qrcodescanner.savedlinks.SavedLinksViewModel

// TODO: Hasnain, this entire class needs to be refactored... Thinking too much like Java and not enough like Kotlin

class SettingsViewModel(context: Context, savedLinksViewModel: SavedLinksViewModel) : ViewModel() {
    private val _settingsSavedPreferences: SharedPreferences = context.getSharedPreferences(
        "settings",
        Context.MODE_PRIVATE
    ) // Private mode because we don't want other apps to access this data

    private val _savedLinksViewModel = savedLinksViewModel

    private val _showUrlEmptyDialog = mutableStateOf(false)
    private var _savedConfigurations = mutableStateOf<Map<String, Config>>(mapOf())
    private val possibleModes = SettingsEnums.getPossibleModes()
    private var currentMode = mutableStateOf(SettingsEnums.READ_MODE)
    private val possibleHttpMethods = HttpEnum.getListOfHttpMethods()
    private var selectedHttpMethod = mutableStateOf(HttpEnum.GET)
    private var url = mutableStateOf("")
    private val possibleRequestTypes = SettingsEnums.getPossibleRequestTypes()
    private var requestType = mutableStateOf(SettingsEnums.CONCATENATE)
    private var possibleBodyTypes = BodyTypes.getListOfBodyTypesAsString()
    private var selectedBodyType = mutableStateOf(BodyTypes.PLAIN_TEXT)

    val showUrlEmptyDialog: State<Boolean> = _showUrlEmptyDialog

    init {
        loadSettings()
        loadConfigurations()
    }

    // Getters
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
        _settingsSavedPreferences.edit().putString("mode", mode.toString()).apply()
    }

    fun setSelectedHttpMethod(method: String) {
        if (!possibleHttpMethods.contains(HttpEnum.fromString(method))) {
            selectedHttpMethod.value = HttpEnum.GET
            return
        }

        selectedHttpMethod.value = HttpEnum.fromString(method)
        _settingsSavedPreferences.edit().putString("httpMethod", method).apply()
    }

    fun isValidUrl(): Boolean {
        return url.value.isNotEmpty() && url.value.isNotBlank()
    }

    fun setUrl(url: String) {
        this.url.value = url
        _settingsSavedPreferences.edit().putString("url", url).apply()
    }

    fun setRequestType(requestType: SettingsEnums) {
        if (!possibleRequestTypes.contains(requestType)) {
            this.requestType.value = SettingsEnums.CONCATENATE
            return
        }

        this.requestType.value = requestType
        _settingsSavedPreferences.edit().putString("requestType", requestType.toString()).apply()
    }

    fun setSelectedBodyType(bodyType: String) {
        if (!possibleBodyTypes.contains(bodyType)) {
            selectedBodyType.value = BodyTypes.PLAIN_TEXT
            return
        }

        selectedBodyType.value = BodyTypes.fromString(bodyType)
        _settingsSavedPreferences.edit().putString("bodyType", bodyType).apply()
    }

    fun getAllowedRequestTypes(): List<SettingsEnums> {
        return if (selectedHttpMethod.value == HttpEnum.GET) {
            listOf(SettingsEnums.CONCATENATE)
        } else {
            possibleRequestTypes
        }
    }

    fun showDialog() {
        _showUrlEmptyDialog.value = true
    }

    fun dismissDialog() {
        _showUrlEmptyDialog.value = false
    }

    private fun saveConfigurationsMap(key: String, map: Map<String, Config>) {
        val gson = Gson()
        val json = gson.toJson(map)
        _settingsSavedPreferences.edit().putString(key, json).apply()
    }

    private fun loadConfigurationsMap(key: String): Map<String, Config>? {
        val gson = Gson()
        val json = _settingsSavedPreferences.getString(key, null) //
        val type = object :
            TypeToken<Map<String, Config>>() {}.type // Create an anonymous object that extends TypeToken of Map<String, Config>. Then we get the type of that object
        return gson.fromJson(json, type)
    }

    fun saveConfiguration(name: String): Boolean {
        val config = Config(
            _configName = name,
            currentMode = currentMode.value,
            selectedHttpMethod = selectedHttpMethod.value,
            url = url.value,
            requestType = requestType.value,
            bodyTypes = selectedBodyType.value,
            savedLinks = _savedLinksViewModel.savedLinks.value
        )

        // First check if the configuration already exists
        return if (_savedConfigurations.value.containsKey(name)) {
            false
        } else {
            _savedConfigurations.value += name to config
            saveConfigurationsMap("configurations", _savedConfigurations.value)
            true
        }
    }

    fun getSavedConfigurations(): Map<String, Config> {
        return _savedConfigurations.value
    }

    fun deleteConfiguration(configName: String) {
        _savedConfigurations.value -= configName
        saveConfigurationsMap("configurations", _savedConfigurations.value)
    }

    fun deleteConfigurations(configNames: List<String>) {
        configNames.forEach { configName ->
            _savedConfigurations.value -= configName
        }

        saveConfigurationsMap("configurations", _savedConfigurations.value)
    }

    fun loadConfiguration(configName: String) {
        val config = _savedConfigurations.value[configName]
        if (config != null) {
            setCurrentMode(config.currentMode)
            setSelectedHttpMethod(config.selectedHttpMethod.toString())
            setUrl(config.url)
            setRequestType(config.requestType)
            setSelectedBodyType(config.bodyTypes.toString())
            _savedLinksViewModel.setLinks(config.savedLinks.orEmpty())
        }
    }

    private fun loadSettings() {
        val mode: String? = _settingsSavedPreferences.getString("mode", "Read Mode")
        val httpMethod: String? = _settingsSavedPreferences.getString("httpMethod", null)
        val url: String? = _settingsSavedPreferences.getString("url", null)
        val requestType: String? = _settingsSavedPreferences.getString("requestType", null)
        val bodyType: String? = _settingsSavedPreferences.getString("bodyType", null)

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

    private fun loadConfigurations() {
        val configurations = loadConfigurationsMap("configurations")
        if (!configurations.isNullOrEmpty()) {
            _savedConfigurations.value = configurations
        }
    }
}
