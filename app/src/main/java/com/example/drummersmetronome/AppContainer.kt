package com.example.drummersmetronome

import android.content.Context
import androidx.room.Room
import com.example.drummersmetronome.audio.MetronomeEngine
import com.example.drummersmetronome.data.db.MetronomeDatabase
import com.example.drummersmetronome.data.repository.MetronomeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val database: MetronomeDatabase by lazy {
        Room.databaseBuilder(
            context,
            MetronomeDatabase::class.java,
            "metronome_db"
        ).build()
    }

    val repository: MetronomeRepository by lazy {
        MetronomeRepository(database.metronomeDao())
    }

    val applicationScope = CoroutineScope(SupervisorJob())

    val metronomeEngine: MetronomeEngine by lazy {
        MetronomeEngine(applicationScope)
    }
}
