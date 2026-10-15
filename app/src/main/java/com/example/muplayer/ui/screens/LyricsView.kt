package com.example.muplayer.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.muplayer.model.LyricLine
import com.example.muplayer.ui.theme.uniqueLiquidDisplay
import com.example.muplayer.viewmodel.MusicViewModel

@Composable
fun LyricsView(
    lyrics: List<LyricLine>?,
    progress: Long,
    viewModel: MusicViewModel
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val lyricsOffsetMs by viewModel.lyricsOffsetMs.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary

    var showOffsetControls by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importLyrics(it) }
    }

    val effectiveProgress = (progress + lyricsOffsetMs).coerceAtLeast(0L)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toolbar with Timing Offset & File Import Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (lyrics.isNullOrEmpty()) "Lyrics" else if (lyrics.any { it.timeMs != -1L }) "Synced Lyrics" else "Plain Text Lyrics",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showOffsetControls = !showOffsetControls }) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = "Adjust Sync Offset",
                        tint = if (showOffsetControls || lyricsOffsetMs != 0L) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { filePickerLauncher.launch(arrayOf("*/*")) }) {
                    Icon(
                        imageVector = Icons.Filled.FileUpload,
                        contentDescription = "Import Lyrics",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!lyrics.isNullOrEmpty()) {
                    IconButton(onClick = { viewModel.deleteLyricsForCurrentTrack() }) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete Lyrics",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Timing Offset Adjustment Control Bar
        AnimatedVisibility(
            visible = showOffsetControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .uniqueLiquidDisplay(
                        shape = CircleShape,
                        accentColor = primaryColor
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { viewModel.adjustLyricsOffset(-500L) }) {
                        Text("-0.5s", color = MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(onClick = { viewModel.adjustLyricsOffset(-100L) }) {
                        Text("-0.1s", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = String.format(java.util.Locale.getDefault(), "Offset: %+.1fs", lyricsOffsetMs / 1000f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    TextButton(onClick = { viewModel.adjustLyricsOffset(100L) }) {
                        Text("+0.1s", color = MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(onClick = { viewModel.adjustLyricsOffset(500L) }) {
                        Text("+0.5s", color = MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(onClick = { viewModel.resetLyricsOffset() }) {
                        Text("Reset", color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Main Lyrics Content
        if (lyrics.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .uniqueLiquidDisplay(
                            shape = RoundedCornerShape(28.dp),
                            accentColor = primaryColor
                        )
                        .padding(28.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Subject,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Lyrics Available",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Import a local .lrc or .txt file to sync lyrics with this song.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Lyrics File")
                        }
                    }
                }
            }
        } else {
            val listState = rememberLazyListState()
            val hasTimestamps = remember(lyrics) { lyrics.any { it.timeMs != -1L } }

            val currentLineIndex = remember(lyrics, effectiveProgress) {
                if (hasTimestamps) {
                    lyrics.indexOfLast { effectiveProgress >= it.timeMs }.coerceAtLeast(0)
                } else {
                    -1
                }
            }

            LaunchedEffect(currentLineIndex) {
                if (hasTimestamps && currentLineIndex >= 0) {
                    listState.animateScrollToItem(maxOf(0, currentLineIndex - 2))
                }
            }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 40.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isCurrent = hasTimestamps && index == currentLineIndex
                    Text(
                        text = line.text,
                        color = if (isCurrent) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                        style = if (isCurrent) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = line.timeMs != -1L) {
                                viewModel.seekTo((line.timeMs - lyricsOffsetMs).coerceAtLeast(0L))
                            }
                            .padding(vertical = 10.dp)
                    )
                }
            }
        }

        // Compact Bottom Controls Dock
        if (currentTrack != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .uniqueLiquidDisplay(
                        shape = CircleShape,
                        accentColor = primaryColor
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = currentTrack?.albumArtUri,
                        contentDescription = "Artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTrack?.title ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentTrack?.artist ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { viewModel.skipToPrevious() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(20.dp))
                    }
                    Surface(
                        onClick = { viewModel.togglePlayPause() },
                        shape = CircleShape,
                        color = primaryColor,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.skipToNext() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
