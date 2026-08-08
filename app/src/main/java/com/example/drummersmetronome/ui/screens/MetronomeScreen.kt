package com.example.drummersmetronome.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drummersmetronome.data.model.Folder

@Composable
fun MetronomeScreen(viewModel: MetronomeViewModel) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val bpm by viewModel.bpm.collectAsState()
    val folders by viewModel.folders.collectAsState()
    
    var showSaveDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // BPM Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(250.dp)
                .clickable { viewModel.toggleMetronome() }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = if (isPlaying) Color.Yellow else Color.Gray,
                    style = Stroke(width = 8.dp.toPx())
                )
            }
            Text(
                text = bpm.toString(),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Adjustments
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.adjustBpm(-5) }) {
                    Text("-5", color = Color.White)
                }
                IconButton(onClick = { viewModel.adjustBpm(-1) }) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(32.dp))
                IconButton(onClick = { viewModel.adjustBpm(1) }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
                IconButton(onClick = { viewModel.adjustBpm(5) }) {
                    Text("+5", color = Color.White)
                }
            }

            Slider(
                value = bpm.toFloat(),
                onValueChange = { viewModel.setBpm(it.toInt()) },
                valueRange = 20f..300f,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
            )
        }

        Button(onClick = { showSaveDialog = true }) {
            Text("Save Preset")
        }
    }

    if (showSaveDialog) {
        SavePresetDialog(
            folders = folders,
            onDismiss = { showSaveDialog = false },
            onSave = { folderId, name ->
                viewModel.savePreset(folderId, name)
                showSaveDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavePresetDialog(
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onSave: (Long, String) -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf<Folder?>(folders.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Preset") },
        text = {
            Column {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("Song Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedFolder?.name ?: "Select Folder",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Folder") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        folders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder.name) },
                                onClick = {
                                    selectedFolder = folder
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedFolder?.let { onSave(it.id, presetName) } },
                enabled = presetName.isNotBlank() && selectedFolder != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
