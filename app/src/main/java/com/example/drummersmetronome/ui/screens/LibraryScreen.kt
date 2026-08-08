package com.example.drummersmetronome.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.drummersmetronome.data.model.Folder
import com.example.drummersmetronome.data.model.Preset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onLoadPreset: (Int) -> Unit
) {
    val folders by viewModel.folders.collectAsState()
    val context = LocalContext.current
    
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }
    var presetToDelete by remember { mutableStateOf<Preset?>(null) }
    var presetToLoad by remember { mutableStateOf<Preset?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { viewModel.exportData(context, it) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importData(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preset Library") },
                actions = {
                    TextButton(onClick = { exportLauncher.launch("metronome_backup.json") }) { Text("Export") }
                    TextButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) { Text("Import") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewFolderDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Folder")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(folders) { folder ->
                FolderItem(
                    folder = folder,
                    viewModel = viewModel,
                    onPresetClick = { presetToLoad = it },
                    onPresetLongClick = { presetToDelete = it },
                    onFolderLongClick = { folderToDelete = it }
                )
            }
        }
    }

    if (showNewFolderDialog) {
        NewFolderDialog(
            onDismiss = { showNewFolderDialog = false },
            onConfirm = { 
                viewModel.addFolder(it)
                showNewFolderDialog = false
            }
        )
    }

    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("Delete Folder?") },
            text = { Text("All presets in '${folder.name}' will be lost.") },
            confirmButton = {
                Button(onClick = { 
                    viewModel.deleteFolder(folder)
                    folderToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { folderToDelete = null }) { Text("Cancel") } }
        )
    }

    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text("Delete Preset?") },
            text = { Text("Delete '${preset.name}'?") },
            confirmButton = {
                Button(onClick = { 
                    viewModel.deletePreset(preset)
                    presetToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { presetToDelete = null }) { Text("Cancel") } }
        )
    }

    presetToLoad?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToLoad = null },
            title = { Text("Load BPM") },
            text = { Text("Load '${preset.name}' (${preset.bpm} BPM)?") },
            confirmButton = {
                Button(onClick = { 
                    onLoadPreset(preset.bpm)
                    presetToLoad = null
                }) { Text("Load") }
            },
            dismissButton = { TextButton(onClick = { presetToLoad = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderItem(
    folder: Folder,
    viewModel: LibraryViewModel,
    onPresetClick: (Preset) -> Unit,
    onPresetLongClick: (Preset) -> Unit,
    onFolderLongClick: (Folder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val presets by viewModel.getPresets(folder.id).collectAsState(emptyList())

    Column {
        ListItem(
            headlineContent = { Text(folder.name) },
            trailingContent = {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            },
            modifier = Modifier.combinedClickable(
                onClick = { expanded = !expanded },
                onLongClick = { onFolderLongClick(folder) }
            )
        )
        if (expanded) {
            presets.forEach { preset ->
                ListItem(
                    headlineContent = { Text(preset.name) },
                    supportingContent = { Text("${preset.bpm} BPM") },
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .combinedClickable(
                            onClick = { onPresetClick(preset) },
                            onLongClick = { onPresetLongClick(preset) }
                        )
                )
            }
        }
    }
}

@Composable
fun NewFolderDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Folder") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Folder Name") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
