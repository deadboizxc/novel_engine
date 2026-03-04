package com.novelengine.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.novelengine.engine.Choice
import com.novelengine.theme.NovelColors

/**
 * Choice button for visual novel gameplay.
 *
 * Features:
 * - Hover and press animations
 * - Disabled state with tooltip
 * - Glowing border effect
 * - Index indicator
 *
 * @param choice Choice data
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the button
 * @param showIndex Whether to show choice index (1, 2, 3...)
 */
@Composable
fun ChoiceButton(
    choice: Choice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showIndex: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animations
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            isHovered -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val borderAlpha by animateFloatAsState(
        targetValue = if (isHovered || isPressed) 1f else 0.6f,
        animationSpec = tween(200),
        label = "borderAlpha"
    )
    
    val bgAlpha by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.3f
            isHovered -> 0.2f
            else -> 0.1f
        },
        animationSpec = tween(200),
        label = "bgAlpha"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .alpha(if (choice.enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        NovelColors.MidPurple.copy(alpha = bgAlpha),
                        NovelColors.DeepPurple.copy(alpha = bgAlpha * 0.5f)
                    )
                )
            )
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NovelColors.AccentPink.copy(alpha = borderAlpha),
                            NovelColors.AccentCyan.copy(alpha = borderAlpha * 0.5f)
                        )
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = choice.enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Index indicator
            if (showIndex) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(NovelColors.AccentPink.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "${choice.index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = NovelColors.AccentPink
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            // Choice text
            Text(
                text = choice.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (choice.enabled) NovelColors.TextPrimary else NovelColors.TextMuted,
                modifier = Modifier.weight(1f)
            )
            
            // Disabled reason tooltip
            if (!choice.enabled && choice.disabledReason != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = choice.disabledReason,
                    style = MaterialTheme.typography.labelSmall,
                    color = NovelColors.TextMuted
                )
            }
        }
    }
}

/**
 * List of choice buttons.
 *
 * @param choices List of choices
 * @param onChoiceSelected Callback when a choice is selected
 * @param modifier Modifier for the column
 */
@Composable
fun ChoiceList(
    choices: List<Choice>,
    onChoiceSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        choices.forEach { choice ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it / 2 },
            ) {
                ChoiceButton(
                    choice = choice,
                    onClick = { onChoiceSelected(choice.index) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Simple text button for menus.
 */
@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = when {
            !enabled -> NovelColors.TextMuted
            isHovered -> NovelColors.AccentPink
            else -> NovelColors.TextPrimary
        },
        textAlign = TextAlign.Center,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(16.dp)
    )
}
