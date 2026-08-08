package com.example.drummersmetronome.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drummersmetronome.audio.MetronomeEngine
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
    val folders: StateFlow<List<Folder>> = repository.allFolders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun toggleMetronome() {
        engine.toggle()
    }

    fun setBpm(newBpm: Int) {
        engine.setBpm(newBpm)
    }

    fun adjustBpm(delta: Int) {
        engine.setBpm(engine.bpm.value + delta)
    }

    fun savePreset(folderId: Long, name: String) {
        viewModelScope.launch {
            repository.insertPreset(folderId, name, engine.bpm.value)
        }
    }
}
