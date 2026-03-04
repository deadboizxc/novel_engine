package com.deadboizxc.tuinovel.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deadboizxc.tuinovel.ui.effects.GlitchTextGenerator
import com.deadboizxc.tuinovel.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * =============================================================================
 * TUI Novel - Анимированный текст терминала
 * Печатающий эффект + глитчи + психоделические искажения
 * =============================================================================
 */

/**
 * Текст с эффектом печатания (typewriter)
 * 
 * @param fullText Полный текст для отображения
 * @param typeDelay Задержка между символами (мс)
 * @param onTypingComplete Callback по завершении печати
 * @param isGlitching Включить глитч-эффект
 * @param glitchIntensity Интенсивность глитча (0.0 - 1.0)
 */
@Composable
fun TypewriterText(
    fullText: String,
    typeDelay: Long = 30L,
    onTypingComplete: () -> Unit = {},
    isGlitching: Boolean = false,
    glitchIntensity: Float = 0.3f,
    modifier: Modifier = Modifier
) {
    var visibleText by remember(fullText) { mutableStateOf("") }
    var isComplete by remember(fullText) { mutableStateOf(false) }
    var displayText by remember(fullText) { mutableStateOf("") }
    
    // Эффект печатания
    LaunchedEffect(fullText) {
        visibleText = ""
        isComplete = false
        
        fullText.forEach { char ->
            visibleText += char
            delay(typeDelay)
        }
        
        isComplete = true
        onTypingComplete()
    }
    
    // Глитч-эффект на уже напечатанный текст
    LaunchedEffect(visibleText, isGlitching) {
        if (isGlitching && visibleText.isNotEmpty()) {
            while (true) {
                displayText = if (Random.nextFloat() < glitchIntensity) {
                    GlitchTextGenerator.corrupt(visibleText, glitchIntensity * 0.5f)
                } else {
                    visibleText
                }
                delay(Random.nextLong(50, 200))
            }
        } else {
            displayText = visibleText
        }
    }
    
    // Модификатор для глитч-эффекта
    val glitchModifier = if (isGlitching) {
        modifier.glitchTextModifier(glitchIntensity)
    } else {
        modifier
    }
    
    Text(
        text = displayText + if (!isComplete) "█" else "",
        color = if (isGlitching) {
            // Случайно меняем цвет при глитче
            remember { mutableStateOf(TerminalGreen) }.let { colorState ->
                LaunchedEffect(isGlitching) {
                    while (isGlitching) {
                        colorState.value = if (Random.nextFloat() > 0.9f) {
                            listOf(PsychoRed, PsychoCyan, PsychoMagenta).random()
                        } else {
                            TerminalGreen
                        }
                        delay(100)
                    }
                }
                colorState.value
            }
        } else {
            TerminalGreen
        },
        fontFamily = TerminalFontFamily,
        fontSize = 14.sp,
        style = TextStyle(
            shadow = if (isGlitching) {
                Shadow(
                    color = PsychoCyan.copy(alpha = 0.5f),
                    offset = Offset(2f, 0f),
                    blurRadius = 4f
                )
            } else null
        ),
        modifier = glitchModifier
    )
}

/**
 * Модификатор глитч-эффекта для текста
 */
@Composable
private fun Modifier.glitchTextModifier(intensity: Float): Modifier {
    val offsetX by produceState(initialValue = 0f) {
        while (true) {
            value = (Random.nextFloat() - 0.5f) * 10f * intensity
            delay(Random.nextLong(30, 100))
        }
    }
    
    val offsetY by produceState(initialValue = 0f) {
        while (true) {
            value = (Random.nextFloat() - 0.5f) * 5f * intensity
            delay(Random.nextLong(50, 150))
        }
    }
    
    val skewX by produceState(initialValue = 0f) {
        while (true) {
            value = (Random.nextFloat() - 0.5f) * 0.1f * intensity
            delay(Random.nextLong(100, 300))
        }
    }
    
    return this.graphicsLayer {
        translationX = offsetX
        translationY = offsetY
        // rotationZ = skewX * 5f // Небольшой наклон
    }
}

/**
 * Текст с RGB разделением (хроматическая аберрация)
 */
@Composable
fun ChromaticText(
    text: String,
    offset: Float = 3f,
    modifier: Modifier = Modifier
) {
    val animatedOffset by produceState(initialValue = offset) {
        while (true) {
            value = Random.nextFloat() * offset
            delay(100)
        }
    }
    
    // Рисуем три слоя текста со сдвигом
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        // Красный канал (сдвиг влево)
        Text(
            text = text,
            color = Color.Red.copy(alpha = 0.5f),
            fontFamily = TerminalFontFamily,
            fontSize = 14.sp,
            modifier = Modifier.graphicsLayer {
                translationX = -animatedOffset
            }
        )
        
        // Циан канал (сдвиг вправо)
        Text(
            text = text,
            color = Color.Cyan.copy(alpha = 0.5f),
            fontFamily = TerminalFontFamily,
            fontSize = 14.sp,
            modifier = Modifier.graphicsLayer {
                translationX = animatedOffset
            }
        )
        
        // Основной зелёный текст
        Text(
            text = text,
            color = TerminalGreen,
            fontFamily = TerminalFontFamily,
            fontSize = 14.sp
        )
    }
}

