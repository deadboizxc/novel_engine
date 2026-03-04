package com.novelengine.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.novelengine.theme.NovelEngineTheme
import com.novelengine.ui.screens.*

/**
 * Main Activity for Novel Engine Android app.
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            NovelEngineApp()
        }
    }
}

/**
 * Navigation state.
 */
sealed class Screen {
    object Menu : Screen()
    object Settings : Screen()
    data class Game(val storyPath: String) : Screen()
}

/**
 * Root application composable.
 */
@Composable
fun NovelEngineApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
    
    // TODO: Check for save file
    val hasSaveGame = false
    
    NovelEngineTheme {
        when (val screen = currentScreen) {
            is Screen.Menu -> {
                MenuScreen(
                    onNewGame = {
                        currentScreen = Screen.Game("stories/blue_frequency")
                    },
                    onContinue = {
                        currentScreen = Screen.Game("stories/blue_frequency")
                    },
                    onSettings = {
                        currentScreen = Screen.Settings
                    },
                    onQuit = {
                        // Finish activity
                    },
                    hasSaveGame = hasSaveGame,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            is Screen.Settings -> {
                SettingsScreen(
                    onBack = {
                        currentScreen = Screen.Menu
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            is Screen.Game -> {
                GameScreenStateful(
                    storyPath = screen.storyPath,
                    onNavigateToMenu = {
                        currentScreen = Screen.Menu
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
