package com.example.muplayer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.muplayer.model.AudioTrack
import com.example.muplayer.ui.components.AddToPlaylistDialog
import com.example.muplayer.ui.components.TagEditorDialog
import com.example.muplayer.ui.theme.liquidGlass
import com.example.muplayer.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    viewModel: MusicViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val tracks by viewModel.tracks.collectAsState()
    
    var selectedFolderPath by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedFolderName by rememberSaveable { mutableStateOf<String?>(null) }
    var trackToAdd by remember { mutableStateOf<AudioTrack?>(null) }
    var trackToEdit by remember { mutableStateOf<AudioTrack?>(null) }

    trackToAdd?.let { track ->
        AddToPlaylistDialog(listOf(track), viewModel, onDismiss = { trackToAdd = null })
    }

    trackToEdit?.let { track ->
        TagEditorDialog(track, viewModel, onDismiss = { trackToEdit = null })
    }

    if (!hasPermission) {
        PermissionDeniedState(onRequestPermission)
        return
    }

    if (tracks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No music folders found.", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    val folders = remember(tracks) {
        tracks.groupBy { it.folderPath }.map { (path, folderTracks) ->
            FolderItemData(
                name = folderTracks.firstOrNull()?.folderName ?: "Unknown",
                path = path,
                trackCount = folderTracks.size
            )
        }.sortedBy { it.name.lowercase() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (selectedFolderPath == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Folders",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(folders, key = { it.path }) { folder ->
                    FolderItem(
                        name = folder.name,
                        trackCount = folder.trackCount,
                        onClick = {
                            selectedFolderPath = folder.path
                            selectedFolderName = folder.name
                        }
                    )
                }
            }
        } else {
            BackHandler {
                selectedFolderPath = null
            }

            val folderTracks = remember(tracks, selectedFolderPath) {
                tracks.filter { it.folderPath == selectedFolderPath }.sortedBy { it.title.lowercase() }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedFolderPath = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedFolderName ?: "Unknown",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${folderTracks.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(folderTracks, key = { it.id }) { track ->
                    LibraryTrackItem(
                        track = track, 
                        onClick = { viewModel.playTrack(track, queue = folderTracks) },
                        onAddToPlaylist = { trackToAdd = track },
                        onEditTags = { trackToEdit = track }
                    )
                }
            }
        }
    }
}

@Composable
fun FolderItem(name: String, trackCount: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .liquidGlass(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Folder,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$trackCount songs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class FolderItemData(val name: String, val path: String, val trackCount: Int)
