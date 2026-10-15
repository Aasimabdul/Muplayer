package com.example.muplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.window.Dialog
import com.example.muplayer.ui.theme.uniqueLiquidDisplay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.activity.compose.BackHandler
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.muplayer.model.AudioTrack
import com.example.muplayer.ui.components.AddToPlaylistDialog
import com.example.muplayer.ui.components.EqualizerDialog
import com.example.muplayer.ui.components.QuickActionsSheet
import com.example.muplayer.ui.components.ShakeToSkipListener
import com.example.muplayer.ui.components.TagEditorDialog
import com.example.muplayer.ui.components.artworkGestureDetector
import com.example.muplayer.ui.theme.GlassSurface
import com.example.muplayer.ui.theme.LocalAppThemePalette
import com.example.muplayer.ui.theme.liquidGlass
import com.example.muplayer.viewmodel.MusicViewModel

import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette

import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(viewModel: MusicViewModel, onBack: () -> Unit) {
    val track by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isFavorite by viewModel.favorites.collectAsState()
    val queue by viewModel.currentQueue.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()
    val appVolume by viewModel.appVolume.collectAsState()
    val sleepTimer by viewModel.sleepTimerTimeRemaining.collectAsState()

    val isAmbientModeEnabled by viewModel.isAmbientModeEnabled.collectAsState()
    val isAmbientAnimationEnabled by viewModel.isAmbientAnimationEnabled.collectAsState()
    val paletteInfo = LocalAppThemePalette.current
    val isLight = paletteInfo.isLight

    val isDoubleTapEnabled by viewModel.gestureDoubleTapEnabled.collectAsState()
    val doubleTapAction by viewModel.gestureDoubleTapAction.collectAsState()
    val isSwipeHorizEnabled by viewModel.gestureSwipeHorizEnabled.collectAsState()
    val isSwipeVertEnabled by viewModel.gestureSwipeVertEnabled.collectAsState()
    val isLongPressEnabled by viewModel.gestureLongPressEnabled.collectAsState()
    val isShakeEnabled by viewModel.gestureShakeEnabled.collectAsState()

    var showQueueSheet by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showTagEditor by remember { mutableStateOf(false) }
    var showQuickActionsSheet by remember { mutableStateOf(false) }
    var gestureFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var trackToAdd by remember { mutableStateOf<AudioTrack?>(null) }

    val context = LocalContext.current
    val defaultSurface = MaterialTheme.colorScheme.surface
    val defaultBackground = MaterialTheme.colorScheme.background
    
    var dominantColor by remember { mutableStateOf(defaultSurface) }
    var mutedColor by remember { mutableStateOf(defaultBackground) }

    LaunchedEffect(track?.albumArtUri) {
        track?.albumArtUri?.let { uri ->
            val request = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false) // CRITICAL for Palette API
                .build()
                
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as BitmapDrawable).bitmap
                val palette = Palette.from(bitmap).generate()
                
                val rawDominant = palette.getDominantColor(defaultSurface.toArgb())
                val rawMuted = palette.getMutedColor(defaultBackground.toArgb())

                dominantColor = if (isLight) Color(rawDominant).lightenForAmbient(0.88f) else Color(rawDominant).darkenForAmbient(0.28f)
                mutedColor = if (isLight) Color(rawMuted).lightenForAmbient(0.92f) else Color(rawMuted).darkenForAmbient(0.18f)
            }
        }
    }

    val targetDominant = if (isAmbientModeEnabled) dominantColor else defaultSurface
    val targetMuted = if (isAmbientModeEnabled) mutedColor else defaultBackground

    val animDominant by animateColorAsState(targetValue = targetDominant, animationSpec = tween(1000))
    val animMuted by animateColorAsState(targetValue = targetMuted, animationSpec = tween(1000))

    val ambientTransition = rememberInfiniteTransition(label = "ambientPulse")
    val gradientOffset by ambientTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAmbientModeEnabled && isAmbientAnimationEnabled && isPlaying) 1500f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )

    BackHandler {
        if (showQueueSheet) {
            showQueueSheet = false
        } else if (showLyrics) {
            showLyrics = false
        } else {
            onBack()
        }
    }

    if (track == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "albumArtPulse")
    val artPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "artPulseScale"
    )

    if (showEqualizerDialog) {
        EqualizerDialog(onDismiss = { showEqualizerDialog = false })
    }

    if (showTagEditor && track != null) {
        TagEditorDialog(track!!, viewModel, onDismiss = { showTagEditor = false })
    }

    Scaffold(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(animDominant, animMuted, animDominant),
                start = androidx.compose.ui.geometry.Offset(gradientOffset, gradientOffset),
                end = androidx.compose.ui.geometry.Offset(1000f - gradientOffset, 1000f - gradientOffset)
            )
        ),
        topBar = {
            TopAppBar(
                title = { Text("Now Playing", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { showTagEditor = true }) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit Tags",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { showEqualizerDialog = true }) {
                        Icon(
                            Icons.Filled.Tune, 
                            contentDescription = "Equalizer",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { showSleepTimerDialog = true }) {
                        Icon(
                            Icons.Filled.Timer, 
                            contentDescription = "Sleep Timer",
                            tint = if (sleepTimer != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { showLyrics = !showLyrics }) {
                        Icon(
                            if (showLyrics) Icons.Filled.MusicNote else Icons.Filled.Subject, 
                            contentDescription = "Toggle Lyrics",
                            tint = if (showLyrics) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { showQueueSheet = true }) {
                        Icon(Icons.Filled.List, contentDescription = "Queue", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { innerPadding ->
    LaunchedEffect(gestureFeedbackMessage) {
        if (gestureFeedbackMessage != null) {
            kotlinx.coroutines.delay(800)
            gestureFeedbackMessage = null
        }
    }

    ShakeToSkipListener(isEnabled = isShakeEnabled) {
        viewModel.skipToNext()
        gestureFeedbackMessage = "Shake: Next Track"
    }

    if (showQuickActionsSheet && track != null) {
        QuickActionsSheet(
            track = track!!,
            isFavorite = isFavorite.contains(track?.id),
            onToggleFavorite = { track?.id?.let { viewModel.toggleFavorite(it) } },
            onOpenTagEditor = { showTagEditor = true },
            onOpenPlaylistDialog = { trackToAdd = track },
            onOpenEqualizer = { showEqualizerDialog = true },
            onOpenSleepTimer = { showSleepTimerDialog = true },
            onDismiss = { showQuickActionsSheet = false }
        )
    }

    trackToAdd?.let {
        AddToPlaylistDialog(listOf(it), viewModel, onDismiss = { trackToAdd = null })
    }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = artPulseScale
                    scaleY = artPulseScale
                }
                .clip(RoundedCornerShape(16.dp))
                .background(GlassSurface)
                .artworkGestureDetector(
                    isDoubleTapEnabled = isDoubleTapEnabled,
                    isSwipeHorizEnabled = isSwipeHorizEnabled,
                    isSwipeVertEnabled = isSwipeVertEnabled,
                    isLongPressEnabled = isLongPressEnabled,
                    onDoubleTap = {
                        when (doubleTapAction) {
                            "PLAY_PAUSE" -> {
                                viewModel.togglePlayPause()
                                gestureFeedbackMessage = if (isPlaying) "Paused" else "Playing"
                            }
                            "NEXT" -> {
                                viewModel.skipToNext()
                                gestureFeedbackMessage = "Next Track"
                            }
                            "PREV" -> {
                                viewModel.skipToPrevious()
                                gestureFeedbackMessage = "Previous Track"
                            }
                            "FAVORITE" -> {
                                track?.id?.let { viewModel.toggleFavorite(it) }
                                gestureFeedbackMessage = "Favorite Toggled"
                            }
                        }
                    },
                    onSwipeLeft = {
                        viewModel.skipToNext()
                        gestureFeedbackMessage = "Next Track"
                    },
                    onSwipeRight = {
                        viewModel.skipToPrevious()
                        gestureFeedbackMessage = "Previous Track"
                    },
                    onSwipeUp = {
                        val currentVol = appVolume
                        viewModel.setAppVolume((currentVol + 0.1f).coerceAtMost(1f))
                        gestureFeedbackMessage = "Volume Up"
                    },
                    onSwipeDown = {
                        val currentVol = appVolume
                        viewModel.setAppVolume((currentVol - 0.1f).coerceAtLeast(0f))
                        gestureFeedbackMessage = "Volume Down"
                    },
                    onLongPress = {
                        showQuickActionsSheet = true
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (showLyrics) {
                LyricsView(lyrics, progress, viewModel)
            } else {
                AsyncImage(
                    model = track?.albumArtUri,
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (gestureFeedbackMessage != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = gestureFeedbackMessage!!,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track?.title ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = track?.artist ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                val fav = isFavorite.contains(track?.id)
                IconButton(onClick = { track?.id?.let { viewModel.toggleFavorite(it) } }) {
                    Icon(
                        imageVector = if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (fav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Volume Control
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.VolumeDown, contentDescription = "Volume", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = appVolume,
                    onValueChange = { viewModel.setAppVolume(it) },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                Text("${(appVolume * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
            Text("App Volume (Independent of System Volume)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(16.dp))

            var dragPosition by remember { mutableStateOf<Float?>(null) }
            val targetFraction = if (duration > 0) progress.toFloat() / duration.toFloat() else 0f
            val animatedFraction by animateFloatAsState(
                targetValue = targetFraction,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
                label = "progressAnim"
            )
            val currentFraction = dragPosition ?: animatedFraction
            val displayTimeMs = dragPosition?.let { (it * duration).toLong() } ?: progress

            val thumbSize by animateDpAsState(
                targetValue = if (dragPosition != null) 24.dp else 16.dp,
                label = "thumbSize"
            )
            val trackHeight by animateDpAsState(
                targetValue = if (dragPosition != null) 10.dp else 4.dp,
                label = "trackHeight"
            )

            val wavePhase by ambientTransition.animateFloat(
                initialValue = 0f,
                targetValue = if (isPlaying) (2f * Math.PI).toFloat() else 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "wavePhase"
            )

            Slider(
                value = currentFraction,
                onValueChange = { dragPosition = it },
                onValueChangeFinished = {
                    dragPosition?.let { pos ->
                        viewModel.seekTo((pos * duration).toLong())
                        dragPosition = null
                    }
                },
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(thumbSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                },
                track = { _ ->
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (dragPosition != null) 10.dp else 24.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val midY = height / 2f
                        
                        val activeWidth = width * currentFraction
                        
                        // Draw inactive track (straight line)
                        drawLine(
                            color = surfaceVariantColor,
                            start = androidx.compose.ui.geometry.Offset(activeWidth, midY),
                            end = androidx.compose.ui.geometry.Offset(width, midY),
                            strokeWidth = trackHeight.toPx(),
                            cap = StrokeCap.Round
                        )
                        
                        // Draw active track (wave)
                        if (activeWidth > 0) {
                            val path = Path()
                            path.moveTo(0f, midY)
                            val waveAmplitude = if (isPlaying && dragPosition == null) (height / 6f) else 0f
                            val waveFrequency = 0.03f
                            
                            // Step by 4 pixels to reduce expensive calculations
                            var x = 0f
                            while (x < activeWidth) {
                                val y = midY + Math.sin((x * waveFrequency + wavePhase).toDouble()).toFloat() * waveAmplitude
                                path.lineTo(x, y)
                                x += 4f
                            }
                            path.lineTo(activeWidth, midY)
                            
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(
                                    width = trackHeight.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(displayTimeMs), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatTime(duration), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        spotColor = if (isLight) Color.Black.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.5f),
                        ambientColor = if (isLight) Color.Black.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.3f)
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = if (isLight) listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFF1F3F7),
                                Color(0xFFE2E7F0)
                            ) else listOf(
                                Color(0xFF181E2A),
                                Color(0xFF111622),
                                Color(0xFF0D101A)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = if (isLight) listOf(
                                Color(0x40000000),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                Color(0x10000000)
                            ) else listOf(
                                Color(0x88FFFFFF),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                Color(0x22FFFFFF)
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        val shuffleEnabled by viewModel.shuffleModeEnabled.collectAsState()
                        Icon(
                            Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else (if (isLight) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.85f))
                        )
                    }
                    IconButton(
                        onClick = { viewModel.skipToPrevious() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Filled.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(36.dp),
                            tint = if (isLight) MaterialTheme.colorScheme.onSurface else Color.White
                        )
                    }
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Filled.PauseCircleFilled else Icons.Filled.PlayCircleFilled,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.skipToNext() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(36.dp),
                            tint = if (isLight) MaterialTheme.colorScheme.onSurface else Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.toggleRepeat() }) {
                        val repeatMode by viewModel.repeatMode.collectAsState()
                        Icon(
                            if (repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                            contentDescription = "Repeat",
                            tint = if (repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else (if (isLight) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.85f))
                        )
                    }
                }
            }
        }
    }

    if (showQueueSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = sheetState,
            containerColor = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .liquidGlass(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                Text(
                    text = "Up Next",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(queue, key = { it.id }) { qTrack ->
                        QueueTrackItem(
                            track = qTrack,
                            isPlaying = qTrack.id == track?.id,
                            onClick = {
                                viewModel.playTrack(qTrack, queue = queue)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSleepTimerDialog) {
        var customHoursText by remember { mutableStateOf("") }
        var customMinutesText by remember { mutableStateOf("") }
        var isCustomError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showSleepTimerDialog = false }) {
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
                        text = "Sleep Timer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (sleepTimer != null) {
                        val totalSecondsLeft = sleepTimer!! / 1000
                        val hoursLeft = totalSecondsLeft / 3600
                        val minutesLeft = (totalSecondsLeft % 3600) / 60
                        val secondsLeft = totalSecondsLeft % 60

                        val timeString = if (hoursLeft > 0) {
                            String.format(Locale.getDefault(), "%dh %02dm %02ds", hoursLeft, minutesLeft, secondsLeft)
                        } else {
                            String.format(Locale.getDefault(), "%02dm %02ds", minutesLeft, secondsLeft)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Timer Active",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { 
                                viewModel.cancelSleepTimer()
                                showSleepTimerDialog = false 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Turn Off Timer")
                        }
                    } else {
                        Text(
                            text = "Quick Presets",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        val options = listOf(5, 10, 15, 30, 45, 60, 90, 120)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(options) { mins ->
                                SuggestionChip(
                                    onClick = {
                                        viewModel.startSleepTimer(mins)
                                        showSleepTimerDialog = false
                                    },
                                    label = { Text("${mins}m") }
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text(
                            text = "Custom Duration",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = customHoursText,
                                onValueChange = { input ->
                                    if ((input.all { it.isDigit() }) && (input.length <= 2)) {
                                        customHoursText = input
                                        isCustomError = false
                                    }
                                },
                                label = { Text("Hours") },
                                placeholder = { Text("0") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = customMinutesText,
                                onValueChange = { input ->
                                    if ((input.all { it.isDigit() }) && (input.length <= 3)) {
                                        customMinutesText = input
                                        isCustomError = false
                                    }
                                },
                                label = { Text("Mins") },
                                placeholder = { Text("15") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (isCustomError) {
                            Text(
                                text = "Enter a valid time (at least 1 min)",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = {
                                val hrs = customHoursText.toIntOrNull() ?: 0
                                val mins = customMinutesText.toIntOrNull() ?: 0
                                val totalMinutes = (hrs * 60) + mins
                                if (totalMinutes > 0) {
                                    viewModel.startSleepTimer(totalMinutes)
                                    showSleepTimerDialog = false
                                } else {
                                    isCustomError = true
                                }
                            },
                            enabled = customHoursText.isNotBlank() || customMinutesText.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Custom Timer")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showSleepTimerDialog = false }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QueueTrackItem(track: AudioTrack, isPlaying: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.albumArtUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

private fun Color.darkenForAmbient(maxBrightness: Float = 0.30f): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[1] = (hsv[1] * 1.15f).coerceIn(0.15f, 0.85f)
    hsv[2] = hsv[2].coerceAtMost(maxBrightness)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun Color.lightenForAmbient(minLightness: Float = 0.88f): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[1] = (hsv[1] * 0.25f).coerceIn(0.05f, 0.20f)
    hsv[2] = hsv[2].coerceAtLeast(minLightness)
    return Color(android.graphics.Color.HSVToColor(hsv))
}
