package com.example.drummersmetronome.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drummersmetronome.audio.MetronomeEngine
import com.example.drummersmetronome.data.model.Preset
import com.example.drummersmetronome.data.model.Folder
import com.example.drummersmetronome.data.repository.MetronomeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MetronomeViewModel(
    private val repository: MetronomeRepository,
    private val engine: MetronomeEngine
) : ViewModel() {

    val isPlaying: StateFlow<Boolean> = engine.isPlaying
    val bpm: StateFlow<Int> = engine.bpm
    val numerator: StateFlow<Int> = engine.numerator
    val denominator: StateFlow<Int> = engine.denominator
    val beatEvent = engine.beatEvent

    val folders: StateFlow<List<Folder>> = repository.allFolders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun toggleMetronome() {
        engine.toggle()
    }

    fun setBpm(newBpm: Int) {
        engine.setBpm(newBpm)
    }

    fun setSignature(num: Int, den: Int) {
        engine.setSignature(num, den)
    }

    fun adjustBpm(delta: Int) {
        engine.setBpm(engine.bpm.value + delta)
    }

    fun loadPreset(preset: Preset) {
        engine.setBpm(preset.bpm)
        engine.setSignature(preset.numerator, preset.denominator)
    }

    fun savePreset(folderId: Long, name: String) {
        viewModelScope.launch {
            repository.insertPreset(
                Preset(
                    folderId = folderId,
                    name = name,
                    bpm = engine.bpm.value,
                    numerator = engine.numerator.value,
                    denominator = engine.denominator.value
                )
            )
        }
    }

    fun createFolderAndSavePreset(folderName: String, presetName: String) {
        viewModelScope.launch {
            val newFolderId = repository.insertFolder(folderName)
            savePreset(newFolderId, presetName)
        }
    }
}
