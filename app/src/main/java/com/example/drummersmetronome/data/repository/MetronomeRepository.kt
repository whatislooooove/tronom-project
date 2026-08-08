package com.example.drummersmetronome.data.repository

import com.example.drummersmetronome.data.db.MetronomeDao
import com.example.drummersmetronome.data.model.Folder
import com.example.drummersmetronome.data.model.Preset
import kotlinx.coroutines.flow.Flow

class MetronomeRepository(private val dao: MetronomeDao) {
    val allFolders: Flow<List<Folder>> = dao.getAllFolders()

    suspend fun insertFolder(name: String): Long {
        return dao.insertFolder(Folder(name = name))
    }

    suspend fun deleteFolder(folder: Folder) {
        dao.deleteFolder(folder)
    }

    fun getPresetsByFolder(folderId: Long): Flow<List<Preset>> {
        return dao.getPresetsByFolder(folderId)
    }

    suspend fun insertPreset(folderId: Long, name: String, bpm: Int) {
        dao.insertPreset(Preset(folderId = folderId, name = name, bpm = bpm))
    }

    suspend fun deletePreset(preset: Preset) {
        dao.deletePreset(preset)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
    
    suspend fun getAllFoldersSync() = dao.getAllFoldersSync()
    suspend fun getPresetsByFolderSync(folderId: Long) = dao.getPresetsByFolderSync(folderId)
    suspend fun insertFolder(folder: Folder) = dao.insertFolder(folder)
    suspend fun insertPreset(preset: Preset) = dao.insertPreset(preset)
}
