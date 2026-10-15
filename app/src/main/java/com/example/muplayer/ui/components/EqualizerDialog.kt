package com.example.muplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.muplayer.service.PlaybackService
import com.example.muplayer.ui.theme.uniqueLiquidDisplay

@Composable
fun EqualizerDialog(onDismiss: () -> Unit) {
    val isEqEnabled by PlaybackService.isEqEnabled.collectAsState()
    val isBassBoostEnabled by PlaybackService.isBassBoostEnabled.collectAsState()
    val bassBoostStrength by PlaybackService.bassBoostStrength.collectAsState()
    val customEqBands by PlaybackService.customEqBands.collectAsState()

    var activePreset by remember { mutableStateOf(PlaybackService.currentEqPreset.value) }

    val presets = listOf(
        "Custom", "Normal", "Classical", "Dance", "Flat", "Folk",
        "Heavy Metal", "Hip Hop", "Jazz", "Pop", "Rock"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .uniqueLiquidDisplay(
                    shape = RoundedCornerShape(28.dp),
                    accentColor = MaterialTheme.colorScheme.primary
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Equalizer & Bass Boost",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isEqEnabled,
                        onCheckedChange = { enabled ->
                            PlaybackService.isEqEnabled.value = enabled
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Presets Section
                Text(
                    text = "Equalizer Presets",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets.indices.toList()) { index ->
                        val presetName = presets[index]
                        val presetId = (index - 1).toShort()
                        val isSelected = activePreset == presetId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                activePreset = presetId
                                PlaybackService.currentEqPreset.value = presetId
                            },
                            label = { Text(presetName) },
                            enabled = isEqEnabled
                        )
                    }
                }

                if (activePreset.toInt() == -1) {
                    // Custom EQ Sliders
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text(
                        text = "Custom Bands",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (band in 0 until 5) {
                            val bandId = band.toShort()
                            val level = customEqBands[bandId] ?: 0.toShort()
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Slider(
                                        value = level.toFloat(),
                                        onValueChange = { newVal ->
                                            val newBands = customEqBands.toMutableMap()
                                            newBands[bandId] = newVal.toInt().toShort()
                                            PlaybackService.customEqBands.value = newBands
                                        },
                                        valueRange = -1500f..1500f,
                                        enabled = isEqEnabled,
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier
                                            .requiredWidth(140.dp) // The length of the slider
                                            .graphicsLayer {
                                                rotationZ = -90f
                                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                                            }
                                    )
                                }
                                Text(
                                    text = when(band) {
                                        0 -> "60Hz"
                                        1 -> "230Hz"
                                        2 -> "910Hz"
                                        3 -> "3kHz"
                                        4 -> "14kHz"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Bass Booster Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bass Booster",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = isBassBoostEnabled,
                        onCheckedChange = { enabled ->
                            PlaybackService.isBassBoostEnabled.value = enabled
                        },
                        enabled = isEqEnabled
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Bass Boost Level",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${((bassBoostStrength.toFloat() / 1000f) * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = bassBoostStrength.toFloat(),
                        onValueChange = { newVal ->
                            PlaybackService.bassBoostStrength.value = newVal.toInt().toShort()
                        },
                        valueRange = 0f..1000f,
                        enabled = isEqEnabled && isBassBoostEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
