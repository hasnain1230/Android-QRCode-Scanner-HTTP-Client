package com.jscj.qrcodescanner

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jscj.qrcodescanner.util.Helper

@Composable
fun AboutAlertIconButton(modifier: Modifier = Modifier) {
    val showDialog = remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            showDialog.value = true
        },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.twotone_info_24),
            contentDescription = stringResource(R.string.about_the_developer),
            tint = Color.White
        )
    }

    AboutAlertDialog(showDialog)
}

@Composable
fun AboutAlertDialog(showDialog: MutableState<Boolean>, context: Context = LocalContext.current) {
    val clipBoardManager: androidx.compose.ui.platform.ClipboardManager =
        LocalClipboardManager.current
    val emailAddress: String = stringResource(id = R.string.email_address)

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = {
                Text(
                    text = "About",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.application_credits),
                        style = TextStyle(fontSize = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.report_bugs),
                        style = TextStyle(fontSize = 16.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.hiring_message),
                        style = TextStyle(fontSize = 16.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = emailAddress,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(android.graphics.Color.parseColor(stringResource(R.string.email_address_link_color))),
                        ),
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        clipBoardManager.setText(AnnotatedString(emailAddress))
                                        Toast.makeText(
                                            context,
                                            "Email address copied to clipboard",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onTap = {
                                        Helper.emailAddress(
                                            context = context,
                                            emailAddress = emailAddress
                                        )
                                    }
                                )
                            }
                    )


                    Spacer(modifier = Modifier.height(16.dp))

                    Divider()

                    TextButton(
                        onClick = {
                            Helper.openUrl(
                                context = context,
                                url = context.getString(R.string.github_url)
                            )
                        },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            text = "GitHub",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color(android.graphics.Color.parseColor(stringResource(id = R.string.link_hex_color_code)))
                            ),
                        )
                    }
                    TextButton(
                        onClick = {
                            Helper.openUrl(
                                context = context,
                                url = context.getString(R.string.linkedin_url)
                            )
                        },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            text = "LinkedIn",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color(android.graphics.Color.parseColor(stringResource(id = R.string.link_hex_color_code)))
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDialog.value = false }
                ) {
                    Text(
                        text = "Okay",
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        )
    }
}