package com.jscj.qrcodescanner.http

import com.jscj.qrcodescanner.settings.SettingsEnums
import com.jscj.qrcodescanner.settings.SettingsViewModel
import okhttp3.OkHttpClient
import okhttp3.Request

class HttpProcessing {
    companion object {
        fun processHttp(settings: SettingsViewModel, qrCodeData: String) {
            val method: HttpEnum = settings.getSelectedHttpMethod().value
            val url: String = settings.getUrl().value
            val requestType: SettingsEnums = settings.getRequestType().value

            if (requestType == SettingsEnums.CONCATENATE) {
                url.plus(qrCodeData)
            }

            val httpClient: OkHttpClient = OkHttpClient()
            var httpClientResponse: String? = null
            var httpClientResponseCode: Int? = null

            when (method) {
                HttpEnum.GET -> {
                    if (requestType == SettingsEnums.BODY_REQUEST) {
                        val request = Request.Builder()
                            .url(url)
                            .method("GET", qrCodeData.toRequestBody())
                            .build()


                        httpClient.newCall(request).execute().use { response ->
                            httpClientResponse = response.body?.string()
                            httpClientResponseCode = response.code
                        }
                    }
                }
            }
        }
    }
}