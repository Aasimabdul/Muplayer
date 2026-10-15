package com.example.muplayer.service

import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.muplayer.MainActivity
import com.example.muplayer.repository.StatisticsRepository
import com.example.muplayer.widget.MuPlayerWidgetProvider
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    companion object {
        val sleepTimerRemaining = MutableStateFlow<Long?>(null)
        val isSleepTimerEndTrack = MutableStateFlow(false)
        val isSleepTimerPaused = MutableStateFlow(false)

        val isEqEnabled = MutableStateFlow(false)
        val currentEqPreset = MutableStateFlow<Short>(0)
        
        // Custom EQ bands: map of band index to level
        val customEqBands = MutableStateFlow<Map<Short, Short>>(emptyMap())
        
        val isBassBoostEnabled = MutableStateFlow(false)
        val bassBoostStrength = MutableStateFlow<Short>(0)

        val isCrossfadeEnabled = MutableStateFlow(false)
        val crossfadeDurationSec = MutableStateFlow(4)
        
        const val CMD_START_TIMER = "START_TIMER"
        const val CMD_START_TIMER_END_TRACK = "START_TIMER_END_TRACK"
        const val CMD_PAUSE_TIMER = "PAUSE_TIMER"
        const val CMD_RESUME_TIMER = "RESUME_TIMER"
        const val CMD_CANCEL_TIMER = "CANCEL_TIMER"
    }

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var sleepTimerJob: Job? = null
    private var fadeOutJob: Job? = null
    private var remainingMsWhenPaused: Long = 0L

    private var crossfadeJob: Job? = null
    private var isFadingOut = false
    private var targetVolume = 1.0f
    private var lastMediaId: String? = null

    private var statsRepo: StatisticsRepository? = null
    private var dataStoreRepo: com.example.muplayer.repository.DataStoreRepository? = null
    private var activeTrackId: Long = 0L
    private var activeTrackTitle: String = ""
    private var activeTrackArtist: String = ""
    private var activeTrackAlbum: String = ""
    private var sessionStartMs: Long = 0L
    private var sessionAccumulatedMs: Long = 0L
    private var isCountedPlay: Boolean = false

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        setupNotificationProvider()
        observeAudioFxSettings()
        startCrossfadeMonitor()
        startWidgetProgressMonitor()
        startPlaybackTimeTracker()
        notifyWidgets()
    }

    private fun setupNotificationProvider() {
        val provider = MuPlayerNotificationProvider(this)
        
        provider.setSmallIcon(com.simbuild.muplayer.R.mipmap.muplayer)
        setMediaNotificationProvider(provider)
    }

    private fun observeAudioFxSettings() {
        serviceScope.launch {
            isEqEnabled.collect { enabled ->
                try { equalizer?.enabled = enabled } catch (_: Exception) {}
            }
        }
        serviceScope.launch {
            currentEqPreset.collect { preset ->
                try {
                    if (preset.toInt() == -1) {
                        // Custom preset
                    } else if (equalizer != null && preset < (equalizer?.numberOfPresets ?: 0)) {
                        equalizer?.usePreset(preset)
                    }
                } catch (_: Exception) {}
            }
        }
        serviceScope.launch {
            customEqBands.collect { bands ->
                if (currentEqPreset.value.toInt() == -1) {
                    bands.forEach { (band, level) ->
                        try { equalizer?.setBandLevel(band, level) } catch (_: Exception) {}
                    }
                }
            }
        }
        serviceScope.launch {
            isBassBoostEnabled.collect { enabled ->
                try { bassBoost?.enabled = enabled } catch (_: Exception) {}
            }
        }
        serviceScope.launch {
            bassBoostStrength.collect { strength ->
                try {
                    if (bassBoost != null && bassBoost?.strengthSupported == true) {
                        bassBoost?.setStrength(strength)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun initializePlayer() {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                30000, 60000, 1500, 3000
            ).build()

        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true // handle audio focus automatically
            )
            .setHandleAudioBecomingNoisy(true) // handle headphone unplugging / Bluetooth disconnection
            .setPauseAtEndOfMediaItems(false)
            .build()
            
        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                super.onAudioSessionIdChanged(audioSessionId)
                try {
                    equalizer?.release()
                    bassBoost?.release()
                    if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                        equalizer = Equalizer(0, audioSessionId).apply {
                            enabled = isEqEnabled.value
                            if (currentEqPreset.value.toInt() == -1) {
                                customEqBands.value.forEach { (band, level) ->
                                    try { setBandLevel(band, level) } catch (_: Exception) {}
                                }
                            } else if (currentEqPreset.value < numberOfPresets) {
                                usePreset(currentEqPreset.value)
                            }
                        }
                        bassBoost = BassBoost(0, audioSessionId).apply {
                            enabled = isBassBoostEnabled.value
                            if (strengthSupported) {
                                setStrength(bassBoostStrength.value)
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Ignore EQ errors on unsupported devices
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                notifyWidgets()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                notifyWidgets()
                val newMediaId = mediaItem?.mediaId
                if (newMediaId != lastMediaId) {
                    lastMediaId = newMediaId
                    if (isCrossfadeEnabled.value && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && isFadingOut) {
                        isFadingOut = false
                        crossfadeJob?.cancel()
                        crossfadeJob = serviceScope.launch {
                            player.volume = 0f
                            val totalCrossfadeMs = crossfadeDurationSec.value * 1000L
                            val steps = 20
                            val stepDelay = maxOf(10L, totalCrossfadeMs / steps)
                            var currentVol = 0f
                            val stepAmount = targetVolume / steps

                            while (currentVol < targetVolume && player.isPlaying) {
                                currentVol = minOf(targetVolume, currentVol + stepAmount)
                                player.volume = currentVol
                                delay(stepDelay)
                            }
                            player.volume = targetVolume
                            isFadingOut = false
                        }
                    } else {
                        cancelCrossfade()
                    }
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    cancelCrossfade()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (isSleepTimerEndTrack.value && playbackState == Player.STATE_ENDED) {
                    cancelSleepTimer() // Clean up since track ended
                }
            }
        })

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val repeatButton = CommandButton.Builder()
            .setPlayerCommand(Player.COMMAND_SET_REPEAT_MODE)
            .setIconResId(androidx.media3.ui.R.drawable.exo_icon_repeat_off)
            .setDisplayName("Repeat")
            .build()
            
        val prevButton = CommandButton.Builder()
            .setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS)
            .setIconResId(androidx.media3.ui.R.drawable.exo_icon_previous)
            .setDisplayName("Previous")
            .build()
            
        val nextButton = CommandButton.Builder()
            .setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT)
            .setIconResId(androidx.media3.ui.R.drawable.exo_icon_next)
            .setDisplayName("Next")
            .build()
            
        val playPauseButton = CommandButton.Builder()
            .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
            .setIconResId(androidx.media3.ui.R.drawable.exo_icon_play)
            .setDisplayName("Play/Pause")
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setCallback(CustomMediaSessionCallback())
            .setCustomLayout(listOf(prevButton, playPauseButton, nextButton, repeatButton))
            .build()

    }

    private fun startCrossfadeMonitor() {
        serviceScope.launch {
            while (true) {
                delay(100)
                if (!isCrossfadeEnabled.value || !::player.isInitialized || !player.isPlaying) {
                    continue
                }

                val duration = player.duration
                val position = player.currentPosition

                if (duration <= 0L || position < 0L) {
                    continue
                }

                if (player.repeatMode == Player.REPEAT_MODE_ONE) {
                    continue
                }

                val hasNext = player.hasNextMediaItem() || player.repeatMode == Player.REPEAT_MODE_ALL
                if (!hasNext) {
                    continue
                }

                val desiredCrossfadeMs = crossfadeDurationSec.value * 1000L
                val effectiveCrossfadeMs = minOf(desiredCrossfadeMs, duration / 2)
                if (effectiveCrossfadeMs < 1000L) {
                    continue
                }

                val remainingMs = duration - position
                if (remainingMs <= effectiveCrossfadeMs && !isFadingOut && crossfadeJob == null) {
                    isFadingOut = true
                    targetVolume = if (player.volume > 0f) player.volume else 1.0f
                    startCrossfadeTransition(remainingMs)
                }
            }
        }
    }

    private fun startCrossfadeTransition(remainingMs: Long) {
        crossfadeJob?.cancel()
        crossfadeJob = serviceScope.launch {
            val steps = 20
            val stepDelay = maxOf(10L, remainingMs / steps)
            var currentVol = player.volume
            val stepAmount = currentVol / steps

            while (currentVol > 0.05f && isFadingOut && player.isPlaying) {
                currentVol = maxOf(0f, currentVol - stepAmount)
                player.volume = currentVol
                delay(stepDelay)
            }

            if (isFadingOut && player.isPlaying && (player.hasNextMediaItem() || player.repeatMode == Player.REPEAT_MODE_ALL)) {
                player.seekToNextMediaItem()
            }
        }
    }

    private fun cancelCrossfade() {
        crossfadeJob?.cancel()
        crossfadeJob = null
        isFadingOut = false
        try {
            if (::player.isInitialized) {
                player.volume = targetVolume
            }
        } catch (_: Exception) {}
    }

    private fun notifyWidgets() {
        if (!::player.isInitialized) return
        val currentItem = player.currentMediaItem
        val isPlaying = player.isPlaying
        val progress = player.currentPosition
        val duration = player.duration

        val title = currentItem?.mediaMetadata?.title?.toString() ?: "MuPlayer"
        val artist = currentItem?.mediaMetadata?.artist?.toString() ?: "Tap to play music"
        val album = currentItem?.mediaMetadata?.albumTitle?.toString() ?: "Unknown Album"
        val artworkUri = currentItem?.mediaMetadata?.artworkUri

        MuPlayerWidgetProvider.updateAllWidgets(
            context = this,
            title = title,
            artist = artist,
            album = album,
            isPlaying = isPlaying,
            progressMs = progress,
            durationMs = duration,
            artworkUri = artworkUri
        )
    }

    private fun startWidgetProgressMonitor() {
        serviceScope.launch {
            while (true) {
                delay(2000)
                if (::player.isInitialized && player.isPlaying) {
                    notifyWidgets()
                }
            }
        }
    }

    private fun startPlaybackTimeTracker() {
        statsRepo = StatisticsRepository(this)
        dataStoreRepo = com.example.muplayer.repository.DataStoreRepository(this)
        serviceScope.launch {
            while (true) {
                delay(1000)
                if (::player.isInitialized && player.isPlaying) {
                    val currentItem = player.currentMediaItem
                    val currentId = currentItem?.mediaId?.toLongOrNull() ?: 0L

                    if (currentId != activeTrackId && currentId != 0L) {
                        flushListeningSession()

                        activeTrackId = currentId
                        activeTrackTitle = currentItem?.mediaMetadata?.title?.toString() ?: "Unknown Title"
                        activeTrackArtist = currentItem?.mediaMetadata?.artist?.toString() ?: "Unknown Artist"
                        activeTrackAlbum = currentItem?.mediaMetadata?.albumTitle?.toString() ?: "Unknown Album"
                        sessionStartMs = System.currentTimeMillis()
                        sessionAccumulatedMs = 0L
                        isCountedPlay = false
                    } else if (currentId != 0L) {
                        sessionAccumulatedMs += 1000L

                        val duration = player.duration
                        if (!isCountedPlay && (sessionAccumulatedMs >= 30000L || (duration > 0 && sessionAccumulatedMs >= duration / 2))) {
                            isCountedPlay = true
                            serviceScope.launch {
                                dataStoreRepo?.recordPlay(currentId)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun flushListeningSession() {
        if (activeTrackId != 0L && sessionAccumulatedMs >= 3000L) {
            val id = activeTrackId
            val title = activeTrackTitle
            val artist = activeTrackArtist
            val album = activeTrackAlbum
            val start = sessionStartMs
            val duration = sessionAccumulatedMs
            val counted = isCountedPlay

            serviceScope.launch {
                statsRepo?.recordListeningSession(
                    trackId = id,
                    title = title,
                    artist = artist,
                    album = album,
                    timestamp = start,
                    durationListenedMs = duration,
                    isCountedPlay = counted
                )
            }
        }
        sessionAccumulatedMs = 0L
        isCountedPlay = false
    }

    private inner class CustomMediaSessionCallback : MediaSession.Callback {
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                CMD_START_TIMER -> {
                    val durationMs = args.getLong("duration", 0L)
                    if (durationMs > 0) {
                        startTimer(durationMs)
                    }
                }
                CMD_START_TIMER_END_TRACK -> {
                    startTimerEndTrack()
                }
                CMD_PAUSE_TIMER -> {
                    pauseTimer()
                }
                CMD_RESUME_TIMER -> {
                    resumeTimer()
                }
                CMD_CANCEL_TIMER -> {
                    cancelSleepTimer()
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }
    
    private fun startTimer(durationMs: Long) {
        cancelSleepTimer()
        sleepTimerRemaining.value = durationMs
        isSleepTimerEndTrack.value = false
        isSleepTimerPaused.value = false
        
        sleepTimerJob = serviceScope.launch {
            var remaining = durationMs
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                sleepTimerRemaining.value = max(0L, remaining)
                
                if (remaining <= 10000L && fadeOutJob == null) {
                    startFadeOut()
                }
            }
            sleepTimerRemaining.value = null
            player.pause()
        }
    }
    
    private fun startTimerEndTrack() {
        cancelSleepTimer()
        isSleepTimerEndTrack.value = true
        isSleepTimerPaused.value = false
        sleepTimerJob = serviceScope.launch {
            while (isSleepTimerEndTrack.value) {
                delay(1000)
                val duration = player.duration
                val position = player.currentPosition
                if (duration != C.TIME_UNSET && position != C.TIME_UNSET) {
                    val remaining = max(0L, duration - position)
                    sleepTimerRemaining.value = remaining
                    if (remaining <= 10000L && remaining > 0 && fadeOutJob == null) {
                        startFadeOut()
                    }
                }
            }
        }
    }
    
    private fun pauseTimer() {
        if (isSleepTimerPaused.value || sleepTimerRemaining.value == null) return
        sleepTimerJob?.cancel()
        fadeOutJob?.cancel()
        remainingMsWhenPaused = sleepTimerRemaining.value ?: 0L
        isSleepTimerPaused.value = true
        // Restore volume if paused during fade
        player.volume = targetVolume
    }
    
    private fun resumeTimer() {
        if (!isSleepTimerPaused.value) return
        if (isSleepTimerEndTrack.value) {
            startTimerEndTrack()
        } else {
            startTimer(remainingMsWhenPaused)
        }
    }

    private fun startFadeOut() {
        fadeOutJob = serviceScope.launch {
            targetVolume = player.volume
            var vol = targetVolume
            val fadeSteps = 10
            val fadeAmount = vol / fadeSteps
            while (vol > 0f) {
                vol -= fadeAmount
                player.volume = max(0f, vol)
                delay(1000L * 10 / fadeSteps)
            }
        }
    }

    private fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        fadeOutJob?.cancel()
        fadeOutJob = null
        player.volume = targetVolume
        sleepTimerRemaining.value = null
        isSleepTimerEndTrack.value = false
        isSleepTimerPaused.value = false
        remainingMsWhenPaused = 0L
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        flushListeningSession()
        cancelSleepTimer()
        cancelCrossfade()
        equalizer?.release()
        bassBoost?.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }
}
