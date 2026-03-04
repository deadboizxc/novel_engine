package com.novelengine.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.novelengine.theme.NovelColors
import com.novelengine.ui.components.MenuButton
import com.novelengine.ui.effects.GlitchyText
import com.novelengine.ui.effects.ScanlinesEffect
import com.novelengine.ui.effects.VignetteEffect

/**
 * Main menu screen.
 *
 * @param onNewGame Start new game callback
 * @param onContinue Continue saved game callback
 * @param onSettings Open settings callback
 * @param onQuit Quit game callback
 * @param hasSaveGame Whether a save game exists
 * @param modifier Modifier for the screen
 */
@Composable
fun MenuScreen(
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onSettings: () -> Unit,
    onQuit: () -> Unit,
    hasSaveGame: Boolean = false,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    // Title animation
    val infiniteTransition = rememberInfiniteTransition(label = "title")
    val titleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleAlpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovelColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -it / 2 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlitchyText(
                        text = "NOVEL ENGINE",
                        intensity = 0.1f,
                        style = MaterialTheme.typography.displayLarge,
                        color = NovelColors.AccentPink.copy(alpha = titleAlpha),
                        modifier = Modifier.alpha(titleAlpha)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "v2.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = NovelColors.TextMuted
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Menu items
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000, delayMillis = 500))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MenuButton(
                        text = "НОВАЯ ИГРА",
                        onClick = onNewGame
                    )
                    
                    MenuButton(
                        text = "ПРОДОЛЖИТЬ",
                        onClick = onContinue,
                        enabled = hasSaveGame
                    )
                    
                    MenuButton(
                        text = "НАСТРОЙКИ",
                        onClick = onSettings
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    MenuButton(
                        text = "ВЫХОД",
                        onClick = onQuit
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000, delayMillis = 1000))
            ) {
                Text(
                    text = "© 2024 Novel Engine Team",
                    style = MaterialTheme.typography.labelSmall,
                    color = NovelColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Effects
        VignetteEffect(
            enabled = true,
            intensity = 0.5f,
            modifier = Modifier.fillMaxSize()
        )
        
        ScanlinesEffect(
            enabled = true,
            opacity = 0.05f,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Settings screen.
 *
 * @param onBack Go back callback
 * @param modifier Modifier for the screen
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovelColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "НАСТРОЙКИ",
                style = MaterialTheme.typography.headlineMedium,
                color = NovelColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // TODO: Add settings controls
            Text(
                text = "В разработке...",
                style = MaterialTheme.typography.bodyMedium,
                color = NovelColors.TextMuted
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            MenuButton(
                text = "НАЗАД",
                onClick = onBack
            )
        }
        
        VignetteEffect(
            enabled = true,
            intensity = 0.4f,
            modifier = Modifier.fillMaxSize()
        )
    }
}
