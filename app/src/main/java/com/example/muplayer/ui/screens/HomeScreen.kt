package com.example.muplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.muplayer.model.AudioTrack
import com.example.muplayer.ui.components.AddToPlaylistDialog
import com.example.muplayer.ui.theme.GlassSurface
import com.example.muplayer.ui.theme.liquidGlass
import com.example.muplayer.ui.theme.uniqueLiquidDisplay
import com.example.muplayer.viewmodel.MusicViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val tracks by viewModel.tracks.collectAsState()
    val favoriteIds by viewModel.favorites.collectAsState()
    val recentlyPlayedIds by viewModel.recentlyPlayedIds.collectAsState()
    val playCounts by viewModel.playCounts.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var expandedSection by remember { mutableStateOf<String?>(null) }
    var tracksToAddToPlaylist by remember { mutableStateOf<List<AudioTrack>?>(null) }

    tracksToAddToPlaylist?.let { selectedTracks ->
        AddToPlaylistDialog(
            tracks = selectedTracks,
            viewModel = viewModel,
            onDismiss = { tracksToAddToPlaylist = null }
        )
    }

    if (!hasPermission) {
        PermissionDeniedState(onRequestPermission)
        return
    }

    if (tracks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No music found. Add some music to your device.", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    if (expandedSection != null) {
        BackHandler { expandedSection = null }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                IconButton(onClick = { expandedSection = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = expandedSection!!,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            val listToDisplay = when (expandedSection) {
                "Favorite Songs" -> favoriteIds.mapNotNull { id -> tracks.find { it.id == id } }
                "Frequently Played" -> recentlyPlayedIds.mapNotNull { id -> tracks.find { it.id == id } }
                "Most Played Songs" -> {
                    val most = tracks
                        .filter { (playCounts[it.id] ?: 0) > 0 }
                        .sortedWith(
                            compareByDescending<AudioTrack> { playCounts[it.id] ?: 0 }
                                .thenBy { it.title }
                                .thenBy { it.id }
                        )
                    if (most.isNotEmpty()) most else tracks.take(50)
                }
                "Recently Added" -> tracks.sortedByDescending { it.dateAdded }
                else -> emptyList()
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                itemsIndexed(listToDisplay, key = { _, t -> t.id }) { index, track ->
                    if (expandedSection == "Most Played Songs") {
                        val count = playCounts[track.id] ?: 1
                        RankTrackItem(
                            rank = index + 1,
                            track = track,
                            playCount = count,
                            isFavorite = favoriteIds.contains(track.id),
                            onFavoriteToggle = { viewModel.toggleFavorite(track.id) },
                            onClick = { viewModel.playTrack(track, queue = listToDisplay) },
                            onAddToPlaylist = { tracksToAddToPlaylist = listOf(track) }
                        )
                    } else {
                        TrackItem(
                            track = track,
                            isFavorite = favoriteIds.contains(track.id),
                            onFavoriteToggle = { viewModel.toggleFavorite(track.id) },
                            onClick = { viewModel.playTrack(track, queue = listToDisplay) },
                            onAddToPlaylist = { tracksToAddToPlaylist = listOf(track) }
                        )
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            HomeHeader()
        }

        item {
            QuickLinksRow(onNavigate)
        }

        if (currentTrack != null) {
            item {
                NowPlayingCard(currentTrack!!, isPlaying) {
                    viewModel.togglePlayPause()
                }
            }
        }

        // 1. Favorite Songs Section
        val favoriteTracks = favoriteIds.mapNotNull { id -> tracks.find { it.id == id } }
        if (favoriteTracks.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Favorite Songs", 
                    icon = Icons.Filled.Favorite,
                    onArrowClick = { expandedSection = "Favorite Songs" }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteTracks.take(4), key = { "fav_${it.id}" }) { track ->
                        SquareTrackCard(
                            track = track,
                            isFavorite = true,
                            onFavoriteToggle = { viewModel.toggleFavorite(track.id) },
                            onClick = { viewModel.playTrack(track, queue = favoriteTracks) },
                            onAddToPlaylist = { tracksToAddToPlaylist = listOf(track) }
                        )
                    }
                }
            }
        }

        // 2. Frequently Played Section
        val recentlyPlayed = recentlyPlayedIds.mapNotNull { id -> tracks.find { it.id == id } }
        if (recentlyPlayed.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Frequently Played", 
                    icon = Icons.Filled.History,
                    onArrowClick = { expandedSection = "Frequently Played" }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentlyPlayed.take(4), key = { "recent_${it.id}" }) { track ->
                        SquareTrackCard(
                            track = track,
                            isFavorite = favoriteIds.contains(track.id),
                            onFavoriteToggle = { viewModel.toggleFavorite(track.id) },
                            onClick = { viewModel.playTrack(track, queue = recentlyPlayed) },
                            onAddToPlaylist = { tracksToAddToPlaylist = listOf(track) }
                        )
                    }
                }
            }
        }

        // 3. Most Played Songs Section
        val mostPlayedTracks = tracks
            .filter { (playCounts[it.id] ?: 0) > 0 }
            .sortedWith(
                compareByDescending<AudioTrack> { playCounts[it.id] ?: 0 }
                    .thenBy { it.title }
                    .thenBy { it.id }
            )

        val topTracksToDisplay = if (mostPlayedTracks.isNotEmpty()) mostPlayedTracks else tracks.take(50)

        if (topTracksToDisplay.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Most Played Songs", 
                    icon = Icons.Filled.Leaderboard,
                    onArrowClick = { expandedSection = "Most Played Songs" }
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    topTracksToDisplay.take(4).forEachIndexed { index, track ->
                        val count = playCounts[track.id] ?: 1
                        RankTrackItem(
                            rank = index + 1,
                            track = track,
                            playCount = count,
                            isFavorite = favoriteIds.contains(track.id),
                            onFavoriteToggle = { viewModel.toggleFavorite(track.id) },
                            onClick = { viewModel.playTrack(track, queue = topTracksToDisplay) },
                            onAddToPlaylist = { tracksToAddToPlaylist = listOf(track) }
                        )
                    }
                }
            }
        }

        // 4. Recently Added Songs Section
        val recentlyAdded = tracks.sortedByDescending { it.dateAdded }
        if (recentlyAdded.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Recently Added", 
                    icon = Icons.Filled.NewReleases,
                    onArrowClick = { expandedSection = "Recently Added" }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentlyAdded.take(4), key = { "added_${it.id}" }) { track ->
                        SquareTrackCard(
                            track = track,
                            isFavorite = favoriteIds.contains(track.id),
                            onFavoriteToggle = { viewModel.toggleFavorite(track.id) },
                            onClick = { viewModel.playTrack(track, queue = recentlyAdded) },
                            onAddToPlaylist = { tracksToAddToPlaylist = listOf(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedState(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.LibraryMusic,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "We need access to your device storage to find and play your music.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Grant Permission", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 12.dp)
            .semantics { contentDescription = "Home Header" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Welcome to",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("Mu")
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                        append("Player")
                    }
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickLinksRow(onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickLinkChip(Icons.Filled.MusicNote, "Songs") { onNavigate("songs") }
        QuickLinkChip(Icons.Filled.MusicNote, "Stats") { onNavigate("stats") }
        QuickLinkChip(Icons.Filled.Folder, "Folders") { onNavigate("folders") }
        QuickLinkChip(Icons.Filled.QueueMusic, "Playlists") { onNavigate("playlists") }
        QuickLinkChip(Icons.Filled.Settings, "Settings") { onNavigate("settings") }
    }
}

@Composable
fun QuickLinkChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .liquidGlass(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun NowPlayingCard(track: AudioTrack, isPlaying: Boolean, onPlayPause: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .uniqueLiquidDisplay(
                shape = CircleShape,
                accentColor = primaryColor,
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurface)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = primaryColor,
                    modifier = Modifier.size(6.dp)
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(44.dp)
                .background(primaryColor, CircleShape)
        ) {
            Icon(
                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "Play/Pause",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SectionTitle(title: String, icon: ImageVector, onArrowClick: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 20.dp, bottom = 12.dp, end = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (onArrowClick != null) {
            IconButton(onClick = onArrowClick) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "See All",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SquareTrackCard(
    track: AudioTrack,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(135.dp)
            .liquidGlass(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Box {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(115.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GlassSurface)
            )
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = onAddToPlaylist,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.58f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlaylistAdd,
                    contentDescription = "Add to Playlist",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RankTrackItem(
    rank: Int,
    track: AudioTrack,
    playCount: Int,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .liquidGlass(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (rank <= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp)
        )
        AsyncImage(
            model = track.albumArtUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GlassSurface)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${track.artist} • Played $playCount ${if (playCount == 1) "time" else "times"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onAddToPlaylist) {
            Icon(
                imageVector = Icons.Filled.PlaylistAdd,
                contentDescription = "Add to Playlist",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onFavoriteToggle) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TrackItem(
    track: AudioTrack,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.albumArtUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GlassSurface)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onAddToPlaylist) {
            Icon(
                imageVector = Icons.Filled.PlaylistAdd,
                contentDescription = "Add to Playlist",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onFavoriteToggle) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
