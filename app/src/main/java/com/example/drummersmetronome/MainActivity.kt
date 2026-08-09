package com.example.drummersmetronome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            val isPlaying by container.metronomeEngine.isPlaying.collectAsState()
            
            MaterialTheme(colorScheme = darkColorScheme()) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Scaffold(
                    bottomBar = {
                        Surface(
                            tonalElevation = 3.dp,
                            modifier = Modifier
                                .navigationBarsPadding() // Space for Android Nav Bar
                                .height(80.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Metronome Button
                                BottomNavItem(
                                    modifier = Modifier.weight(1f),
                                    label = "Metronome",
                                    icon = Icons.Default.Timer,
                                    selected = currentDestination?.route == Screen.Metronome.route,
                                    onClick = {
                                        navController.navigate(Screen.Metronome.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )

                                // 2. Center Action Button
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FloatingActionButton(
                                        onClick = { container.metronomeEngine.toggle() },
                                        shape = CircleShape,
                                        containerColor = if (isPlaying) Color(0xFFFFEB3B) else Color(0xFF4CAF50),
                                        contentColor = if (isPlaying) Color.Black else Color.White,
                                        modifier = Modifier.size(60.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Toggle Metronome",
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }

                                // 3. Library Button
                                BottomNavItem(
                                    modifier = Modifier.weight(1f),
                                    label = "Library",
                                    icon = Icons.Default.List,
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

@Composable
fun BottomNavItem(
    modifier: Modifier,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = contentColor
        )
    }
}
