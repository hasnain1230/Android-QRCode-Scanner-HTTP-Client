package com.jscj.qrcodescanner.qrcode

import android.content.Context
import android.graphics.Rect
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.jscj.qrcodescanner.R
import com.jscj.qrcodescanner.http.HttpProcessing
import com.jscj.qrcodescanner.settings.SettingsEnums
import com.jscj.qrcodescanner.settings.SettingsViewModel
import com.jscj.qrcodescanner.util.Constants
import com.jscj.qrcodescanner.util.Helper

class QRCodeViews {
    companion object {
        var dialogsShown: Int = 0
    }


    @Composable
    fun ShowQRCodeDataPopup(
        qrCodeData: MutableState<String?>,
        context: Context,
        titleText: String,
        titleColor: Color,
        bodyText: String,
        showDialog: MutableState<Boolean>,
        qrCodeBounds: MutableState<Rect?>,
        rebindCamera: () -> Unit,
        success: Boolean
    ) {
        if (dialogsShown == 0) {
            val soundToPlay = if (success) Constants.QR_CODE_BEEP else Constants.ERROR_BEEP
            Helper.playSound(context, soundToPlay)
        }

        if (showDialog.value) {
            dialogsShown++

            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                    qrCodeData.value = null
                    qrCodeBounds.value = null
                    dialogsShown = 0
                },
                title = {
                    Text(text = titleText, color = titleColor)
                },
                text = {
                    // Check if bodyText contains a URL
                    val url = Patterns.WEB_URL.matcher(bodyText).takeIf { it.find() }?.group()
                    if (url != null) {
                        val annotatedText = buildAnnotatedString {
                            val startIndex = bodyText.indexOf(url)
                            val endIndex = startIndex + url.length
                            append(bodyText)
                            addStringAnnotation(
                                tag = "URL",
                                annotation = url,
                                start = startIndex,
                                end = endIndex
                            )
                            addStyle(
                                style = SpanStyle(
                                    color = Color(
                                        android.graphics.Color.parseColor(
                                            stringResource(
                                                id = R.string.link_hex_color_code
                                            )
                                        )
                                    ),
                                    textDecoration = TextDecoration.Underline
                                ),
                                start = startIndex,
                                end = endIndex
                            )

                            addStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                start = 0,
                                end = startIndex
                            )

                            addStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                start = endIndex,
                                end = bodyText.length
                            )
                        }
                        ClickableText(
                            text = annotatedText,
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(
                                    "URL",
                                    start = offset, // Find URLs at the specific character the user clicked (in this case, they will always be within the URL)
                                    end = offset
                                ).firstOrNull()?.let { annotation ->
                                    Helper.openUrl(context, annotation.item)
                                }
                            }
                        )
                    } else {
                        Text(
                            text = bodyText,
                            color = MaterialTheme.colorScheme.onBackground
                        ) // Set normal text color
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showDialog.value = false
                        qrCodeData.value = null
                        qrCodeBounds.value = null
                        dialogsShown = 0

                        rebindCamera.invoke()
                    }) {
                        Text(context.getString(R.string.okay_button_text)) // Set button text color
                    }
                },
                shape = RoundedCornerShape(size = 12.dp),
                modifier = Modifier
                    .padding(6.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        AbsoluteRoundedCornerShape(12.dp)
                    )
            )
        }
    }

    @Composable
    fun handleQRCodeData(
        qrCodeData: String,
        settingsViewModel: SettingsViewModel
    ): MutableMap<String, MutableState<*>> {
        val titleText: MutableState<String> = remember { mutableStateOf("") }
        val bodyText: MutableState<String?> = remember { mutableStateOf("") }
        val showPopup: MutableState<Boolean> = remember { mutableStateOf(false) }
        val defaultColor: Color = MaterialTheme.colorScheme.onBackground
        val titleColor: MutableState<Color> = remember { mutableStateOf(defaultColor) }
        val success: MutableState<Boolean> = remember { mutableStateOf(false) }

        settingsViewModel.getCurrentMode().value.let { mode ->
            if (mode == SettingsEnums.READ_MODE) {
                titleText.value = "Scanned QR Code"
                bodyText.value = qrCodeData
                showPopup.value = true
                success.value = true
            } else if (mode == SettingsEnums.HTTP_MODE) {
                LaunchedEffect(qrCodeData) {
                    val result: Pair<Int, String?> = try {
                        HttpProcessing.processHttp(
                            settings = settingsViewModel,
                            qrCodeData = qrCodeData
                        )
                    } catch (e: Exception) {
                        Pair(-1, e.message)
                    }

                    val responseCode = result.first
                    val responseBody = result.second

                    if (responseCode in 200..299) {
                        titleText.value =
                            "${settingsViewModel.getSelectedHttpMethod().value} Request Successful"
                        titleColor.value = Color.Green
                        success.value = true
                    } else {
                        titleText.value =
                            "Error - ${settingsViewModel.getSelectedHttpMethod().value} Request Failed"
                        titleColor.value = Color.Red
                        success.value = false
                    }



                    bodyText.value =
                        if (responseCode in 200..299 && settingsViewModel.getRequestType().value == SettingsEnums.CONCATENATE) {
                            "Successfully sent ${settingsViewModel.getSelectedHttpMethod().value} request to ${
                                settingsViewModel.getUrl().value.plus(
                                    qrCodeData
                                )
                            }\n\nResponse Code: $responseCode\n\n"
                        } else if (responseCode in 200..299 && settingsViewModel.getRequestType().value == SettingsEnums.BODY_REQUEST) {
                            "Successfully sent ${settingsViewModel.getSelectedHttpMethod().value} request to ${settingsViewModel.getUrl().value}\n\nResponse Code: $responseCode\n\nResponse Body: $responseBody"
                        } else {
                            "There was an error sending the ${settingsViewModel.getSelectedHttpMethod().value} request to ${settingsViewModel.getUrl().value}\n\nResponse Code: $responseCode\n\nResponse Body: $responseBody"
                        }

                    showPopup.value = true
                }
            }
        }

        // Return hash map of title and body text
        return mutableMapOf(
            "titleText" to titleText,
            "bodyText" to bodyText,
            "showPopup" to showPopup,
            "titleColor" to titleColor,
            "success" to success
        )
    }

}