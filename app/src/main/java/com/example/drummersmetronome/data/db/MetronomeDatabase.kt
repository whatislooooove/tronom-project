package com.example.drummersmetronome.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.drummersmetronome.data.model.Folder
import com.example.drummersmetronome.data.model.Preset

@Database(entities = [Folder::class, Preset::class], version = 1, exportSchema = false)
abstract class MetronomeDatabase : RoomDatabase() {
    abstract fun metronomeDao(): MetronomeDao
}
