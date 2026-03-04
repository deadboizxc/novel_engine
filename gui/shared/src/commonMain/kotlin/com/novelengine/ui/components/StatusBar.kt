package com.novelengine.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.novelengine.theme.NovelColors

/**
 * Status bar showing game state information.
 *
 * Displays:
 * - Sanity (coins) with color indicator
 * - Current scene ID
 * - Active flags (optional)
 *
 * @param coins Current coins/sanity value
 * @param maxCoins Maximum coins value
 * @param sceneId Current scene ID
 * @param showSceneId Whether to show the scene ID
 * @param modifier Modifier for the bar
 */
@Composable
fun StatusBar(
    coins: Int,
    maxCoins: Int = 100,
    sceneId: String = "",
    showSceneId: Boolean = true,
    modifier: Modifier = Modifier
) {
    val sanityPercentage = (coins.toFloat() / maxCoins).coerceIn(0f, 1f)
    
    val sanityColor by animateColorAsState(
        targetValue = NovelColors.sanityColor(coins, maxCoins),
        animationSpec = tween(500),
        label = "sanityColor"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NovelColors.Surface.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sanity indicator
        SanityIndicator(
            coins = coins,
            maxCoins = maxCoins,
            color = sanityColor
        )
        
        // Scene ID (debug/dev mode)
        if (showSceneId && sceneId.isNotEmpty()) {
            Text(
                text = sceneId,
                style = MaterialTheme.typography.labelSmall,
                color = NovelColors.TextMuted
            )
        }
    }
}

/**
 * Sanity/coins indicator with progress bar.
 *
 * @param coins Current value
 * @param maxCoins Maximum value
 * @param color Color based on sanity level
 */
@Composable
fun SanityIndicator(
    coins: Int,
    maxCoins: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val percentage = (coins.toFloat() / maxCoins).coerceIn(0f, 1f)
    
    // Pulse animation for low sanity
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (percentage < 0.3f) 0.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (percentage < 0.3f) 500 else 1000,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Label
        Text(
            text = "SANITY",
            style = MaterialTheme.typography.labelSmall,
            color = NovelColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Progress bar
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(NovelColors.SurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = pulseAlpha))
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Value
        Text(
            text = "$coins%",
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = pulseAlpha)
        )
    }
}

/**
 * Compact status chip for inventory items.
 *
 * @param name Item name
 * @param count Item count (optional)
 */
@Composable
fun ItemChip(
    name: String,
    count: Int = 1,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(NovelColors.DeepPurple)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = NovelColors.TextPrimary
        )
        
        if (count > 1) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "×$count",
                style = MaterialTheme.typography.labelSmall,
                color = NovelColors.TextSecondary
            )
        }
    }
}

/**
 * Flag indicator (active flags shown as chips).
 *
 * @param flags Map of flag names to their values
 * @param showOnlyTrue Whether to show only true flags
 */
@Composable
fun FlagIndicator(
    flags: Map<String, Boolean>,
    showOnlyTrue: Boolean = true,
    modifier: Modifier = Modifier
) {
    val visibleFlags = if (showOnlyTrue) {
        flags.filter { it.value }
    } else {
        flags
    }
    
    if (visibleFlags.isEmpty()) return
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        visibleFlags.forEach { (name, value) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (value) NovelColors.AccentCyan.copy(alpha = 0.3f)
                        else NovelColors.SurfaceVariant
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (value) NovelColors.AccentCyan else NovelColors.TextMuted
                )
            }
        }
    }
}
