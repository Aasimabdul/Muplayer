package com.example.muplayer.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.muplayer.model.AudioTrack
import com.example.muplayer.ui.theme.uniqueLiquidDisplay
import com.example.muplayer.viewmodel.MusicViewModel

@Composable
fun AddToPlaylistDialog(
    tracks: List<AudioTrack>,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val playlists by viewModel.playlists.collectAsState()
    var showCreateForm by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (showCreateForm) "New Playlist" else "Add to Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (showCreateForm) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        singleLine = true,
                        label = { Text("Playlist name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showCreateForm = false
                            newName = ""
                        }) {
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            enabled = newName.isNotBlank(),
                            onClick = {
                                val job = viewModel.createPlaylist(newName.trim(), tracks.map { it.id })
                                job.invokeOnCompletion { onDismiss() }
                            }
                        ) {
                            Text("Create & Add")
                        }
                    }
                } else {
                    Text(
                        text = if (tracks.size == 1) "Add this song to a playlist" else "Add ${tracks.size} songs to a playlist",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = { showCreateForm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "+ Create New Playlist",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (playlists.isEmpty()) {
                        Text(
                            text = "You don't have any playlists yet. Create one to save these songs.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                            items(playlists, key = { it.id }) { playlist ->
                                Surface(
                                    onClick = {
                                        val job = viewModel.addTracksToPlaylist(playlist.id, tracks.map { it.id })
                                        job.invokeOnCompletion { onDismiss() }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlaylistPlay,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = playlist.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${playlist.trackIds.size} songs",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
