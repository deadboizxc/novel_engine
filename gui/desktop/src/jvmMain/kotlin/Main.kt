package com.novelengine.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.novelengine.theme.NovelEngineTheme
import com.novelengine.ui.screens.*

/**
 * Navigation state for the application.
 */
sealed class Screen {
    object Menu : Screen()
    object Settings : Screen()
    data class Game(val storyPath: String) : Screen()
}

/**
 * Main entry point for the desktop application.
 */
fun main() = application {
    val windowState = WindowState(
        size = DpSize(1280.dp, 720.dp)
    )
    
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Novel Engine",
        resizable = true
    ) {
        NovelEngineApp()
    }
}

/**
 * Root application composable with navigation.
 */
@Composable
fun NovelEngineApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
    
    // Check for existing save
    val hasSaveGame = remember {
        java.io.File("save_1.json").exists()
    }
    
    NovelEngineTheme {
        when (val screen = currentScreen) {
            is Screen.Menu -> {
                MenuScreen(
                    onNewGame = {
                        // Default story path
                        currentScreen = Screen.Game("stories/blue_frequency")
                    },
                    onContinue = {
                        // TODO: Load save and determine story path
                        currentScreen = Screen.Game("stories/blue_frequency")
                    },
                    onSettings = {
                        currentScreen = Screen.Settings
                    },
                    onQuit = {
                        // Exit application
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
