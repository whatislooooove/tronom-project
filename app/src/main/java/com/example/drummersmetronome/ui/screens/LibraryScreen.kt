package com.example.drummersmetronome.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drummersmetronome.data.model.Folder
import com.example.drummersmetronome.data.model.Preset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    metronomeViewModel: MetronomeViewModel,
    onNavigateBack: () -> Unit
) {
    val foldersNullable by viewModel.folders.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }
    var presetToDelete by remember { mutableStateOf<Preset?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { viewModel.exportData(context, it) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { 
            viewModel.importData(context, it)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Library imported successfully",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), contentAlignment = Alignment.Center) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(data.visuals.message, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
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
        val folders = foldersNullable
        if (folders == null) {
            // Loading state - show nothing, the VM will provide data instantly after first load
            Box(modifier = Modifier.fillMaxSize().padding(padding))
        } else if (folders.isEmpty()) {
            // Show "Empty" only if we are certain there's no data
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No folders yet. Create one!", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(folders, key = { it.id }) { folder ->
                    FolderItem(
                        folder = folder,
                        viewModel = viewModel,
                        onPresetClick = { preset ->
                            metronomeViewModel.loadPreset(preset)
                            onNavigateBack()
                        },
                        onPresetLongClick = { presetToDelete = it },
                        onFolderLongClick = { folderToDelete = it }
                    )
                }
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
            headlineContent = { Text(folder.name, fontSize = 20.sp, fontWeight = FontWeight.Medium) },
            trailingContent = {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .combinedClickable(
                    onClick = { expanded = !expanded },
                    onLongClick = { onFolderLongClick(folder) }
                )
        )
        if (expanded) {
            if (presets.isEmpty()) {
                Text(
                    "No presets in this folder",
                    modifier = Modifier.padding(start = 32.dp, bottom = 8.dp),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            } else {
                presets.forEach { preset ->
                    ListItem(
                        headlineContent = { Text(preset.name) },
                        supportingContent = { Text("${preset.bpm} BPM - ${preset.numerator}/${preset.denominator}") },
                        modifier = Modifier
                            .padding(start = 24.dp)
                            .combinedClickable(
                                onClick = { onPresetClick(preset) },
                                onLongClick = { onPresetLongClick(preset) }
                            )
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
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
