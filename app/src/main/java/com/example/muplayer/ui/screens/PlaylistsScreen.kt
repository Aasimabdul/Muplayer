package com.example.muplayer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.muplayer.model.AudioTrack
import com.example.muplayer.model.Playlist
import com.example.muplayer.ui.components.AddToPlaylistDialog
import com.example.muplayer.ui.components.TagEditorDialog
import com.example.muplayer.ui.theme.liquidGlass
import com.example.muplayer.ui.theme.uniqueLiquidDisplay
import com.example.muplayer.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(viewModel: MusicViewModel) {
    val playlists by viewModel.playlists.collectAsState()
    val allTracks by viewModel.tracks.collectAsState()
    val favoriteIds by viewModel.favorites.collectAsState()
    val playCounts by viewModel.playCounts.collectAsState()

    var selectedPlaylistId by rememberSaveable { mutableStateOf<String?>(null) }
    var trackToAdd by remember { mutableStateOf<AudioTrack?>(null) }
    var trackToEdit by remember { mutableStateOf<AudioTrack?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importM3uPlaylist(it) }
    }

    trackToAdd?.let { track ->
        AddToPlaylistDialog(listOf(track), viewModel, onDismiss = { trackToAdd = null })
    }

    trackToEdit?.let { track ->
        TagEditorDialog(track, viewModel, onDismiss = { trackToEdit = null })
    }

    if (showCreateDialog) {
        var newName by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showCreateDialog = false }) {
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
                    Text(
                        text = "New Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        singleLine = true,
                        placeholder = { Text("Playlist Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (newName.isNotBlank()) {
                                viewModel.createPlaylist(newName)
                                showCreateDialog = false
                            }
                        }) { Text("Create") }
                    }
                }
            }
        }
    }

    val mostPlayedTrackIds = remember(playCounts, allTracks) {
        playCounts.entries
            .filter { it.value > 0 }
            .sortedWith(
                compareByDescending<Map.Entry<Long, Int>> { it.value }
                    .thenBy { entry -> allTracks.find { it.id == entry.key }?.title ?: "" }
                    .thenBy { it.key }
            )
            .map { it.key }
            .take(50)
    }

    val virtualPlaylists = listOfNotNull(
        Playlist(
            id = "special_liked_songs",
            name = "Liked Songs",
            trackIds = favoriteIds
        ).takeIf { favoriteIds.isNotEmpty() },
        Playlist(
            id = "special_most_played",
            name = "Most Played Songs",
            trackIds = mostPlayedTrackIds
        ).takeIf { mostPlayedTrackIds.isNotEmpty() }
    )

    val allPlaylists = virtualPlaylists + playlists

    Column(modifier = Modifier.fillMaxSize()) {
        if (selectedPlaylistId == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Playlists",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(
                        onClick = { filePickerLauncher.launch(arrayOf("audio/x-mpegurl", "audio/mpegurl", "application/vnd.apple.mpegurl", "application/x-mpegurl", "*/*")) },
                        modifier = Modifier.liquidGlass(RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Filled.FileUpload, contentDescription = "Import M3U", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.liquidGlass(RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "New Playlist", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            if (allPlaylists.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No playlists yet.", color = MaterialTheme.colorScheme.onBackground)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(allPlaylists, key = { it.id }) { playlist ->
                        val isVirtual = playlist.id.startsWith("special_")
                        PlaylistItem(
                            playlist = playlist,
                            onClick = { selectedPlaylistId = playlist.id },
                            onRename = { newName -> if (!isVirtual) viewModel.renamePlaylist(playlist.id, newName) },
                            onDelete = { if (!isVirtual) viewModel.deletePlaylist(playlist.id) },
                            isVirtual = isVirtual
                        )
                    }
                }
            }
        } else {
            BackHandler { selectedPlaylistId = null }

            val playlist = allPlaylists.find { it.id == selectedPlaylistId }
            if (playlist == null) {
                selectedPlaylistId = null
                return
            }

            val isVirtual = playlist.id.startsWith("special_")

            val playlistTracks = remember(playlist.trackIds, allTracks) {
                playlist.trackIds.mapNotNull { id -> allTracks.find { it.id == id } }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedPlaylistId = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${playlistTracks.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (playlistTracks.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.playTrack(playlistTracks.first(), queue = playlistTracks) },
                        modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play All", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            if (playlistTracks.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("This playlist is empty.", color = MaterialTheme.colorScheme.onBackground)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(playlistTracks, key = { it.id }) { track ->
                        LibraryTrackItem(
                            track = track,
                            onClick = { viewModel.playTrack(track, queue = playlistTracks) },
                            onAddToPlaylist = { trackToAdd = track },
                            onEditTags = { trackToEdit = track },
                            onRemoveFromPlaylist = { if (!isVirtual) viewModel.removeTrackFromPlaylist(playlist.id, track.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistItem(
    playlist: Playlist,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    isVirtual: Boolean = false
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showRenameDialog) {
        var renameText by remember { mutableStateOf(playlist.name) }
        Dialog(onDismissRequest = { showRenameDialog = false }) {
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
                    Text(
                        text = "Rename Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (renameText.isNotBlank()) onRename(renameText)
                            showRenameDialog = false
                        }) { Text("Save") }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .uniqueLiquidDisplay(
                        shape = RoundedCornerShape(28.dp),
                        accentColor = MaterialTheme.colorScheme.error
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Delete Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Are you sure you want to delete '${playlist.name}'?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onDelete()
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .liquidGlass(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .semantics { contentDescription = "Open playlist ${playlist.name}" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (playlist.id) {
                "special_liked_songs" -> Icons.Filled.Favorite
                "special_most_played" -> Icons.Filled.Star
                else -> Icons.Filled.QueueMusic
            },
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${playlist.trackIds.size} songs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!isVirtual) {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = { menuExpanded = false; showRenameDialog = true }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { menuExpanded = false; showDeleteDialog = true }
                    )
                }
            }
        }
    }
}
