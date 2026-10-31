package com.example.muplayer.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.muplayer.ui.components.EqualizerDialog
import com.example.muplayer.ui.theme.AppThemePalette
import com.example.muplayer.ui.theme.AppThemes
import com.example.muplayer.ui.theme.uniqueLiquidDisplay
import com.example.muplayer.viewmodel.MusicViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    onNavigateToStats: () -> Unit = {}
) {
    val context = LocalContext.current
    val playbackPrefs = remember { context.getSharedPreferences("muplayer_playback_settings", 0) }
    var excludedFolders by remember { mutableStateOf(playbackPrefs.getString("excluded_folders", "") ?: "") }
    var resumeLast by remember { mutableStateOf(playbackPrefs.getBoolean("resume_last", true)) }
    var rememberQueue by remember { mutableStateOf(playbackPrefs.getBoolean("remember_queue", true)) }
    val currentTracks by viewModel.tracks.collectAsState()
    val sleepTimerRemaining by viewModel.sleepTimerTimeRemaining.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val defaultSort by viewModel.defaultSort.collectAsState()
    val isAmbientModeEnabled by viewModel.isAmbientModeEnabled.collectAsState()
    val isAmbientAnimationEnabled by viewModel.isAmbientAnimationEnabled.collectAsState()
    val isLiquidGlassEnabled by viewModel.isLiquidGlassEnabled.collectAsState()
    val isThemeAnimationEnabled by viewModel.isThemeAnimationEnabled.collectAsState()
    val isCrossfadeEnabled by viewModel.isCrossfadeEnabled.collectAsState()
    val crossfadeDurationSec by viewModel.crossfadeDurationSec.collectAsState()

    val isDoubleTapEnabled by viewModel.gestureDoubleTapEnabled.collectAsState()
    val doubleTapAction by viewModel.gestureDoubleTapAction.collectAsState()
    val isSwipeHorizEnabled by viewModel.gestureSwipeHorizEnabled.collectAsState()
    val isSwipeVertEnabled by viewModel.gestureSwipeVertEnabled.collectAsState()
    val isLongPressEnabled by viewModel.gestureLongPressEnabled.collectAsState()
    val isShakeEnabled by viewModel.gestureShakeEnabled.collectAsState()

    val backupPreview by viewModel.backupPreview.collectAsState()
    val restoreSummary by viewModel.restoreSummary.collectAsState()

    val isSystemDark = isSystemInDarkTheme()

    var showSortDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showResetThemeConfirm by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showCrossfadeDurationDialog by remember { mutableStateOf(false) }
    var showDoubleTapActionDialog by remember { mutableStateOf(false) }
    var showResetGesturesConfirm by remember { mutableStateOf(false) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportBackup(it) { success ->
                if (success) {
                    Toast.makeText(context, "Backup exported successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to export backup", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.previewBackup(it) { success ->
                if (!success) {
                    Toast.makeText(context, "Invalid or unreadable backup file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showEqualizerDialog) {
        EqualizerDialog(onDismiss = { showEqualizerDialog = false })
    }

    val allThemePalettes = remember(isSystemDark) {
        listOf(
            AppThemes.NavyBlue,
            AppThemes.getSystemAdaptive(isSystemDark),
            AppThemes.NeonBlue,
            AppThemes.AmoledBlack,
            AppThemes.PureWhite,
            AppThemes.PurpleGalaxy,
            AppThemes.EmeraldForest,
            AppThemes.SunsetAmber,
            AppThemes.CrimsonRed,
            AppThemes.ArcticCyan
        )
    }

    // Restore Backup Preview Dialog
    if (backupPreview != null) {
        val preview = backupPreview!!
        val dateStr = remember(preview.timestamp) {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(preview.timestamp))
        }

        Dialog(onDismissRequest = { viewModel.clearBackupPreview() }) {
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
                        text = "Restore Backup Preview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Created on: $dateStr (v${preview.schemaVersion})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "• Playlists: ${preview.playlistsCount} (${preview.totalPlaylistTracks} songs)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Favorite Songs: ${preview.favoritesCount}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Theme: ${preview.themeName.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "Choose Restore Mode:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.executeRestore(mergeMode = true) {
                                    Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Merge")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.executeRestore(mergeMode = false) {
                                    Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Replace")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.clearBackupPreview() }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    // Restore Summary Dialog
    if (restoreSummary != null) {
        val summary = restoreSummary!!
        Dialog(onDismissRequest = { viewModel.clearRestoreSummary() }) {
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
                        text = "Restore Complete",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("• Playlists restored: ${summary.playlistsRestored}")
                        Text("• Favorites restored: ${summary.favoritesRestored}")
                        Text("• Track matches found: ${summary.matchedTracksCount}")
                        if (summary.unmatchedTracksCount > 0) {
                            Text(
                                text = "• ${summary.unmatchedTracksCount} tracks not found in current device files",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = { viewModel.clearRestoreSummary() }) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }

    if (showSortDialog) {
        Dialog(onDismissRequest = { showSortDialog = false }) {
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
                        text = "Default Library Sort",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Column {
                        SortOptionItem("TITLE", "Title", defaultSort, viewModel) { showSortDialog = false }
                        SortOptionItem("ARTIST", "Artist", defaultSort, viewModel) { showSortDialog = false }
                        SortOptionItem("RECENTLY_ADDED", "Recently Added", defaultSort, viewModel) { showSortDialog = false }
                        SortOptionItem("DURATION", "Duration", defaultSort, viewModel) { showSortDialog = false }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showSortDialog = false }) { Text("Close") }
                    }
                }
            }
        }
    }

    if (showResetThemeConfirm) {
        Dialog(onDismissRequest = { showResetThemeConfirm = false }) {
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
                        text = "Reset Theme Settings?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "This will restore Navy Blue as your active theme and re-enable Liquid Glass effects and theme animations.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showResetThemeConfirm = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.resetThemeToDefault()
                                showResetThemeConfirm = false
                            }
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }
        }
    }
    
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
                        text = "Clear App Preferences",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "This will reset your play history, theme, and sort preferences. Playlists and Favorites will also be cleared. Are you sure?",
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
                                viewModel.clearAppPreferences()
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

    if (showCrossfadeDurationDialog) {
        Dialog(onDismissRequest = { showCrossfadeDurationDialog = false }) {
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
                        text = "Crossfade Duration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Select transition duration between tracks:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column {
                        listOf(2, 4, 6, 8, 10).forEach { duration ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setCrossfadeDuration(duration)
                                        showCrossfadeDurationDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$duration Seconds",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (crossfadeDurationSec == duration) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCrossfadeDurationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    if (showDoubleTapActionDialog) {
        Dialog(onDismissRequest = { showDoubleTapActionDialog = false }) {
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
                        text = "Double-Tap Action",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val options = listOf(
                        "PLAY_PAUSE" to "Play / Pause",
                        "NEXT" to "Skip to Next Track",
                        "PREV" to "Skip to Previous Track",
                        "FAVORITE" to "Toggle Favorite"
                    )

                    Column {
                        options.forEach { (actionKey, actionName) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setGestureDoubleTapAction(actionKey)
                                        showDoubleTapActionDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = actionName,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (doubleTapAction == actionKey) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDoubleTapActionDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    if (showResetGesturesConfirm) {
        Dialog(onDismissRequest = { showResetGesturesConfirm = false }) {
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
                        text = "Reset Gesture Settings?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "This will restore default gesture controls (Double-tap Play/Pause, Swiping, Long-press menu enabled, Shake-to-skip disabled).",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showResetGesturesConfirm = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.resetGesturesToDefault()
                                showResetGesturesConfirm = false
                            }
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            item {
                Text(
                    "Theme & Appearance",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp)
                )

                // 2-Column Responsive Theme Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    allThemePalettes.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { pal ->
                                val isSelected = currentTheme == pal.id || (currentTheme == "midnight" && pal.id == "navy_blue") || (currentTheme == "light" && pal.id == "pure_white") || (currentTheme == "amoled" && pal.id == "amoled_black")
                                ThemePreviewCard(
                                    palette = pal,
                                    isSelected = isSelected,
                                    onSelect = { viewModel.setTheme(pal.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Toggles: Ambient Mode, Liquid Glass, Theme Animations
                ListItem(
                    modifier = Modifier
                        .clickable { viewModel.setAmbientModeEnabled(!isAmbientModeEnabled) }
                        .semantics { contentDescription = "Toggle Ambient Mode" },
                    headlineContent = { Text("Ambient Mode Lighting") },
                    supportingContent = { Text("Dynamic background glow based on album artwork") },
                    trailingContent = {
                        Switch(
                            checked = isAmbientModeEnabled,
                            onCheckedChange = { viewModel.setAmbientModeEnabled(it) }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
                ListItem(
                    modifier = Modifier
                        .clickable(enabled = isAmbientModeEnabled) { viewModel.setAmbientAnimationEnabled(!isAmbientAnimationEnabled) }
                        .semantics { contentDescription = "Toggle Ambient Animation" },
                    headlineContent = { Text("Ambient Mode Animation") },
                    supportingContent = { Text("Animate the ambient background colors") },
                    trailingContent = {
                        Switch(
                            checked = isAmbientAnimationEnabled,
                            onCheckedChange = { viewModel.setAmbientAnimationEnabled(it) },
                            enabled = isAmbientModeEnabled
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                ListItem(
                    headlineContent = { Text("Liquid Glass Effects") },
                    supportingContent = { Text("Enable glossy translucent glass displays and tabs") },
                    trailingContent = {
                        Switch(
                            checked = isLiquidGlassEnabled,
                            onCheckedChange = { viewModel.setLiquidGlassEnabled(it) }
                        )
                    },
                    modifier = Modifier
                        .clickable { viewModel.setLiquidGlassEnabled(!isLiquidGlassEnabled) }
                        .semantics { contentDescription = "Toggle Liquid Glass Effects" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                ListItem(
                    headlineContent = { Text("Theme Transition Animations") },
                    supportingContent = { Text("Smooth motion effects when switching themes") },
                    trailingContent = {
                        Switch(
                            checked = isThemeAnimationEnabled,
                            onCheckedChange = { viewModel.setThemeAnimationEnabled(it) }
                        )
                    },
                    modifier = Modifier
                        .clickable { viewModel.setThemeAnimationEnabled(!isThemeAnimationEnabled) }
                        .semantics { contentDescription = "Toggle Theme Animations" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Reset Theme Button
                ListItem(
                    headlineContent = { Text("Reset Theme to Default", color = MaterialTheme.colorScheme.primary) },
                    supportingContent = { Text("Restore Navy Blue theme & default appearance effects") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .clickable { showResetThemeConfirm = true }
                        .semantics { contentDescription = "Reset Theme Settings" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Gesture Controls Section
                Text(
                    "Gesture Controls",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Double-Tap Artwork") },
                    supportingContent = {
                        Text(
                            when (doubleTapAction) {
                                "NEXT" -> "Skip to Next Track"
                                "PREV" -> "Skip to Previous Track"
                                "FAVORITE" -> "Toggle Favorite"
                                else -> "Play / Pause"
                            }
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = isDoubleTapEnabled,
                            onCheckedChange = { viewModel.setGestureDoubleTapEnabled(it) }
                        )
                    },
                    modifier = Modifier
                        .clickable { viewModel.setGestureDoubleTapEnabled(!isDoubleTapEnabled) }
                        .semantics { contentDescription = "Toggle Double Tap Artwork Gesture" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                if (isDoubleTapEnabled) {
                    ListItem(
                        headlineContent = { Text("Double-Tap Action") },
                        supportingContent = {
                            Text(
                                when (doubleTapAction) {
                                    "NEXT" -> "Skip to Next Track"
                                    "PREV" -> "Skip to Previous Track"
                                    "FAVORITE" -> "Toggle Favorite"
                                    else -> "Play / Pause"
                                }
                            )
                        },
                        modifier = Modifier
                            .clickable { showDoubleTapActionDialog = true }
                            .semantics { contentDescription = "Change Double Tap Action" },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                ListItem(
                    headlineContent = { Text("Swipe Left / Right to Skip Tracks") },
                    supportingContent = { Text("Swipe left for Next track, right for Previous track") },
                    trailingContent = {
                        Switch(
                            checked = isSwipeHorizEnabled,
                            onCheckedChange = { viewModel.setGestureSwipeHorizEnabled(it) }
                        )
                    },
                    modifier = Modifier
                        .clickable { viewModel.setGestureSwipeHorizEnabled(!isSwipeHorizEnabled) }
                        .semantics { contentDescription = "Toggle Swipe Track Change Gesture" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                ListItem(
                    headlineContent = { Text("Swipe Up / Down to Adjust Volume") },
                    supportingContent = { Text("Swipe up to increase volume, down to decrease") },
                    trailingContent = {
                        Switch(
                            checked = isSwipeVertEnabled,
                            onCheckedChange = { viewModel.setGestureSwipeVertEnabled(it) }
                        )
                    },
                    modifier = Modifier
                        .clickable { viewModel.setGestureSwipeVertEnabled(!isSwipeVertEnabled) }
                        .semantics { contentDescription = "Toggle Swipe Volume Gesture" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                ListItem(
                    headlineContent = { Text("Long-Press for Quick Actions Menu") },
                    supportingContent = { Text("Long press artwork to open quick menu") },
                    trailingContent = {
                        Switch(
                            checked = isLongPressEnabled,
                            onCheckedChange = { viewModel.setGestureLongPressEnabled(it) }
                        )
                    },
                    modifier = Modifier
                        .clickable { viewModel.setGestureLongPressEnabled(!isLongPressEnabled) }
                        .semantics { contentDescription = "Toggle Long Press Quick Actions Gesture" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                ListItem(
                    headlineContent = { Text("Shake Device to Skip Track") },
                    supportingContent = { Text("Accelerometer sensor trigger (disabled when app inactive)") },
                    trailingContent = {
                        Switch(
                            checked = isShakeEnabled,
                            onCheckedChange = { viewModel.setGestureShakeEnabled(it) }
                        )
                    },
                    modifier = Modifier
                        .clickable { viewModel.setGestureShakeEnabled(!isShakeEnabled) }
                        .semantics { contentDescription = "Toggle Shake to Skip Gesture" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                ListItem(
                    headlineContent = { Text("Reset Gestures to Default", color = MaterialTheme.colorScheme.primary) },
                    supportingContent = { Text("Restore default gesture mappings & thresholds") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset Gestures",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .clickable { showResetGesturesConfirm = true }
                        .semantics { contentDescription = "Reset Gestures" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Text(
                    "Library & Audio",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Gapless Playback") },
                    supportingContent = { Text("Active — Eliminates unwanted silence between tracks where supported") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                ListItem(
                    headlineContent = { Text("Scan Music Library") },
                    supportingContent = { Text("Rescan device audio files · ${currentTracks.size} tracks currently indexed") },
                    leadingContent = { Icon(Icons.Filled.Refresh, contentDescription = "Scan library") },
                    modifier = Modifier.clickable {
                        viewModel.loadLibrary()
                        Toast.makeText(context, "Music library scan started", Toast.LENGTH_SHORT).show()
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                ListItem(
                    headlineContent = { Text("Resume Last Played Song") },
                    supportingContent = { Text("Restore the last track and playback position on app reopen") },
                    trailingContent = { Switch(checked = resumeLast, onCheckedChange = {
                        resumeLast = it
                        playbackPrefs.edit().putBoolean("resume_last", it).apply()
                    }) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                ListItem(
                    headlineContent = { Text("Remember Last Queue") },
                    supportingContent = { Text("Restore the previous queue when reopening MuPlayer") },
                    trailingContent = { Switch(checked = rememberQueue, onCheckedChange = {
                        rememberQueue = it
                        playbackPrefs.edit().putBoolean("remember_queue", it).apply()
                    }) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Excluded Folders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Comma-separated folder names or paths. Applied on the next scan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = excludedFolders,
                        onValueChange = { excludedFolders = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        label = { Text("e.g. WhatsApp, Podcasts, Recordings") },
                        minLines = 2
                    )
                    Button(onClick = {
                        playbackPrefs.edit().putString("excluded_folders", excludedFolders).apply()
                        viewModel.loadLibrary()
                        Toast.makeText(context, "Folder exclusions saved; library scan started", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Save & Rescan Library") }
                }

                ListItem(
                    headlineContent = { Text("Sleep Timer") },
                    supportingContent = { Text(sleepTimerRemaining?.let { "Active · " + ((it + 59999L) / 60000L) + " min remaining" } ?: "Gradually fade playback before stopping") },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            listOf(15, 30, 60).forEach { minutes ->
                                TextButton(onClick = { viewModel.startSleepTimer(minutes); Toast.makeText(context, "Sleep timer: $minutes minutes", Toast.LENGTH_SHORT).show() }) { Text("${minutes}m") }
                            }
                            TextButton(onClick = { viewModel.cancelSleepTimer() }) { Text("Off") }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                ListItem(
                    headlineContent = { Text("Clear Cache & Artwork Cache") },
                    supportingContent = { Text("Remove temporary cached files; music files and playlists are kept") },
                    leadingContent = { Icon(Icons.Filled.Refresh, contentDescription = "Clear cache") },
                    modifier = Modifier.clickable {
                        val ok = try {
                            context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
                            true
                        } catch (_: Exception) { false }
                        Toast.makeText(context, if (ok) "App cache cleared" else "Some cache files could not be removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                ListItem(
                    headlineContent = { Text("Crossfade Tracks") },
                    supportingContent = { Text("Smoothly fade out ending track and fade in upcoming track") },
                    trailingContent = {
                        Switch(
                            checked = isCrossfadeEnabled,
                            onCheckedChange = { viewModel.setCrossfadeEnabled(it) }
                        )
                    },
                    modifier = Modifier
                        .clickable { viewModel.setCrossfadeEnabled(!isCrossfadeEnabled) }
                        .semantics { contentDescription = "Toggle Crossfade" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                if (isCrossfadeEnabled) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    ListItem(
                        headlineContent = { Text("Crossfade Duration") },
                        supportingContent = { Text("$crossfadeDurationSec Seconds") },
                        modifier = Modifier
                            .clickable { showCrossfadeDurationDialog = true }
                            .semantics { contentDescription = "Set Crossfade Duration" },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                ListItem(
                    headlineContent = { Text("Equalizer & Bass Boost") },
                    supportingContent = { Text("Hardware-backed 10-preset EQ & Bass Booster level") },
                    modifier = Modifier
                        .clickable { showEqualizerDialog = true }
                        .semantics { contentDescription = "Open Equalizer" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                ListItem(
                    headlineContent = { Text("Default Sort") },
                    supportingContent = { 
                        Text(when(defaultSort) {
                            "ARTIST" -> "Artist"
                            "RECENTLY_ADDED" -> "Recently Added"
                            "DURATION" -> "Duration"
                            else -> "Title"
                        })
                    },
                    modifier = Modifier
                        .clickable { showSortDialog = true }
                        .semantics { contentDescription = "Change Default Sort" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Text(
                    "Backup & Restore",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Export Offline Backup") },
                    supportingContent = { Text("Save playlists, favorites & preferences to a .json file") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Backup,
                            contentDescription = "Backup",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            createBackupLauncher.launch("MuPlayer_Backup_$dateStr.json")
                        }
                        .semantics { contentDescription = "Export Backup" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                ListItem(
                    headlineContent = { Text("Restore from Backup") },
                    supportingContent = { Text("Import and preview playlists & settings from a .json file") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Restore,
                            contentDescription = "Restore",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            openBackupLauncher.launch(arrayOf("application/json", "*/*"))
                        }
                        .semantics { contentDescription = "Restore Backup" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                
                Text(
                    "Data",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Listening Statistics & History") },
                    supportingContent = { Text("View local listening time, top tracks & artists") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Restore,
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .clickable { onNavigateToStats() }
                        .semantics { contentDescription = "Open Listening Statistics" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                ListItem(
                    headlineContent = { Text("Clear App Preferences") },
                    modifier = Modifier
                        .clickable { showClearConfirm = true }
                        .semantics { contentDescription = "Clear all app data" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                
                Text(
                    "About",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                ListItem(
                    headlineContent = {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append("Mu")
                                }
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                                    append("Player")
                                }
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    supportingContent = { Text("Version 6.1.0 · Open-source") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
                Text(
                    "Open-source credits: Kotlin, Jetpack Compose, AndroidX Media3, Room, DataStore, Coil and AndroidX Palette. MuPlayer is an open-source project; see the repository LICENSE for terms.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ThemePreviewCard(
    palette: AppThemePalette,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) palette.accent else palette.border.copy(alpha = 0.3f)
    
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(18.dp),
        color = palette.background,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Select ${palette.name} theme" }
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header: Title & Selected Checkmark Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = palette.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = palette.accent,
                            contentColor = if (palette.isLight) Color.White else Color.Black,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Miniature Music Player Preview
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.surface,
                    border = BorderStroke(0.5.dp, palette.border.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Album Art Placeholder
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = palette.accent.copy(alpha = 0.25f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.MusicNote,
                                        contentDescription = null,
                                        tint = palette.accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Track Info Lines
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(2.dp),
                                    color = palette.primaryText,
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(6.dp)
                                ) {}
                                Spacer(modifier = Modifier.height(3.dp))
                                Surface(
                                    shape = RoundedCornerShape(2.dp),
                                    color = palette.secondaryText,
                                    modifier = Modifier
                                        .fillMaxWidth(0.45f)
                                        .height(4.dp)
                                ) {}
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Mini Play/Pause Circle
                            Surface(
                                shape = CircleShape,
                                color = palette.accent,
                                modifier = Modifier.size(18.dp)
                            ) {}
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Mini Progress Line
                        Surface(
                            shape = RoundedCornerShape(2.dp),
                            color = palette.progress,
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(2.5.dp)
                        ) {}
                    }
                }

                // Color Swatches Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorSwatchDot(color = palette.primary)
                    ColorSwatchDot(color = palette.surface)
                    ColorSwatchDot(color = palette.accent)
                }
            }
        }
    }
}

@Composable
fun ColorSwatchDot(color: Color) {
    Surface(
        shape = CircleShape,
        color = color,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)),
        modifier = Modifier.size(14.dp)
    ) {}
}

@Composable
fun SortOptionItem(
    id: String,
    name: String,
    currentSort: String,
    viewModel: MusicViewModel,
    onSelected: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.setDefaultSort(id); onSelected() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
        if (currentSort == id) {
            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
