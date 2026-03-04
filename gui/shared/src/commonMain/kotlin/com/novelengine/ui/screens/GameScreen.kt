package com.novelengine.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novelengine.engine.GameState
import com.novelengine.engine.GameStateHolder
import com.novelengine.theme.NovelColors
import com.novelengine.ui.components.*
import com.novelengine.ui.effects.*

/**
 * Main game screen composable.
 *
 * Displays the current scene with text, choices, status bar,
 * and visual effects.
 *
 * @param gameState Current game state
 * @param onChoiceSelected Callback when player selects a choice
 * @param onSaveGame Callback to save game
 * @param onMenuRequested Callback to open menu
 * @param modifier Modifier for the screen
 */
@Composable
fun GameScreen(
    gameState: GameState,
    onChoiceSelected: (Int) -> Unit,
    onSaveGame: () -> Unit = {},
    onMenuRequested: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isTypingComplete by remember { mutableStateOf(false) }
    
    // Reset typing state when scene changes
    LaunchedEffect(gameState.currentSceneId) {
        isTypingComplete = false
    }
    
    // Check for active animation
    val hasGlitchAnimation = gameState.currentAnimation?.let {
        it.type == "glitch" && it.isActive
    } ?: false
    
    val hasStaticAnimation = gameState.currentAnimation?.let {
        it.type == "static" && it.isActive
    } ?: false
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovelColors.Background)
    ) {
        // Main content with effects
        GlitchEffect(
            enabled = hasGlitchAnimation,
            intensity = gameState.currentAnimation?.let { 1f - it.progress } ?: 0.5f
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Status bar
                StatusBar(
                    coins = gameState.coins,
                    sceneId = gameState.currentSceneId,
                    showSceneId = true // Set to false in production
                )
                
                // Scene text
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    TextDisplay(
                        text = gameState.sceneText,
                        isTyping = !isTypingComplete,
                        glitchEnabled = hasGlitchAnimation,
                        onTypingComplete = { isTypingComplete = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Choices (shown when typing is complete)
                AnimatedVisibility(
                    visible = isTypingComplete && gameState.choices.isNotEmpty() && !gameState.isLoading,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    ChoiceList(
                        choices = gameState.choices,
                        onChoiceSelected = onChoiceSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
                
                // Loading indicator
                AnimatedVisibility(
                    visible = gameState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        // Simple loading indicator
                        androidx.compose.material3.CircularProgressIndicator(
                            color = NovelColors.AccentPink,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Game over indicator
                AnimatedVisibility(
                    visible = gameState.isGameOver && isTypingComplete,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        MenuButton(
                            text = "[ КОНЕЦ ]",
                            onClick = onMenuRequested
                        )
                    }
                }
            }
        }
        
        // Static overlay
        StaticEffect(
            enabled = hasStaticAnimation,
            intensity = gameState.currentAnimation?.let { 0.3f * (1f - it.progress) } ?: 0.3f,
            modifier = Modifier.fillMaxSize()
        )
        
        // Vignette (always on for atmosphere)
        VignetteEffect(
            enabled = true,
            intensity = 0.4f,
            modifier = Modifier.fillMaxSize()
        )
        
        // Scanlines (optional CRT effect)
        ScanlinesEffect(
            enabled = false, // Toggle for CRT mode
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Stateful game screen that manages its own GameStateHolder.
 *
 * @param storyPath Path to story directory
 * @param onNavigateToMenu Callback to navigate to menu
 */
@Composable
fun GameScreenStateful(
    storyPath: String,
    onNavigateToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameStateHolder = remember { GameStateHolder() }
    val gameState by gameStateHolder.state.collectAsState()
    
    // Load story on first composition
    LaunchedEffect(storyPath) {
        if (gameStateHolder.loadStory(storyPath)) {
            gameStateHolder.newGame()
        }
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            gameStateHolder.close()
        }
    }
    
    // Clear animation when it completes
    LaunchedEffect(gameState.currentAnimation) {
        gameState.currentAnimation?.let { anim ->
            kotlinx.coroutines.delay((anim.duration * 1000).toLong())
            gameStateHolder.clearAnimation()
        }
    }
    
    GameScreen(
        gameState = gameState,
        onChoiceSelected = { index ->
            gameStateHolder.selectChoice(index)
        },
        onSaveGame = {
            // Save to default slot
            gameStateHolder.saveToFile("save_1.json")
        },
        onMenuRequested = onNavigateToMenu,
        modifier = modifier
    )
}