/**
 * "Испорченный" текст с рандомными символами
 */
@Composable
fun CorruptedText(
    text: String,
    corruptionLevel: Float = 0.3f,
    modifier: Modifier = Modifier
) {
    var displayText by remember { mutableStateOf(text) }
    
    LaunchedEffect(text) {
        while (true) {
            displayText = GlitchTextGenerator.corrupt(text, corruptionLevel)
            delay(Random.nextLong(50, 200))
        }
    }
    
    Text(
        text = displayText,
        color = CorruptedTextColor,
        fontFamily = TerminalFontFamily,
        fontSize = 14.sp,
        modifier = modifier
    )
}

/**
 * Текст с Zalgo эффектом (демонический текст)
 */
@Composable
fun ZalgoText(
    text: String,
    intensity: Int = 2,
    modifier: Modifier = Modifier
) {
    var displayText by remember { mutableStateOf(text) }
    
    LaunchedEffect(text) {
        while (true) {
            displayText = GlitchTextGenerator.zalgofy(text, intensity)
            delay(Random.nextLong(100, 300))
        }
    }
    
    Text(
        text = displayText,
        color = PsychoRed,
        fontFamily = TerminalFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

/**
 * Текст с эффектом "эхо" (повторяющиеся строки)
 */
@Composable
fun EchoText(
    text: String,
    echoCount: Int = 3,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        repeat(echoCount) { index ->
            val alpha = 1f - (index * 0.3f)
            val offset = index * 4
            
            Text(
                text = text,
                color = TerminalGreen.copy(alpha = alpha.coerceIn(0.1f, 1f)),
                fontFamily = TerminalFontFamily,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = offset.dp)
                    .graphicsLayer {
                        this.alpha = alpha
                    }
            )
        }
    }
}

/**
 * Мерцающий текст
 */
@Composable
fun FlickeringText(
    text: String,
    flickerSpeed: Long = 100L,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        while (true) {
            // Случайные интервалы для более естественного мерцания
            delay(if (isVisible) Random.nextLong(50, 500) else Random.nextLong(30, 150))
            isVisible = !isVisible || Random.nextFloat() > 0.3f
        }
    }
    
    Text(
        text = text,
        color = TerminalGreen,
        fontFamily = TerminalFontFamily,
        fontSize = 14.sp,
        modifier = modifier.graphicsLayer {
            alpha = if (isVisible) 1f else 0.1f
        }
    )
}

/**
 * Текст, который "дрожит"
 */
@Composable
fun ShakingText(
    text: String,
    intensity: Float = 1f,
    modifier: Modifier = Modifier
) {
    val offsetX by produceState(initialValue = 0f) {
        while (true) {
            value = (Random.nextFloat() - 0.5f) * 6f * intensity
            delay(16) // ~60fps
        }
    }
    
    val offsetY by produceState(initialValue = 0f) {
        while (true) {
            value = (Random.nextFloat() - 0.5f) * 6f * intensity
            delay(16)
        }
    }
    
    Text(
        text = text,
        color = PsychoRed,
        fontFamily = TerminalFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.graphicsLayer {
            translationX = offsetX
            translationY = offsetY
        }
    )
}

/**
 * Текст с градиентной анимацией цвета
 */
@Composable
fun RainbowText(
    text: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val hue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hue"
    )
    
    Text(
        text = text,
        color = Color.hsl(hue, 1f, 0.5f),
        fontFamily = TerminalFontFamily,
        fontSize = 14.sp,
        modifier = modifier
    )
}

/**
 * Расширение Color для HSL
 */
private fun Color.Companion.hsl(hue: Float, saturation: Float, lightness: Float): Color {
    val c = (1 - kotlin.math.abs(2 * lightness - 1)) * saturation
    val x = c * (1 - kotlin.math.abs((hue / 60) % 2 - 1))
    val m = lightness - c / 2
    
    val (r, g, b) = when {
        hue < 60 -> Triple(c, x, 0f)
        hue < 120 -> Triple(x, c, 0f)
        hue < 180 -> Triple(0f, c, x)
        hue < 240 -> Triple(0f, x, c)
        hue < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    
    return Color(r + m, g + m, b + m)
}
