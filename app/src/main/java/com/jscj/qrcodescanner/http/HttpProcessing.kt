package com.jscj.qrcodescanner.http

import com.jscj.qrcodescanner.settings.SettingsEnums
import com.jscj.qrcodescanner.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class HttpProcessing {
    companion object {
        private fun buildRequestBody(bodyType: BodyTypes, qrCodeData: String): RequestBody {
            val mediaType = when (bodyType) {
                BodyTypes.JSON -> "application/json; charset=utf-8"
                BodyTypes.XML -> "application/xml; charset=utf-8"
                BodyTypes.PLAIN_TEXT -> "text/plain; charset=utf-8"
            }.toMediaTypeOrNull()
            return qrCodeData.toRequestBody(mediaType)
        }

        private fun buildRequest(
            settings: SettingsViewModel,
            qrCodeData: String,
            method: HttpEnum
        ): Request {
            val url = if (settings.getRequestType().value == SettingsEnums.CONCATENATE) {
                settings.getUrl().value.plus(qrCodeData)
            } else {
                settings.getUrl().value
            }

            val requestBody = if (settings.getRequestType().value == SettingsEnums.BODY_REQUEST) {
                buildRequestBody(settings.getSelectedBodyType().value, qrCodeData)
            } else {
                null
            }

            // Print request body data


            return Request.Builder().url(url).also { builder ->
                when (method) {
                    HttpEnum.GET -> builder.get()
                    HttpEnum.POST -> builder.post(requestBody ?: "".toRequestBody())
                    HttpEnum.PUT -> builder.put(requestBody ?: "".toRequestBody())
                    HttpEnum.DELETE -> builder.delete(requestBody ?: "".toRequestBody())
                    HttpEnum.PATCH -> builder.patch(requestBody ?: "".toRequestBody())
                }
            }.build()
        }

        suspend fun processHttp(
            settings: SettingsViewModel,
            qrCodeData: String
        ): Pair<Int, String?> {
            return withContext(Dispatchers.IO) {
                val method: HttpEnum = settings.getSelectedHttpMethod().value
                val request: Request = buildRequest(settings, qrCodeData, method)
                val httpClientResponse: String?
                val httpClientResponseCode: Int

                OkHttpClient().newCall(request).execute().use { response ->
                    httpClientResponseCode = response.code
                    httpClientResponse = response.body?.string()
                }

                Pair(httpClientResponseCode, httpClientResponse)
            }
        }
    }
}
