package com.example.muplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.muplayer.model.AudioTrack
import com.example.muplayer.ui.theme.uniqueLiquidDisplay
import com.example.muplayer.ui.theme.bounceClick
import com.example.muplayer.viewmodel.MusicViewModel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush

@Composable
fun MiniPlayer(
    track: AudioTrack,
    viewModel: MusicViewModel,
    onClick: () -> Unit,
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary

    val targetProgressFraction = if (duration > 0) {
        (progress.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgressFraction,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "miniPlayerProgress"
    )

    val shape = RoundedCornerShape(36.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .bounceClick { onClick() }
            .uniqueLiquidDisplay(shape = shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Album Art with glowing border
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
            ) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Song Title and Artist
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = primaryColor,
                        modifier = Modifier.size(6.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Playback Controls
            IconButton(
                onClick = { viewModel.skipToPrevious() },
                modifier = Modifier.size(32.dp).bounceClick { viewModel.skipToPrevious() }
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            Surface(
                shape = CircleShape,
                color = primaryColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(42.dp)
                    .bounceClick { viewModel.togglePlayPause() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            IconButton(
                onClick = { viewModel.skipToNext() },
                modifier = Modifier.size(32.dp).bounceClick { viewModel.skipToNext() }
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Animated / Gradient Liquid Progress Line
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(2.5.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 1.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
            color = primaryColor,
            trackColor = Color.Transparent,
        )
    }
}




