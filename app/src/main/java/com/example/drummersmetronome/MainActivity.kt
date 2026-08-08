package com.example.drummersmetronome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.drummersmetronome.ui.navigation.MetronomeNavGraph
import com.example.drummersmetronome.ui.navigation.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force hardware volume buttons to control media volume
        volumeControlStream = android.media.AudioManager.STREAM_MUSIC
        
        val container = (application as MetronomeApplication).container

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                                label = { Text("Metronome") },
                                selected = currentDestination?.route == Screen.Metronome.route,
                                onClick = {
                                    navController.navigate(Screen.Metronome.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.List, contentDescription = null) },
                                label = { Text("Library") },
                                selected = currentDestination?.route == Screen.Library.route,
                                onClick = {
                                    navController.navigate(Screen.Library.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        MetronomeNavGraph(
                            navController = navController,
                            repository = container.repository,
                            engine = container.metronomeEngine
                        )
                    }
                }
            }
        }
    }
}
