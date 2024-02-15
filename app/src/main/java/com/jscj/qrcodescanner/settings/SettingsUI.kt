package com.jscj.qrcodescanner.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jscj.qrcodescanner.R
import com.jscj.qrcodescanner.http.BodyTypes
import com.jscj.qrcodescanner.http.HttpEnum

class SettingsUI(private val settingsViewModel: SettingsViewModel) {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen(onNavigateBack: () -> Unit) {
        var showUrlEmptyDialog by remember { mutableStateOf(false) }

        if (settingsViewModel.showUrlEmptyDialog.value) {
            ShowURLEmptyDialog(
                showDialog = settingsViewModel.showUrlEmptyDialog.value,
                onDismiss = { settingsViewModel.dismissDialog() }
            )
        }

        // Modify the navigation logic
        val modifiedOnNavigateBack = {
            if (settingsViewModel.getCurrentMode().value == SettingsEnums.HTTP_MODE && settingsViewModel.getUrl().value.isEmpty()) {
                settingsViewModel.showDialog()
            } else {
                onNavigateBack()
            }
        }

        MaterialTheme(
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes
        )  {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.settings_title)) },
                        navigationIcon = {
                            IconButton(onClick = modifiedOnNavigateBack) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back_button_content_descritption)
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
                    Text(stringResource(R.string.select_mode_text))
                    Spacer(
                        Modifier
                            .width(16.dp)
                            .padding(8.dp))

                    ModeSelectionRow(currentMode = settingsViewModel.getCurrentMode().value, onModeChange = { settingsViewModel.setCurrentMode(it) })

                    if (showUrlEmptyDialog) {
                        ShowURLEmptyDialog(
                            showDialog = showUrlEmptyDialog,
                            onDismiss = { showUrlEmptyDialog = false }
                        )
                    }

                    if (settingsViewModel.getCurrentMode().value == SettingsEnums.HTTP_MODE) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.select_http_method_text))
                        Dropdown(selectedItem = settingsViewModel.getSelectedHttpMethod().value.toString(), HttpEnum.getListOfHttpMethodsAsString(), onMethodSelected = { settingsViewModel.setSelectedHttpMethod(it) })
                        Spacer(modifier = Modifier.height(16.dp))
                        UrlInputField(url = settingsViewModel.getUrl().value, onUrlChange = { settingsViewModel.setUrl(it) })
                        Spacer(modifier = Modifier.height(16.dp))
                        RequestTypeRadioButtons(requestType = settingsViewModel.getRequestType().value, onRequestTypeChange = { settingsViewModel.setRequestType(it) })

                        if (settingsViewModel.getRequestType().value == SettingsEnums.BODY_REQUEST) {
                            // JSON, XML, Plain Text
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Select Body Type")
                            Dropdown(selectedItem = settingsViewModel.getSelectedBodyType().value.toString(), BodyTypes.getListOfBodyTypesAsString(), onMethodSelected = { settingsViewModel.setSelectedBodyType(it) })

                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ModeSelectionRow(currentMode: SettingsEnums, onModeChange: (SettingsEnums) -> Unit) {
        Row {
            RadioButton(
                selected = currentMode == SettingsEnums.READ_MODE,
                onClick = { onModeChange(SettingsEnums.READ_MODE) }
            )

            Spacer(Modifier.width(8.dp))
            Text(SettingsEnums.READ_MODE.toString())

            RadioButton(
                selected = currentMode == SettingsEnums.HTTP_MODE,
                onClick = { onModeChange(SettingsEnums.HTTP_MODE) }
            )

            Text(SettingsEnums.HTTP_MODE.toString())
        }
    }

    @Composable
    fun ShowURLEmptyDialog(showDialog: Boolean, onDismiss: () -> Unit) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    Button(onClick = onDismiss) {
                        Text("Okay")
                    }
                },
                title = { Text("URL Required") },
                text = { Text("Please enter a URL first, or select \"Read Mode.\"") }
            )
        }
    }



    @Composable
    fun Dropdown(selectedItem: String, dropDownItems: List<String>, onMethodSelected: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }

        Column {
            Box {
                Button(onClick = { expanded = true }) {
                    Text(text = selectedItem)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    dropDownItems.forEach { method ->
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
            Text(stringResource(R.string.enter_url_text))
            TextField(
                value = url,
                onValueChange = onUrlChange,
                label = { Text(stringResource(R.string.url_label_text)) }
            )
        }
    }

    @Composable
    fun RequestTypeRadioButtons(requestType: SettingsEnums, onRequestTypeChange: (SettingsEnums) -> Unit) {
        Column {
            Text(stringResource(R.string.request_type_text))
            Row {
                RadioButton(
                    selected = requestType == SettingsEnums.CONCATENATE,
                    onClick = { onRequestTypeChange(SettingsEnums.CONCATENATE) }
                )
                Text(SettingsEnums.CONCATENATE.toString())
                Spacer(Modifier.width(8.dp))
                RadioButton(
                    selected = requestType == SettingsEnums.BODY_REQUEST,
                    onClick = {
                        onRequestTypeChange(SettingsEnums.BODY_REQUEST)
                    }
                )

                Text(SettingsEnums.BODY_REQUEST.toString())
            }
        }
    }

}
