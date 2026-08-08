package com.example.drummersmetronome.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.drummersmetronome.audio.MetronomeEngine
import com.example.drummersmetronome.data.repository.MetronomeRepository
import com.example.drummersmetronome.ui.screens.LibraryScreen
import com.example.drummersmetronome.ui.screens.LibraryViewModel
import com.example.drummersmetronome.ui.screens.MetronomeScreen
import com.example.drummersmetronome.ui.screens.MetronomeViewModel

sealed class Screen(val route: String) {
    object Metronome : Screen("metronome")
    object Library : Screen("library")
}

@Composable
fun MetronomeNavGraph(
    navController: NavHostController,
    repository: MetronomeRepository,
    engine: MetronomeEngine
) {
    NavHost(navController = navController, startDestination = Screen.Metronome.route) {
        composable(Screen.Metronome.route) {
            val viewModel = MetronomeViewModel(repository, engine)
            MetronomeScreen(viewModel)
        }
        composable(Screen.Library.route) {
            val viewModel = LibraryViewModel(repository)
            LibraryScreen(
                viewModel = viewModel,
                onLoadPreset = { bpm ->
                    engine.setBpm(bpm)
                    navController.popBackStack()
                }
            )
        }
    }
}
