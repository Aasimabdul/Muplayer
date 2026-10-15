package com.example.muplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.muplayer.data.DailyListeningStat
import com.example.muplayer.data.PlayHistoryEntry
import com.example.muplayer.data.TopItemCount
import com.example.muplayer.ui.theme.uniqueLiquidDisplay
import com.example.muplayer.viewmodel.MusicViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: MusicViewModel, onBack: (() -> Unit)? = null) {
    val statsTimeRange by viewModel.statsTimeRange.collectAsState()
    val totalTimeMs by viewModel.totalListeningTimeMs.collectAsState()
    val history by viewModel.historySince.collectAsState()
    val dailyStats by viewModel.dailyStats.collectAsState()
    val topArtists by viewModel.topArtists.collectAsState()
    val topAlbums by viewModel.topAlbums.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        Dialog(onDismissRequest = { showClearConfirm = false }) {
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
                        text = "Clear Listening Statistics?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "This will permanently delete your offline listening history and stats. Your playlists, favorites, and music files will NOT be affected.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.clearListeningStatistics()
                                showClearConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listening Statistics") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Time Range Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = statsTimeRange == 7,
                        onClick = { viewModel.setStatsTimeRange(7) },
                        label = { Text("Last 7 Days") }
                    )
                    FilterChip(
                        selected = statsTimeRange == 30,
                        onClick = { viewModel.setStatsTimeRange(30) },
                        label = { Text("Last 30 Days") }
                    )
                    FilterChip(
                        selected = statsTimeRange == 0,
                        onClick = { viewModel.setStatsTimeRange(0) },
                        label = { Text("All Time") }
                    )
                }
            }

            // 2. Summary KPI Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Total Time",
                        value = formatDurationHours(totalTimeMs),
                        icon = Icons.Filled.AccessTime,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Songs Played",
                        value = "${history.size}",
                        icon = Icons.Filled.MusicNote,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Daily Activity Bar Chart
            if (dailyStats.isNotEmpty()) {
                item {
                    Text(
                        "Daily Listening Activity",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )

                    DailyActivityChart(dailyStats)
                }
            }

            // 4. Top Artists
            if (topArtists.isNotEmpty()) {
                item {
                    Text(
                        "Top Artists",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                    )
                }
                itemsIndexed(topArtists.take(5)) { index, artist ->
                    TopItemRow(index = index + 1, item = artist)
                }
            }

            // 5. Top Albums
            if (topAlbums.isNotEmpty()) {
                item {
                    Text(
                        "Top Albums",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                    )
                }
                itemsIndexed(topAlbums.take(5)) { index, album ->
                    TopItemRow(index = index + 1, item = album)
                }
            }

            // 6. Chronological History
            item {
                Text(
                    "Recent History",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                )
            }

            if (history.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No listening history recorded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(history.take(30), key = { it.id }) { entry ->
                    HistoryEntryRow(entry)
                }
            }

            // 7. Clear History Option
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                ListItem(
                    headlineContent = {
                        Text("Clear Listening Statistics", color = MaterialTheme.colorScheme.error)
                    },
                    supportingContent = {
                        Text("Reset history & stats without affecting playlists or songs")
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.clickable { showClearConfirm = true },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        }
    }
}

@Composable
fun KpiCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .uniqueLiquidDisplay(
                shape = RoundedCornerShape(20.dp),
                accentColor = MaterialTheme.colorScheme.primary
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun DailyActivityChart(stats: List<DailyListeningStat>) {
    val maxMs = remember(stats) { (stats.maxOfOrNull { it.totalListeningTimeMs } ?: 1L).coerceAtLeast(1L) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .uniqueLiquidDisplay(
                shape = RoundedCornerShape(20.dp),
                accentColor = MaterialTheme.colorScheme.primary
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            stats.takeLast(7).forEach { stat ->
                val ratio = (stat.totalListeningTimeMs.toFloat() / maxMs.toFloat()).coerceIn(0.08f, 1.0f)
                val dayLabel = remember(stat.dateString) {
                    try {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(stat.dateString)
                        SimpleDateFormat("EEE", Locale.getDefault()).format(date ?: Date())
                    } catch (_: Exception) {
                        ""
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        modifier = Modifier
                            .fillMaxHeight(ratio * 0.8f)
                            .width(18.dp)
                    ) {}
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TopItemRow(index: Int, item: TopItemCount) {
    ListItem(
        headlineContent = {
            Text(item.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text("${item.totalCount} plays • ${formatDurationHours(item.totalTimeMs)}")
        },
        leadingContent = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("#$index", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
fun HistoryEntryRow(entry: PlayHistoryEntry) {
    val dateStr = remember(entry.timestamp) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(entry.timestamp))
    }

    ListItem(
        headlineContent = {
            Text(entry.title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text("${entry.artist} • $dateStr", maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            Text(formatDurationMinSec(entry.durationListenedMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
    )
}

private fun formatDurationHours(ms: Long): String {
    val totalMins = ms / (1000 * 60)
    val hours = totalMins / 60
    val mins = totalMins % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}

private fun formatDurationMinSec(ms: Long): String {
    val totalSecs = ms / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format(Locale.getDefault(), "%d:%02d", mins, secs)
}
