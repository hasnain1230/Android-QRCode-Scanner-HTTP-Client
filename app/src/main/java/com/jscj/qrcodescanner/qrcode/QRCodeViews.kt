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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.jscj.qrcodescanner.Helper
import com.jscj.qrcodescanner.R

class QRCodeViews {
    companion object {
        var dialogsShown: Int = 0
    }


    @Composable
    fun ShowQRCodeDataPopup(
        qrCodeData: MutableState<String?>,
        context: Context,
        titleText: String,
        bodyText: String,
        showDialog: MutableState<Boolean>,
        qrCodeBounds: MutableState<Rect?>,
        rebindCamera: () -> Unit,
    ) {
        if (dialogsShown == 0) {
            Helper.playSound(context, com.google.zxing.client.android.R.raw.zxing_beep)
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
                    Text(titleText)
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
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                ),
                                start = startIndex,
                                end = endIndex
                            )
                        }
                        ClickableText(
                            text = annotatedText,
                            style = TextStyle(color = MaterialTheme.colorScheme.primary), // Set normal text color
                            onClick = { offset ->
                                annotatedText.getStringAnnotations("URL", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        Helper.openUrl(context, annotation.item)
                                    }
                            }
                        )
                    } else {
                        Text(bodyText) // Set normal text color
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
                    .background(MaterialTheme.colorScheme.surface, AbsoluteRoundedCornerShape(12.dp))
            )
        }
    }

}