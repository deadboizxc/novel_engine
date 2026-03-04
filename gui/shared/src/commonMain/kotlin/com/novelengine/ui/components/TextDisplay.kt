package com.novelengine.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.novelengine.theme.NovelColors
import com.novelengine.ui.effects.TypewriterText
import com.novelengine.ui.effects.GlitchyText

/**
 * Text display component for scene content.
 *
 * Features:
 * - Typewriter animation for new text
 * - Tap to skip animation
 * - Scrollable for long content
 * - Glitch effect support
 *
 * @param text Scene text to display
 * @param isTyping Whether to animate the text
 * @param glitchEnabled Whether to apply glitch effect
 * @param onTypingComplete Callback when typing animation finishes
 * @param onTap Callback when user taps the display
 * @param modifier Modifier for the component
 */
@Composable
fun TextDisplay(
    text: String,
    isTyping: Boolean = true,
    glitchEnabled: Boolean = false,
    onTypingComplete: () -> Unit = {},
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var skipRequested by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    // Reset skip when text changes
    LaunchedEffect(text) {
        skipRequested = false
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.pressed }) {
                            skipRequested = true
                            onTap()
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            if (glitchEnabled) {
                GlitchyText(
                    text = text,
                    intensity = 0.4f,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NovelColors.TextPrimary
                )
            } else if (isTyping && !skipRequested) {
                TypewriterText(
                    text = text,
                    charDelayMs = 25L,
                    skipRequested = skipRequested,
                    onComplete = onTypingComplete,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NovelColors.TextPrimary
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NovelColors.TextPrimary
                )
            }
        }
        
        // Fade gradient at bottom for scroll indication
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NovelColors.Background.copy(alpha = 0.9f)
                        )
                    )
                )
        )
    }
}

/**
 * Compact text display for menu screens or dialogs.
 */
@Composable
fun CompactTextDisplay(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = NovelColors.TextPrimary,
        textAlign = textAlign,
        modifier = modifier.padding(16.dp)
    )
}
