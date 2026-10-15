package com.example.muplayer.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.muplayer.model.AudioTrack
import com.example.muplayer.ui.components.AddToPlaylistDialog
import com.example.muplayer.ui.components.TagEditorDialog
import com.example.muplayer.ui.theme.GlassBorder
import com.example.muplayer.ui.theme.GlassSurface
import com.example.muplayer.ui.theme.liquidGlass
import com.example.muplayer.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

enum class SortOption {
    TITLE, ARTIST, RECENTLY_ADDED, DURATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val tracks by viewModel.tracks.collectAsState()
    val defaultSortString by viewModel.defaultSort.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember(defaultSortString) { 
        mutableStateOf(
            try { SortOption.valueOf(defaultSortString) } catch (_: Exception) { SortOption.TITLE }
        ) 
    }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var trackToAdd by remember { mutableStateOf<List<AudioTrack>?>(null) }
    var trackToEdit by remember { mutableStateOf<AudioTrack?>(null) }
    var selectedTracks by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    trackToAdd?.let { tracks ->
        AddToPlaylistDialog(tracks, viewModel, onDismiss = { trackToAdd = null; selectedTracks = emptySet() })
    }

    trackToEdit?.let { track ->
        TagEditorDialog(track, viewModel, onDismiss = { trackToEdit = null })
    }

    if (!hasPermission) {
        PermissionDeniedState(onRequestPermission)
        return
    }

    val filteredTracks = remember(tracks, searchQuery, sortOption) {
        var list = if (searchQuery.isBlank()) {
            tracks
        } else {
            tracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true)
            }
        }
        list = when (sortOption) {
            SortOption.TITLE -> list.sortedBy { it.title.lowercase() }
            SortOption.ARTIST -> list.sortedBy { it.artist.lowercase() }
            SortOption.RECENTLY_ADDED -> list.sortedByDescending { it.dateAdded }
            SortOption.DURATION -> list.sortedByDescending { it.durationMs }
        }
        list
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val isSelectionMode = selectedTracks.isNotEmpty()

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Tracks?") },
            text = { Text("Are you sure you want to delete ${selectedTracks.size} tracks from your device? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.deleteTracks(selectedTracks.toList())
                    selectedTracks = emptySet()
                    showDeleteConfirm = false 
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isSelectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedTracks = emptySet() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Selection")
                }
                Text(
                    text = "${selectedTracks.size} Selected",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { trackToAdd = tracks.filter { selectedTracks.contains(it.id) } }) {
                    Text("Add to Playlist", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("Mu")
                        }
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                            append("Player")
                        }
                    },
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search title, artist, album...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = GlassSurface,
                    focusedContainerColor = GlassSurface,
                    unfocusedBorderColor = GlassBorder,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box {
                IconButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier.liquidGlass(RoundedCornerShape(16.dp)).size(56.dp)
                ) {
                    Icon(Icons.Filled.Sort, contentDescription = "Sort", tint = MaterialTheme.colorScheme.onBackground)
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(text = { Text("Title") }, onClick = { sortOption = SortOption.TITLE; sortMenuExpanded = false })
                    DropdownMenuItem(text = { Text("Artist") }, onClick = { sortOption = SortOption.ARTIST; sortMenuExpanded = false })
                    DropdownMenuItem(text = { Text("Recently Added") }, onClick = { sortOption = SortOption.RECENTLY_ADDED; sortMenuExpanded = false })
                    DropdownMenuItem(text = { Text("Duration") }, onClick = { sortOption = SortOption.DURATION; sortMenuExpanded = false })
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        viewModel.loadLibrary()
                        delay(1000)
                        isRefreshing = false
                    }
                }
            ) {
                if (tracks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No music found on device.", color = MaterialTheme.colorScheme.onBackground)
                    }
                } else if (filteredTracks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results found.", color = MaterialTheme.colorScheme.onBackground)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(filteredTracks, key = { it.id }) { track ->
                            LibraryTrackItem(
                            track = track,
                            onClick = { 
                                if (isSelectionMode) {
                                    selectedTracks = if (selectedTracks.contains(track.id)) selectedTracks - track.id else selectedTracks + track.id
                                } else {
                                    viewModel.playTrack(track, queue = filteredTracks)
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedTracks = setOf(track.id)
                                }
                            },
                            isSelected = selectedTracks.contains(track.id),
                            isSelectionMode = isSelectionMode,
                            onAddToPlaylist = { trackToAdd = listOf(track) },
                            onEditTags = { trackToEdit = track }
                        )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryTrackItem(
    track: AudioTrack, 
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onAddToPlaylist: () -> Unit = {},
    onEditTags: () -> Unit = {},
    onRemoveFromPlaylist: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .liquidGlass(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp)
            .semantics { contentDescription = "Play ${track.title} by ${track.artist}" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        AsyncImage(
            model = track.albumArtUri,
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GlassSurface)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatDuration(track.durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to Playlist") }, 
                        onClick = { menuExpanded = false; onAddToPlaylist() }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit Tags") }, 
                        onClick = { menuExpanded = false; onEditTags() }
                    )
                    if (onRemoveFromPlaylist != null) {
                        DropdownMenuItem(
                            text = { Text("Remove from Playlist") }, 
                            onClick = { menuExpanded = false; onRemoveFromPlaylist() }
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
