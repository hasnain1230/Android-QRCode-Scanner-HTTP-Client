package com.jscj.qrcodescanner.savedlinks

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jscj.qrcodescanner.Helper

class SavedLinksUI(private val savedLinksViewModel: SavedLinksViewModel) {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SavedLinksScreen(onNavigateBack: () -> Unit) {
        val context = LocalContext.current
        var showDialog by remember { mutableStateOf(false) }
        var newLink by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Saved Links") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Link")
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Add a new link") },
                        text = {
                            TextField(
                                value = newLink,
                                onValueChange = { newLink = it },
                                label = { Text("https://") }
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (newLink.isNotEmpty()) {
                                    newLink.trim().lowercase().let {
                                        newLink = if (it.startsWith("https://")) {
                                            it
                                        } else {
                                            "https://$it"
                                        }
                                    }
                                    savedLinksViewModel.addLink(newLink)
                                } else {
                                    Toast.makeText(context, "Link cannot be empty", Toast.LENGTH_SHORT).show()
                                }

                                showDialog = false
                                newLink = ""
                            }) {
                                Text("Add")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                val links by savedLinksViewModel.savedLinks.observeAsState(listOf())
                LazyColumn {
                    items(links) { link ->
                        ListItem(
                            text = { Text(link) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Helper.openUrl(context, link)
                                }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    @Composable
    fun ListItem(text: @Composable () -> Unit, modifier: Modifier) {
        Box(modifier = modifier.padding(16.dp)) {
            text()
        }
    }
}
