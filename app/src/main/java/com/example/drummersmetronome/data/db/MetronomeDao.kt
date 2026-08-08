package com.example.drummersmetronome.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.drummersmetronome.data.model.Folder
import com.example.drummersmetronome.data.model.Preset
import kotlinx.coroutines.flow.Flow

@Dao
interface MetronomeDao {
    @Query("SELECT * FROM folders")
    fun getAllFolders(): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder): Long

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("SELECT * FROM presets WHERE folderId = :folderId")
    fun getPresetsByFolder(folderId: Long): Flow<List<Preset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: Preset)

    @Delete
    suspend fun deletePreset(preset: Preset)

    @Transaction
    suspend fun clearAll() {
        deleteAllPresets()
        deleteAllFolders()
    }

    @Query("DELETE FROM presets")
    suspend fun deleteAllPresets()

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()
    
    @Query("SELECT * FROM folders")
    suspend fun getAllFoldersSync(): List<Folder>
    
    @Query("SELECT * FROM presets WHERE folderId = :folderId")
    suspend fun getPresetsByFolderSync(folderId: Long): List<Preset>
}
