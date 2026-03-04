package com.novelengine.ui.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Glitch effect that creates RGB split and random offsets.
 *
 * Used for psychological horror moments when the reality shifts.
 *
 * @param enabled Whether the effect is active
 * @param intensity Effect intensity (0.0 to 1.0)
 * @param content Content to apply the effect to
 */
@Composable
fun GlitchEffect(
    enabled: Boolean,
    intensity: Float = 0.5f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var rgbSplit by remember { mutableStateOf(0f) }

    LaunchedEffect(enabled) {
        while (enabled) {
            // Random glitch intensity
            val glitchIntensity = if (Random.nextFloat() > 0.7f) {
                Random.nextFloat() * intensity
            } else {
                intensity * 0.2f
            }

            offsetX = (Random.nextFloat() - 0.5f) * 20 * glitchIntensity
            offsetY = (Random.nextFloat() - 0.5f) * 10 * glitchIntensity
            rgbSplit = Random.nextFloat() * 5 * glitchIntensity

            // Random timing for organic feel
            delay(30L + Random.nextLong(100))
        }
        // Reset when disabled
        offsetX = 0f
        offsetY = 0f
        rgbSplit = 0f
    }

    Box(modifier = modifier) {
        if (enabled && rgbSplit > 0) {
            // Red channel (shifted right)
            Box(
                modifier = Modifier
                    .offset { IntOffset(rgbSplit.toInt() + 2, 0) }
                    .alpha(0.5f)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            color = Color.Red,
                            blendMode = BlendMode.Multiply
                        )
                    }
            ) {
                content()
            }

            // Blue channel (shifted left)
            Box(
                modifier = Modifier
                    .offset { IntOffset(-rgbSplit.toInt() - 2, 0) }
                    .alpha(0.5f)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            color = Color.Blue,
                            blendMode = BlendMode.Multiply
                        )
                    }
            ) {
                content()
            }
        }

        // Main content with offset
        Box(
            modifier = Modifier.offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
        ) {
            content()
        }
    }
}

/**
 * Static noise effect overlay.
 *
 * Creates TV static visual noise.
 *
 * @param enabled Whether the effect is active
 * @param intensity Noise intensity (0.0 to 1.0)
 * @param modifier Modifier for the canvas
 */
@Composable
fun StaticEffect(
    enabled: Boolean,
    intensity: Float = 0.3f,
    modifier: Modifier = Modifier
) {
    var noiseSeed by remember { mutableStateOf(0L) }

    LaunchedEffect(enabled) {
        while (enabled) {
            noiseSeed = System.currentTimeMillis()
            delay(50)
        }
    }

    if (enabled) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val random = Random(noiseSeed)
            val pixelSize = 4f
            val cols = (size.width / pixelSize).toInt()
            val rows = (size.height / pixelSize).toInt()

            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    if (random.nextFloat() < intensity) {
                        val brightness = random.nextFloat()
                        val color = Color.White.copy(alpha = brightness * intensity)
                        drawRect(
                            color = color,
                            topLeft = Offset(col * pixelSize, row * pixelSize),
                            size = androidx.compose.ui.geometry.Size(pixelSize, pixelSize)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Screen shake effect.
 *
 * Shakes the content for impact moments.
 *
 * @param enabled Whether the effect is active
 * @param intensity Shake intensity (0.0 to 1.0)
 * @param content Content to shake
 */
@Composable
fun ShakeEffect(
    enabled: Boolean,
    intensity: Float = 0.5f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    LaunchedEffect(enabled) {
        if (enabled) {
            val maxOffset = 10f * intensity
            repeat(10) {
                offsetX = (Random.nextFloat() - 0.5f) * 2 * maxOffset
                offsetY = (Random.nextFloat() - 0.5f) * 2 * maxOffset
                delay(50)
            }
            // Gradually settle
            repeat(5) {
                offsetX *= 0.5f
                offsetY *= 0.5f
                delay(50)
            }
            offsetX = 0f
            offsetY = 0f
        }
    }

    Box(
        modifier = modifier.offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
    ) {
        content()
    }
}

/**
 * Scanlines effect for CRT monitor aesthetic.
 *
 * @param enabled Whether the effect is active
 * @param lineSpacing Space between scanlines in pixels
 * @param modifier Modifier for the canvas
 */
@Composable
fun ScanlinesEffect(
    enabled: Boolean,
    lineSpacing: Int = 3,
    opacity: Float = 0.1f,
    modifier: Modifier = Modifier
) {
    if (enabled) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val lines = (size.height / lineSpacing).toInt()
            for (i in 0 until lines) {
                val y = i * lineSpacing.toFloat()
                drawLine(
                    color = Color.Black.copy(alpha = opacity),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
        }
    }
}

/**
 * Vignette effect for focus and atmosphere.
 *
 * Darkens the edges of the screen.
 *
 * @param enabled Whether the effect is active
 * @param intensity Darkness intensity (0.0 to 1.0)
 * @param modifier Modifier for the canvas
 */
@Composable
fun VignetteEffect(
    enabled: Boolean,
    intensity: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    if (enabled) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val maxRadius = maxOf(size.width, size.height) * 0.7f

            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = intensity * 0.3f),
                        Color.Black.copy(alpha = intensity * 0.6f),
                        Color.Black.copy(alpha = intensity)
                    ),
                    center = Offset(centerX, centerY),
                    radius = maxRadius
                ),
                center = Offset(centerX, centerY),
                radius = maxRadius * 1.5f
            )
        }
    }
}

/**
 * Flicker effect for unstable lights.
 *
 * @param enabled Whether the effect is active
 * @param content Content to flicker
 */
@Composable
fun FlickerEffect(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var alpha by remember { mutableStateOf(1f) }

    LaunchedEffect(enabled) {
        while (enabled) {
            alpha = if (Random.nextFloat() > 0.9f) {
                Random.nextFloat() * 0.3f + 0.7f
            } else {
                1f
            }
            delay(50 + Random.nextLong(100))
        }
        alpha = 1f
    }

    Box(modifier = modifier.alpha(alpha)) {
        content()
    }
}
