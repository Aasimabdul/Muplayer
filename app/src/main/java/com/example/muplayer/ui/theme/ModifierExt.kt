package com.example.muplayer.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(20.dp),
    accentColor: Color = MaterialTheme.colorScheme.primary,
    opacity: Float = 0.65f,
    borderOpacity: Float = 0.4f,
    elevation: Dp = 4.dp
): Modifier {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val isLight = LocalAppThemePalette.current.isLight

    // Luminous edge highlights (Apple-style specular highlight)
    val borderColors = if (isLight) {
        listOf(
            Color.White.copy(alpha = 0.8f),
            Color.White.copy(alpha = 0.2f),
            Color.Black.copy(alpha = 0.05f)
        )
    } else {
        listOf(
            Color.White.copy(alpha = borderOpacity),
            accentColor.copy(alpha = borderOpacity * 0.5f),
            Color.White.copy(alpha = 0.05f)
        )
    }

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = accentColor.copy(alpha = 0.3f),
            spotColor = Color.Black.copy(alpha = if (isLight) 0.1f else 0.5f)
        )
        .clip(shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    surfaceColor.copy(alpha = opacity + 0.15f),
                    surfaceVariant.copy(alpha = opacity - 0.05f)
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(colors = borderColors),
            shape = shape
        )
}

/**
 * Spring-based bounce animation on click, commonly used in iOS/Apple design.
 */
fun Modifier.bounceClick(
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit
): Modifier = composed {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by actualInteractionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "bounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = actualInteractionSource,
            indication = LocalIndication.current,
            onClick = onClick
        )
}

@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp)
): Modifier = glassSurface(shape = shape)

@Composable
fun Modifier.liquidGlassTab(
    shape: Shape = RoundedCornerShape(24.dp),
    accentColor: Color = MaterialTheme.colorScheme.primary
): Modifier = glassSurface(shape = shape, accentColor = accentColor, opacity = 0.85f, elevation = 12.dp)

@Composable
fun Modifier.uniqueLiquidDisplay(
    shape: Shape = RoundedCornerShape(24.dp),
    accentColor: Color = MaterialTheme.colorScheme.primary
): Modifier = glassSurface(shape = shape, accentColor = accentColor, opacity = 0.85f, elevation = 16.dp)


