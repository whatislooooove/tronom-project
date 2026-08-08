package com.example.drummersmetronome.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    // Create ViewModels once at the Graph level so they persist during navigation
    val metronomeViewModel = remember { MetronomeViewModel(repository, engine) }
    val libraryViewModel = remember { LibraryViewModel(repository) }

    NavHost(navController = navController, startDestination = Screen.Metronome.route) {
        composable(Screen.Metronome.route) {
            MetronomeScreen(metronomeViewModel)
        }
        composable(Screen.Library.route) {
            LibraryScreen(
                viewModel = libraryViewModel,
                metronomeViewModel = metronomeViewModel,
                onNavigateBack = {
                    navController.navigate(Screen.Metronome.route) {
                        popUpTo(Screen.Metronome.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
