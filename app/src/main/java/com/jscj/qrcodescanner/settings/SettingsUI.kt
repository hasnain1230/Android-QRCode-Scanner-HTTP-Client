package com.jscj.qrcodescanner.settings

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class SettingsUI {
    private val viewModel = SettingsController()
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen(onNavigateBack: () -> Unit) {
        val darkTheme = isSystemInDarkTheme() // Will implement this later

        MaterialTheme(
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes
        )  {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Settings") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .padding(16.dp)
                ) {
                    Text("Select Mode")
                    Spacer(
                        Modifier
                            .width(16.dp)
                            .padding(8.dp))

                    ModeSelectionRow(currentMode = viewModel.getCurrentMode().value, onModeChange = { viewModel.setCurrentMode(it) })


                    if (viewModel.getCurrentMode().value == "http_mode") {
                        Spacer(modifier = Modifier.height(16.dp))
                        HttpMethodDropdown(selectedHttpMethod = viewModel.getSelectedHttpMethod().value, onMethodSelected = { viewModel.setSelectedHttpMethod(it) })
                        Spacer(modifier = Modifier.height(16.dp))
                        UrlInputField(url = viewModel.getUrl().value, onUrlChange = { viewModel.setUrl(it) })
                        Spacer(modifier = Modifier.height(16.dp))
                        RequestTypeRadioButtons(requestType = viewModel.getRequestType().value, onRequestTypeChange = { viewModel.setRequestType(it) })
                    }
                }
            }
        }
    }

    @Composable
    fun ModeSelectionRow(currentMode: String, onModeChange: (String) -> Unit) {
        Row {
            RadioButton(
                selected = currentMode == "read_mode",
                onClick = { onModeChange("read_mode") }
            )

            Spacer(Modifier.width(8.dp))
            Text("Read Mode")

            RadioButton(
                selected = currentMode == "http_mode",
                onClick = { onModeChange("http_mode") }
            )
            Text("HTTP Mode")
        }
    }

    @Composable
    fun HttpMethodDropdown(selectedHttpMethod: String, onMethodSelected: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        val httpMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE")
        val interactionSource = remember { MutableInteractionSource() } // Add this line

        Column {
            Text("Select HTTP Method")
            Box {
                Button(onClick = { expanded = true }) {
                    Text(text = selectedHttpMethod)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    httpMethods.forEach { method ->
                        DropdownMenuItem(
                            onClick = {
                                onMethodSelected(method)
                                expanded = false
                            },
                            text = {
                                Text(text = method)
                            }
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun UrlInputField(url: String, onUrlChange: (String) -> Unit) {
        Column {
            Text("Enter URL")
            TextField(
                value = url,
                onValueChange = onUrlChange,
                label = { Text("URL") }
            )
        }
    }

    @Composable
    fun RequestTypeRadioButtons(requestType: String, onRequestTypeChange: (String) -> Unit) {
        Column {
            Text("Request Type")
            Row {
                RadioButton(
                    selected = requestType == "concatenate",
                    onClick = { onRequestTypeChange("concatenate") }
                )
                Text("Concatenate")
                Spacer(Modifier.width(8.dp))
                RadioButton(
                    selected = requestType == "body_request",
                    onClick = { onRequestTypeChange("body_request") }
                )
                Text("Body Request")
            }
        }
    }

}
