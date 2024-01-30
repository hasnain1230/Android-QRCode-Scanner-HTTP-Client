package com.jscj.qrcodescanner.qrcode

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.jscj.qrcodescanner.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QRCodeViews {
    // Function to open URL in browser
    private fun openUrlInBrowser(url: String, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    @Composable
    fun ShowQRCodeDataPopup(
        qrCodeData: MutableState<String?>,
        context: Context,
        titleText: String,
        bodyText: String
    ) {
        val showDialog = remember { mutableStateOf(true) }

        // Function to open URL
        fun openUrl(url: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text(titleText, color = Color.White) }, // Set title color
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
                                    color = Color.Blue, // Color for the link
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
                                        openUrl(annotation.item)
                                    }
                            }
                        )
                    } else {
                        Text(bodyText, color = Color.White) // Set normal text color
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showDialog.value = false
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            qrCodeData.value = null
                        }
                    }) {
                        Text(context.getString(R.string.okay_button_text), color = Color.White) // Set button text color
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