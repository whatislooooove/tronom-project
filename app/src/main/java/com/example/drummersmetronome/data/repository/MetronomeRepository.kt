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

    suspend fun insertPreset(preset: Preset): Long {
        return dao.insertPreset(preset)
    }

    suspend fun deletePreset(preset: Preset) {
        dao.deletePreset(preset)
    }

    suspend fun clearAll() {
        dao.deleteAllPresets()
        dao.deleteAllFolders()
    }
    
    suspend fun getAllFoldersSync() = dao.getAllFoldersSync()
    suspend fun getPresetsByFolderSync(folderId: Long) = dao.getPresetsByFolderSync(folderId)
    suspend fun insertFolder(folder: Folder) = dao.insertFolder(folder)
}
