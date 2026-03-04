package com.novelengine.ui.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Typewriter effect that reveals text character by character.
 *
 * Creates an immersive reading experience common in visual novels.
 *
 * @param text The full text to display
 * @param charDelayMs Delay between each character in milliseconds
 * @param onComplete Callback when typing is complete
 * @param skipRequested If true, instantly shows all text
 * @param modifier Modifier for the text
 * @param style Text style
 * @param color Text color
 */
@Composable
fun TypewriterText(
    text: String,
    charDelayMs: Long = 30L,
    onComplete: () -> Unit = {},
    skipRequested: Boolean = false,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    var displayedText by remember(text) { mutableStateOf("") }
    var isComplete by remember(text) { mutableStateOf(false) }
    
    // Cursor blink animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )
    
    LaunchedEffect(text, skipRequested) {
        if (skipRequested) {
            displayedText = text
            isComplete = true
            onComplete()
            return@LaunchedEffect
        }
        
        displayedText = ""
        isComplete = false
        
        for (i in text.indices) {
            if (skipRequested) {
                displayedText = text
                break
            }
            
            displayedText = text.substring(0, i + 1)
            
            // Variable delay for more natural feel
            val char = text[i]
            val actualDelay = when (char) {
                '.', '!', '?' -> charDelayMs * 5  // Long pause at sentence end
                ',', ';', ':' -> charDelayMs * 2  // Medium pause at breaks
                '\n' -> charDelayMs * 3           // Pause at newlines
                ' ' -> charDelayMs / 2            // Fast for spaces
                else -> charDelayMs
            }
            
            delay(actualDelay)
        }
        
        isComplete = true
        onComplete()
    }
    
    // Display text with optional cursor
    Box(modifier = modifier) {
        Text(
            text = if (isComplete) displayedText else "$displayedText█",
            style = style,
            color = color,
            modifier = if (!isComplete) Modifier.alpha(1f) else Modifier
        )
        
        // Blinking cursor overlay (only while typing)
        if (!isComplete) {
            Text(
                text = displayedText + "█",
                style = style,
                color = color.copy(alpha = cursorAlpha),
                modifier = Modifier.alpha(cursorAlpha)
            )
        }
    }
}

/**
 * Glitchy text that randomly corrupts characters.
 *
 * Used for horror moments when reality breaks down.
 *
 * @param text The text to display
 * @param intensity Corruption intensity (0.0 to 1.0)
 * @param enabled Whether the effect is active
 * @param modifier Modifier for the text
 * @param style Text style
 * @param color Text color
 */
@Composable
fun GlitchyText(
    text: String,
    intensity: Float = 0.3f,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    var displayedText by remember { mutableStateOf(text) }
    
    // Glitch characters
    val glitchChars = "█▓▒░╔╗╚╝║═╬├┤┬┴┼▀▄▌▐■□●○"
    
    LaunchedEffect(text, enabled) {
        while (enabled) {
            val chars = text.toCharArray()
            
            for (i in chars.indices) {
                if (Random.nextFloat() < intensity * 0.3f) {
                    chars[i] = glitchChars.random()
                }
            }
            
            displayedText = String(chars)
            delay(50 + Random.nextLong(100))
            
            // Occasionally show original text
            if (Random.nextFloat() > 0.7f) {
                displayedText = text
                delay(100 + Random.nextLong(200))
            }
        }
        
        displayedText = text
    }
    
    Text(
        text = displayedText,
        style = style,
        color = color,
        modifier = modifier
    )
}

/**
 * Fade-in text animation.
 *
 * @param text Text to display
 * @param durationMs Animation duration in milliseconds
 * @param modifier Modifier for the text
 * @param style Text style
 * @param color Text color
 */
@Composable
fun FadeInText(
    text: String,
    durationMs: Int = 500,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    var visible by remember(text) { mutableStateOf(false) }
    
    LaunchedEffect(text) {
        visible = false
        delay(50)
        visible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMs, easing = EaseInOut),
        label = "fadeIn"
    )
    
    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier.alpha(alpha)
    )
}

/**
 * Text with scramble reveal effect.
 *
 * Characters cycle through random values before settling.
 *
 * @param text Target text to reveal
 * @param durationMs Total animation duration
 * @param modifier Modifier for the text
 * @param style Text style
 * @param color Text color
 */
@Composable
fun ScrambleText(
    text: String,
    durationMs: Long = 1000L,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    var displayedText by remember(text) { mutableStateOf("") }
    val scrambleChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*"
    
    LaunchedEffect(text) {
        val charRevealTime = durationMs / text.length
        val iterations = 5
        
        for (charIndex in text.indices) {
            // Scramble this character a few times
            for (i in 0 until iterations) {
                val revealed = text.substring(0, charIndex)
                val scrambled = scrambleChars.random()
                val remaining = text.substring(charIndex + 1).map {
                    if (it == ' ' || it == '\n') it else scrambleChars.random()
                }.joinToString("")
                
                displayedText = "$revealed$scrambled$remaining"
                delay(charRevealTime / iterations)
            }
            
            // Reveal this character
            displayedText = text.substring(0, charIndex + 1) +
                text.substring(charIndex + 1).map {
                    if (it == ' ' || it == '\n') it else scrambleChars.random()
                }.joinToString("")
        }
        
        displayedText = text
    }
    
    Text(
        text = displayedText,
        style = style,
        color = color,
        modifier = modifier
    )
}
