package com.example.muplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.muplayer.model.AudioTrack
import com.example.muplayer.ui.theme.uniqueLiquidDisplay

@Composable
fun QuickActionsSheet(
    track: AudioTrack,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenTagEditor: () -> Unit,
    onOpenPlaylistDialog: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onDismiss: () -> Unit
) {
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                ListItem(
                    headlineContent = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites") },
                    leadingContent = {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        onToggleFavorite()
                        onDismiss()
                    }
                )

                ListItem(
                    headlineContent = { Text("Edit Metadata / Tags") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        onDismiss()
                        onOpenTagEditor()
                    }
                )

                ListItem(
                    headlineContent = { Text("Add to Playlist") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.PlaylistAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        onDismiss()
                        onOpenPlaylistDialog()
                    }
                )

                ListItem(
                    headlineContent = { Text("Equalizer") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        onDismiss()
                        onOpenEqualizer()
                    }
                )

                ListItem(
                    headlineContent = { Text("Sleep Timer") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        onDismiss()
                        onOpenSleepTimer()
                    }
                )

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
