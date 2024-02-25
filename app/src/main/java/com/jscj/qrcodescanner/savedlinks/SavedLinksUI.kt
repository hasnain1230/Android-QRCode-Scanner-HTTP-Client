package com.jscj.qrcodescanner.savedlinks

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.jscj.qrcodescanner.Helper
import com.jscj.qrcodescanner.R
import kotlin.math.abs

class SavedLinksUI(private val savedLinksViewModel: SavedLinksViewModel) {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SavedLinksScreen(onNavigateBack: () -> Unit) {
        val context = LocalContext.current
        var showDialog by remember { mutableStateOf(false) }
        var newLink by remember { mutableStateOf("") }
        var isSelectionMode by remember { mutableStateOf(false) }
        val selectedLinks = remember { mutableStateListOf<String>() }

        // Handle system back press to exit edit mode
        BackHandler(isSelectionMode) {
            isSelectionMode = false
            selectedLinks.clear()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Saved Links") },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isSelectionMode) {
                                isSelectionMode = false
                                selectedLinks.clear()
                            } else {
                                onNavigateBack()
                            }
                        }) {
                            Icon(Icons.TwoTone.ArrowBack, contentDescription = if (isSelectionMode) "Close" else "Back")
                        }
                    },
                    actions = {
                        if (isSelectionMode) {
                            if (selectedLinks.isNotEmpty()) {
                                IconButton(onClick = {
                                    // TODO: Implement logic to delete selected links
                                    savedLinksViewModel.removeLinks(selectedLinks)
                                    selectedLinks.clear()
                                }) {
                                    Icon(painter = painterResource(id = R.drawable.twotone_delete_24), contentDescription = "Delete Selected")
                                }
                            } else {
                                IconButton(onClick = {
                                    isSelectionMode = false
                                    selectedLinks.clear()
                                }) {
                                    Icon(Icons.TwoTone.Close, contentDescription = "Exit Edit Mode")
                                }
                            }
                        } else {
                            IconButton(onClick = { isSelectionMode = true }) {
                                Icon(Icons.TwoTone.Edit, contentDescription = "Select")
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!isSelectionMode) {
                    FloatingActionButton(onClick = { showDialog = true }) {
                        Icon(Icons.TwoTone.Add, contentDescription = "Add Link")
                    }
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
                                .fillMaxWidth(),
                            onClick = {
                                Helper.openUrl(context, link)
                            },
                            onDelete = {
                                savedLinksViewModel.removeLink(link)
                            },
                            isSelected = link in selectedLinks,
                            onSelect = { isSelected ->
                                if (isSelected) selectedLinks.add(link) else selectedLinks.remove(link)
                            },
                            isSelectionMode = isSelectionMode
                        )
                        Divider()
                    }
                }
            }
        }
    }

    @Composable
    fun ListItem(
        text: @Composable () -> Unit,
        modifier: Modifier,
        onClick: () -> Unit,
        onDelete: () -> Unit,
        isSelected: Boolean,
        onSelect: (Boolean) -> Unit,
        isSelectionMode: Boolean
    ) {
        var showMenu by remember { mutableStateOf(false) }
        var isPressed by remember { mutableStateOf(false) }
        var dropDownMenuPosition by remember { mutableStateOf(DpOffset.Zero) }

        Box(modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { offset ->
                    val xPos = abs((offset.x.toDp()).value).toDp()
                    dropDownMenuPosition = DpOffset(xPos, 0.dp)
                    showMenu = true
                },
                onTap = { onClick() },
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(if (isPressed) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surface),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelect(it) }
                    )
                    Spacer(Modifier.width(8.dp))
                }
                text()
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = {
                    showMenu = false
                    isPressed = false
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
                        Text("Delete Link")
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.twotone_delete_forever_24), // Replace with your delete icon asset
                            contentDescription = "Delete Link",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }
    }
}


