package com.jscj.qrcodescanner.settings

import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.jscj.qrcodescanner.R
import com.jscj.qrcodescanner.http.BodyTypes
import com.jscj.qrcodescanner.http.HttpEnum
import kotlin.math.abs


// TODO: Hasnain, this entire UI needs to be rewritten using Preferences and Jetpack DataStore
class SettingsUI(private val settingsViewModel: SettingsViewModel) {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen(onNavigateBack: () -> Unit) {
        var showUrlEmptyDialog by remember { mutableStateOf(false) }
        val settingsScreenContext = LocalContext.current

        if (settingsViewModel.showUrlEmptyDialog.value) {
            ShowURLEmptyDialog(
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
        ) {
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
                        },
                        actions = {
                            val showConfigurationsDialog = remember { mutableStateOf(false) }

                            IconButton(onClick = {
                                showConfigurationsDialog.value = true
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.twotone_view_list_24),
                                    contentDescription = "View Configurations"
                                )
                            }

                            if (showConfigurationsDialog.value) {
                                ShowConfigurationsDialog(
                                    configurations = settingsViewModel.getSavedConfigurations(),
                                    onDismiss = { showConfigurationsDialog.value = false }
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    if (settingsViewModel.getCurrentMode().value == SettingsEnums.READ_MODE) {
                        return@Scaffold
                    }

                    val showDialog = remember { mutableStateOf(false) }
                    FloatingActionButton(onClick = {
                        if (!settingsViewModel.isValidUrl()) {
                            Toast.makeText(
                                settingsScreenContext,
                                "URL is required",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showDialog.value = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.twotone_save_24),
                            contentDescription = "Save"
                        )

                        if (showDialog.value) {
                            SaveConfigDialog(
                                onSave = {
                                    if (settingsViewModel.saveConfiguration(it)) {
                                        Toast.makeText(
                                            settingsScreenContext,
                                            "Configuration saved",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showDialog.value = false
                                    } else {
                                        Toast.makeText(
                                            settingsScreenContext,
                                            "Configuration with that name already exists",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onDismiss = { showDialog.value = false }
                            )
                        }
                    }
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
                            .padding(8.dp)
                    )

                    ModeSelectionRow(
                        currentMode = settingsViewModel.getCurrentMode().value,
                        onModeChange = { settingsViewModel.setCurrentMode(it) })

                    if (showUrlEmptyDialog) {
                        ShowURLEmptyDialog(
                            onDismiss = { showUrlEmptyDialog = false }
                        )
                    }

                    if (settingsViewModel.getCurrentMode().value == SettingsEnums.HTTP_MODE) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.select_http_method_text))
                        Dropdown(
                            selectedItem = settingsViewModel.getSelectedHttpMethod().value.toString(),
                            HttpEnum.getListOfHttpMethodsAsString(),
                            onMethodSelected = { settingsViewModel.setSelectedHttpMethod(it) })
                        Spacer(modifier = Modifier.height(16.dp))
                        UrlInputField(
                            url = settingsViewModel.getUrl().value,
                            onUrlChange = { settingsViewModel.setUrl(it) })
                        Spacer(modifier = Modifier.height(16.dp))

                        RequestTypeRadioButtons(requestType = settingsViewModel.getRequestType().value,
                            requestTypeList = settingsViewModel.getAllowedRequestTypes(),
                            onRequestTypeChange = { settingsViewModel.setRequestType(it) })

                        if (settingsViewModel.getSelectedHttpMethod().value != HttpEnum.GET &&
                            settingsViewModel.getRequestType().value == SettingsEnums.BODY_REQUEST
                        ) {
                            // JSON, XML, Plain Text
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.select_body_type))
                            Dropdown(
                                selectedItem = settingsViewModel.getSelectedBodyType().value.toString(),
                                BodyTypes.getListOfBodyTypesAsString(),
                                onMethodSelected = { settingsViewModel.setSelectedBodyType(it) })

                        } else {
                            settingsViewModel.setRequestType(SettingsEnums.CONCATENATE)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SaveConfigDialog(onSave: (String) -> Unit, onDismiss: () -> Unit) {
        var configName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Name Your Configuration") },
            text = {
                Column {
                    Text("Please the name you would like to use for your current configuration.")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = configName,
                        onValueChange = { configName = it },
                        placeholder = { Text("Configuration Name") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSave(configName.trim())
                        configName = "" // Reset the text field after saving
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
            }
        )
    }

    @Composable
    fun ShowConfigurationsDialog(configurations: Map<String, Config>, onDismiss: () -> Unit) {
        val configurationEntries = configurations.entries.toList()

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Saved Configurations") },
            text = {
                LazyColumn {
                    items(
                        count = configurationEntries.size,
                        itemContent = { index ->
                            val entry = configurationEntries[index]
                            ConfigurationItem(
                                name = entry.key,
                                onClick = {
                                    settingsViewModel.loadConfiguration(entry.key)
                                    onDismiss()
                                },
                                onDelete = {
                                    settingsViewModel.deleteConfiguration(entry.key)
                                }
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { onDismiss() }) {
                    Text("Close")
                }
            }
        )
    }


    @Composable
    fun ConfigurationItem(name: String, onClick: () -> Unit, onDelete: () -> Unit) {
        var showMenu by remember { mutableStateOf(false) }
        var dropDownMenuPosition by remember { mutableStateOf(DpOffset.Zero) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { offset ->
                            val xPos = abs((offset.x.toDp()).value).toDp()
                            dropDownMenuPosition = DpOffset(xPos, 0.dp)
                            showMenu = true
                        },
                        onTap = {
                            onClick()
                        }
                    )
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = {
                showMenu = false
            },
            offset = dropDownMenuPosition
        ) {
            DropdownMenuItem(
                onClick = {
                    onDelete()
                    showMenu = false
                },
                text = {
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Configuration")
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.twotone_delete_forever_24), // Replace with your delete icon asset
                        contentDescription = "Delete Configuration",
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
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
    fun ShowURLEmptyDialog(onDismiss: () -> Unit) {
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


    @Composable
    fun Dropdown(
        selectedItem: String,
        dropDownItems: List<String>,
        onMethodSelected: (String) -> Unit
    ) {
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
    fun RequestTypeRadioButtons(
        requestType: SettingsEnums,
        requestTypeList: List<SettingsEnums>,
        onRequestTypeChange: (SettingsEnums) -> Unit
    ) {
        Column {
            Text(stringResource(R.string.request_type_text))
            Row {
                for (type in requestTypeList) {
                    RadioButton(
                        selected = requestType == type,
                        onClick = { onRequestTypeChange(type) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(type.toString())
                }
            }
        }
    }

}
