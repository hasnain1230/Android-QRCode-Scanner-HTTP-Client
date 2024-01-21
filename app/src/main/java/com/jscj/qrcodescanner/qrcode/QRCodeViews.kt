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
    fun ShowQRCodeDataPopup(qrCodeData: MutableState<String?>, context: Context) {
        if (qrCodeData.value == null) {
            return
        }

        val showDialog = remember { mutableStateOf(true) }


        // Check if the QR code data is a URL
        val isUrl = qrCodeData.value?.let { Patterns.WEB_URL.matcher(it).matches() }

        println("QR Code Views $qrCodeData")

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text(context.getString(R.string.scanned_qr_code)) },
                text = {
                    if (isUrl == true) {
                        val colorHex = Color(android.graphics.Color.parseColor(context.getString(R.string.link_hex_color_code)))
                        val annotatedText = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = colorHex, textDecoration = TextDecoration.Underline)) {
                                append(qrCodeData.value)
                            }
                        }
                        ClickableText(
                            text = annotatedText,
                            onClick = { openUrlInBrowser(qrCodeData.value!!, context) },
                        )
                    } else {
                        Text(qrCodeData.value!!)
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
                        Text(context.getString(R.string.okay_button_text))
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