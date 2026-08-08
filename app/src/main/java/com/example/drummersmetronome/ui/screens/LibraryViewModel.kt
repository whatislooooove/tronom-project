package com.example.drummersmetronome.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drummersmetronome.data.model.Folder
import com.example.drummersmetronome.data.model.FolderExport
import com.example.drummersmetronome.data.model.Preset
import com.example.drummersmetronome.data.model.PresetExport
import com.example.drummersmetronome.data.repository.MetronomeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

class LibraryViewModel(private val repository: MetronomeRepository) : ViewModel() {

    // Initial state is null to avoid "Empty" screen flicker
    val folders: StateFlow<List<Folder>?> = repository.allFolders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    fun getPresets(folderId: Long) = repository.getPresetsByFolder(folderId)

    fun addFolder(name: String) {
        viewModelScope.launch {
            repository.insertFolder(name)
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            repository.deleteFolder(folder)
        }
    }

    fun deletePreset(preset: Preset) {
        viewModelScope.launch {
            repository.deletePreset(preset)
        }
    }

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            val allFolders = repository.getAllFoldersSync()
            val exportList = allFolders.map { folder ->
                val presets = repository.getPresetsByFolderSync(folder.id).map {
                    PresetExport(it.name, it.bpm)
                }
                FolderExport(folder.name, presets)
            }
            val json = Json.encodeToString(exportList)
            context.contentResolver.openOutputStream(uri)?.use { 
                it.write(json.toByteArray())
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            val stringBuilder = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                }
            }
            val importList = Json.decodeFromString<List<FolderExport>>(stringBuilder.toString())
            repository.clearAll()
            importList.forEach { folderExport ->
                val folderId = repository.insertFolder(Folder(name = folderExport.name))
                folderExport.presets.forEach { presetExport ->
                    repository.insertPreset(Preset(folderId = folderId, name = presetExport.name, bpm = presetExport.bpm))
                }
            }
        }
    }
}
