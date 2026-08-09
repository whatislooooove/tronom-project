package com.example.drummersmetronome.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drummersmetronome.R
import com.example.drummersmetronome.data.model.Folder
import kotlinx.coroutines.delay

@Composable
fun MetronomeScreen(viewModel: MetronomeViewModel) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val bpm by viewModel.bpm.collectAsState()
    val numerator by viewModel.numerator.collectAsState()
    val denominator by viewModel.denominator.collectAsState()
    val folders by viewModel.folders.collectAsState()
    
    var currentBeat by remember { mutableIntStateOf(-1) }
    var lastBeatTime by remember { mutableLongStateOf(0L) }
    var showSaveDialog by remember { mutableStateOf(false) }

    // Listen to beats from the engine
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            viewModel.beatEvent.collect { beat ->
                currentBeat = beat
                lastBeatTime = System.currentTimeMillis()
            }
        } else {
            currentBeat = -1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D1B33)) // Dark purple room style
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Bongo Cat Image Area
        Box(
            modifier = Modifier
                .size(320.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { viewModel.toggleMetronome() }
                ),
            contentAlignment = Alignment.Center
        ) {
            BongoCatAnimation(
                beat = currentBeat,
                lastBeatTime = lastBeatTime,
                isPlaying = isPlaying
            )
        }

        // BPM Display (Number only)
        Text(
            text = bpm.toString(),
            fontSize = 110.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        // Signature Picker
        SignaturePicker(numerator, denominator) { n, d -> viewModel.setSignature(n, d) }

        // Adjustments Buttons
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BpmButton("-1", -1) { viewModel.adjustBpm(it) }
                Spacer(modifier = Modifier.width(32.dp))
                BpmButton("+1", 1) { viewModel.adjustBpm(it) }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BpmButton("-5", -5) { viewModel.adjustBpm(it) }
                Spacer(modifier = Modifier.width(32.dp))
                BpmButton("+5", 5) { viewModel.adjustBpm(it) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Slider(
                value = bpm.toFloat(),
                onValueChange = { viewModel.setBpm(it.toInt()) },
                valueRange = 20f..300f,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White.copy(alpha = 0.5f)
                )
            )
        }

        Button(
            onClick = { showSaveDialog = true },
            modifier = Modifier.height(56.dp).fillMaxWidth(0.6f),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
        ) {
            Text("Save Preset", color = Color.White)
        }
    }

    if (showSaveDialog) {
        SavePresetDialog(
            folders = folders,
            onDismiss = { showSaveDialog = false },
            onSave = { folderId, name ->
                viewModel.savePreset(folderId, name)
                showSaveDialog = false
            },
            onCreateFolderAndSave = { folderName, presetName ->
                viewModel.createFolderAndSavePreset(folderName, presetName)
                showSaveDialog = false
            }
        )
    }
}

@Composable
fun BongoCatAnimation(beat: Int, lastBeatTime: Long, isPlaying: Boolean) {
    val hitDuration = 100L // Time paws stay down in ms
    var isCurrentlyHitting by remember { mutableStateOf(false) }

    // Logic to reset to idle state after hit duration
    LaunchedEffect(lastBeatTime) {
        if (isPlaying && beat != -1) {
            isCurrentlyHitting = true
            delay(hitDuration)
            isCurrentlyHitting = false
        } else {
            isCurrentlyHitting = false
        }
    }

    val imageRes = when {
        !isCurrentlyHitting -> R.drawable.cat_idle
        beat == 0 -> R.drawable.cat_both // Strong beat
        else -> R.drawable.cat_right     // Weak beat
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Bongo Cat Metronome",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun BpmButton(label: String, delta: Int, onClick: (Int) -> Unit) {
    FilledTonalButton(
        onClick = { onClick(delta) },
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White
        )
    ) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SignaturePicker(num: Int, den: Int, onChange: (Int, Int) -> Unit) {
    var expandedNum by remember { mutableStateOf(false) }
    var expandedDen by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            TextButton(onClick = { expandedNum = true }) { 
                Text(num.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White) 
            }
            DropdownMenu(
                expanded = expandedNum, 
                onDismissRequest = { expandedNum = false },
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                (1..16).forEach { n ->
                    DropdownMenuItem(
                        text = { Text(n.toString()) }, 
                        onClick = { onChange(n, den); expandedNum = false }
                    )
                }
            }
        }
        Text("/", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))
        Box {
            TextButton(onClick = { expandedDen = true }) { 
                Text(den.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White) 
            }
            DropdownMenu(
                expanded = expandedDen, 
                onDismissRequest = { expandedDen = false },
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                listOf(2, 4, 8, 16).forEach { d ->
                    DropdownMenuItem(
                        text = { Text(d.toString()) }, 
                        onClick = { onChange(num, d); expandedDen = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavePresetDialog(
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onSave: (Long, String) -> Unit,
    onCreateFolderAndSave: (String, String) -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf<Folder?>(folders.firstOrNull()) }
    var newFolderName by remember { mutableStateOf("") }
    var isCreatingNewFolder by remember { mutableStateOf(folders.isEmpty()) }
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
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isCreatingNewFolder) {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("New Folder Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (folders.isNotEmpty()) {
                        TextButton(onClick = { isCreatingNewFolder = false }) {
                            Text("Select existing folder")
                        }
                    }
                } else {
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
                    TextButton(onClick = { isCreatingNewFolder = true }) {
                        Text("+ Create New Folder")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (isCreatingNewFolder) {
                        onCreateFolderAndSave(newFolderName, presetName)
                    } else {
                        selectedFolder?.let { onSave(it.id, presetName) }
                    }
                },
                enabled = presetName.isNotBlank() && (if (isCreatingNewFolder) newFolderName.isNotBlank() else selectedFolder != null)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
