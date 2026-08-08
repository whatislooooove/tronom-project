package com.example.drummersmetronome.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "folders")
@Serializable
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Serializable
data class FolderExport(
    val name: String,
    val presets: List<PresetExport>
)
