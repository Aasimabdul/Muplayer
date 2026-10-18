package com.example.muplayer.viewmodel

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.C
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import android.net.Uri
import com.example.muplayer.data.DailyListeningStat
import com.example.muplayer.data.PlayHistoryEntry
import com.example.muplayer.data.TopItemCount
import com.example.muplayer.model.AudioTrack
import com.example.muplayer.model.LyricLine
import com.example.muplayer.model.Playlist
import com.example.muplayer.repository.BackupManager
import com.example.muplayer.repository.DataStoreRepository
import com.example.muplayer.repository.LyricsRepository
import com.example.muplayer.repository.MusicRepository
import com.example.muplayer.repository.StatisticsRepository
import com.example.muplayer.repository.TrackTagsData
import com.example.muplayer.service.PlaybackService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)
    private val dataStoreRepository = DataStoreRepository(application)
    private val lyricsRepository = LyricsRepository(application)

    private val _tracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val tracks: StateFlow<List<AudioTrack>> = _tracks.asStateFlow()

    private val _favorites = MutableStateFlow<List<Long>>(emptyList())
    val favorites: StateFlow<List<Long>> = _favorites.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _recentlyPlayedIds = MutableStateFlow<List<Long>>(emptyList())
    val recentlyPlayedIds: StateFlow<List<Long>> = _recentlyPlayedIds.asStateFlow()

    private val _playCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val playCounts: StateFlow<Map<Long, Int>> = _playCounts.asStateFlow()

    private val _currentQueue = MutableStateFlow<List<AudioTrack>>(emptyList())
    val currentQueue: StateFlow<List<AudioTrack>> = _currentQueue.asStateFlow()

    private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
    val currentTrack: StateFlow<AudioTrack?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled: StateFlow<Boolean> = _shuffleModeEnabled.asStateFlow()

    private val _lyrics = MutableStateFlow<List<LyricLine>?>(null)
    val lyrics: StateFlow<List<LyricLine>?> = _lyrics.asStateFlow()

    private val _lyricsOffsetMs = MutableStateFlow(0L)
    val lyricsOffsetMs: StateFlow<Long> = _lyricsOffsetMs.asStateFlow()

    private val backupManager = BackupManager(application)

    private val _backupPreview = MutableStateFlow<com.example.muplayer.repository.BackupPreviewSummary?>(null)
    val backupPreview: StateFlow<com.example.muplayer.repository.BackupPreviewSummary?> = _backupPreview.asStateFlow()

    private val _restoreSummary = MutableStateFlow<com.example.muplayer.repository.RestoreResultSummary?>(null)
    val restoreSummary: StateFlow<com.example.muplayer.repository.RestoreResultSummary?> = _restoreSummary.asStateFlow()

    private val _appVolume = MutableStateFlow(1f)
    val appVolume: StateFlow<Float> = _appVolume.asStateFlow()

    private val _currentTheme = MutableStateFlow("midnight")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()

    private val _defaultSort = MutableStateFlow("TITLE")
    val defaultSort: StateFlow<String> = _defaultSort.asStateFlow()

    private val _isAmbientModeEnabled = MutableStateFlow(true)
    val isAmbientModeEnabled: StateFlow<Boolean> = _isAmbientModeEnabled.asStateFlow()

    private val _isAmbientAnimationEnabled = MutableStateFlow(true)
    val isAmbientAnimationEnabled: StateFlow<Boolean> = _isAmbientAnimationEnabled.asStateFlow()

    private val _isLiquidGlassEnabled = MutableStateFlow(true)
    val isLiquidGlassEnabled: StateFlow<Boolean> = _isLiquidGlassEnabled.asStateFlow()

    private val _isThemeAnimationEnabled = MutableStateFlow(true)
    val isThemeAnimationEnabled: StateFlow<Boolean> = _isThemeAnimationEnabled.asStateFlow()

    private val _isCrossfadeEnabled = MutableStateFlow(false)
    val isCrossfadeEnabled: StateFlow<Boolean> = _isCrossfadeEnabled.asStateFlow()

    private val _crossfadeDurationSec = MutableStateFlow(4)
    val crossfadeDurationSec: StateFlow<Int> = _crossfadeDurationSec.asStateFlow()

    val gestureDoubleTapEnabled: StateFlow<Boolean> = dataStoreRepository.gestureDoubleTapFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val gestureDoubleTapAction: StateFlow<String> = dataStoreRepository.gestureDoubleTapActionFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "PLAY_PAUSE")

    val gestureSwipeHorizEnabled: StateFlow<Boolean> = dataStoreRepository.gestureSwipeHorizFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val gestureSwipeVertEnabled: StateFlow<Boolean> = dataStoreRepository.gestureSwipeVertFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val gestureLongPressEnabled: StateFlow<Boolean> = dataStoreRepository.gestureLongPressFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val gestureShakeEnabled: StateFlow<Boolean> = dataStoreRepository.gestureShakeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val statisticsRepository = StatisticsRepository(application)

    private val _statsTimeRange = MutableStateFlow(7)
    val statsTimeRange: StateFlow<Int> = _statsTimeRange.asStateFlow()

    fun setStatsTimeRange(days: Int) {
        _statsTimeRange.value = days
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val historySince: StateFlow<List<PlayHistoryEntry>> = _statsTimeRange.flatMapLatest { days ->
        val sinceMs = if (days <= 0) 0L else System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        statisticsRepository.getHistorySince(sinceMs)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalListeningTimeMs: StateFlow<Long> = _statsTimeRange.flatMapLatest { days ->
        val sinceMs = if (days <= 0) 0L else System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        statisticsRepository.getTotalListeningTimeMsSince(sinceMs)
    }.map { it ?: 0L }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyStats: StateFlow<List<DailyListeningStat>> = _statsTimeRange.flatMapLatest { days ->
        val rangeDays = if (days <= 0) 30 else days
        statisticsRepository.getDailyStatsForPastDays(rangeDays)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val topArtists: StateFlow<List<TopItemCount>> = _statsTimeRange.flatMapLatest { days ->
        val sinceMs = if (days <= 0) 0L else System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        statisticsRepository.getTopArtistsSince(sinceMs)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val topAlbums: StateFlow<List<TopItemCount>> = _statsTimeRange.flatMapLatest { days ->
        val sinceMs = if (days <= 0) 0L else System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        statisticsRepository.getTopAlbumsSince(sinceMs)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun clearListeningStatistics() {
        viewModelScope.launch {
            statisticsRepository.clearListeningStatistics()
        }
    }

    private val _sleepTimerTimeRemaining = MutableStateFlow<Long?>(null)
    val sleepTimerTimeRemaining: StateFlow<Long?> = _sleepTimerTimeRemaining.asStateFlow()
    private var sleepTimerJob: kotlinx.coroutines.Job? = null

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    var mediaController: MediaController? = null
        private set

    init {
        loadLibrary()
        initializeMediaController()
        
        viewModelScope.launch {
            dataStoreRepository.playlistsFlow.collect { lists ->
                _playlists.value = lists
            }
        }
        
        viewModelScope.launch {
            dataStoreRepository.favoritesFlow.collect { favs ->
                _favorites.value = favs
            }
        }

        viewModelScope.launch {
            dataStoreRepository.recentlyPlayedFlow.collect { recent ->
                _recentlyPlayedIds.value = recent
            }
        }

        viewModelScope.launch {
            dataStoreRepository.playCountsFlow.collect { counts ->
                _playCounts.value = counts
            }
        }

        viewModelScope.launch {
            dataStoreRepository.themeFlow.collect { theme ->
                _currentTheme.value = theme
            }
        }

        viewModelScope.launch {
            dataStoreRepository.defaultSortFlow.collect { sort ->
                _defaultSort.value = sort
            }
        }

        viewModelScope.launch {
            dataStoreRepository.ambientModeFlow.collect { enabled ->
                _isAmbientModeEnabled.value = enabled
            }
        }
        
        viewModelScope.launch {
            dataStoreRepository.ambientAnimationFlow.collect { enabled ->
                _isAmbientAnimationEnabled.value = enabled
            }
        }

        viewModelScope.launch {
            dataStoreRepository.liquidGlassFlow.collect { enabled ->
                _isLiquidGlassEnabled.value = enabled
            }
        }

        viewModelScope.launch {
            dataStoreRepository.themeAnimationFlow.collect { enabled ->
                _isThemeAnimationEnabled.value = enabled
            }
        }

        viewModelScope.launch {
            dataStoreRepository.crossfadeEnabledFlow.collect { enabled ->
                _isCrossfadeEnabled.value = enabled
                PlaybackService.isCrossfadeEnabled.value = enabled
            }
        }

        viewModelScope.launch {
            dataStoreRepository.crossfadeDurationFlow.collect { duration ->
                _crossfadeDurationSec.value = duration
                PlaybackService.crossfadeDurationSec.value = duration
            }
        }
        
        viewModelScope.launch {
            while (true) {
                if (_isPlaying.value) {
                    updateProgress()
                }
                kotlinx.coroutines.delay(500L)
            }
        }
    }

    fun loadLibrary() {
        viewModelScope.launch {
            val allTracks = musicRepository.getAllTracks()
            _tracks.value = allTracks

            // The media service can keep playing while the Activity is recreated.
            // Reconcile its current item after the library is available so the
            // Mini Player can resolve the playing media ID back to an AudioTrack.
            updateCurrentTrack()
        }
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), PlaybackService::class.java)
        )
        viewModelScope.launch {
            dataStoreRepository.appVolumeFlow.collect { vol ->
                if (_appVolume.value != vol) {
                    _appVolume.value = vol
                    mediaController?.volume = vol
                }
            }
        }
        
        mediaControllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
            mediaController?.addListener(playerListener)
            _appVolume.value = mediaController?.volume ?: 1f

            // Reconnect to the existing MediaSession without restarting playback.
            // Its current item may already be playing before this ViewModel connects.
            updateCurrentTrack()
            updatePlaybackState()
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateCurrentTrack()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlaybackState()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _shuffleModeEnabled.value = shuffleModeEnabled
        }

        override fun onVolumeChanged(volume: Float) {
            // Intentionally ignored so the volume slider doesn't jump
            // during automatic crossfade animations
        }
    }

    private fun updateCurrentTrack() {
        val controller = mediaController
        val currentMediaItem = controller?.currentMediaItem
        val currentMediaId = currentMediaItem?.mediaId
        val id = currentMediaId?.toLongOrNull()

        // Always reconcile the UI state, including when there is no current item.
        // Leaving the old value untouched can hide or display a stale Mini Player.
        _currentTrack.value = id?.let { trackId ->
            _tracks.value.find { it.id == trackId }
        }

        if (currentMediaItem == null || id == null) {
            _duration.value = 0L
            _progress.value = 0L
            _lyrics.value = null
            _lyricsOffsetMs.value = 0L
            return
        }

        _duration.value = controller.duration.coerceAtLeast(0L)
        _progress.value = controller.currentPosition.coerceAtLeast(0L)
        _lyricsOffsetMs.value = 0L

        viewModelScope.launch {
            _lyrics.value = lyricsRepository.getLyrics(id)
        }
    }

    private fun updatePlaybackState() {
        _isPlaying.value = mediaController?.isPlaying == true
        _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
        _progress.value = mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L
    }

    fun updateProgress() {
        _progress.value = mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L
    }

    fun playTrack(track: AudioTrack, queue: List<AudioTrack> = _tracks.value) {
        mediaController?.let { controller ->
            _currentQueue.value = queue
            val startIndex = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
            val mediaItems = queue.map { audioTrack ->
                MediaItem.Builder()
                    .setMediaId(audioTrack.id.toString())
                    .setUri(audioTrack.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(audioTrack.title)
                            .setArtist(audioTrack.artist)
                            .setAlbumTitle(audioTrack.album)
                            .setArtworkUri(audioTrack.albumArtUri)
                            .build()
                    )
                    .build()
            }
            controller.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
            controller.prepare()
            controller.play()
        }
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _progress.value = positionMs
    }

    fun toggleShuffle() {
        mediaController?.shuffleModeEnabled = !(mediaController?.shuffleModeEnabled ?: false)
    }

    fun toggleRepeat() {
        val current = mediaController?.repeatMode ?: Player.REPEAT_MODE_OFF
        mediaController?.repeatMode = when (current) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun deleteTracks(trackIds: List<Long>) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                var deletedCount = 0
                val tracksToDelete = _tracks.value.filter { trackIds.contains(it.id) }
                for (track in tracksToDelete) {
                    try {
                        val deleted = context.contentResolver.delete(track.uri, null, null)
                        if (deleted > 0) deletedCount++
                    } catch (e: SecurityException) {
                        // May require scoped storage intent on Android 10+
                    }
                }
                if (deletedCount > 0) {
                    loadLibrary()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setAppVolume(volume: Float) {
        _appVolume.value = volume
        mediaController?.volume = volume
        viewModelScope.launch { dataStoreRepository.setAppVolume(volume) }
    }

    fun importLyrics(uri: Uri) {
        viewModelScope.launch {
            val trackId = _currentTrack.value?.id ?: return@launch
            val success = lyricsRepository.importLyrics(trackId, uri)
            if (success) {
                _lyrics.value = lyricsRepository.getLyrics(trackId)
                _lyricsOffsetMs.value = 0L
            }
        }
    }

    fun importM3uPlaylist(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: return@launch
                val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream))
                val paths = mutableListOf<String>()
                var playlistName = "Imported Playlist"

                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (displayNameIndex != -1) {
                            val name = it.getString(displayNameIndex)
                            if (name.endsWith(".m3u", ignoreCase = true)) {
                                playlistName = name.dropLast(4)
                            } else if (name.endsWith(".m3u8", ignoreCase = true)) {
                                playlistName = name.dropLast(5)
                            } else {
                                playlistName = name
                            }
                        }
                    }
                }

                reader.forEachLine { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                        paths.add(trimmed)
                    }
                }
                reader.close()

                val matchedTrackIds = mutableListOf<Long>()
                val allTracks = _tracks.value
                for (path in paths) {
                    val fileName = java.io.File(path).name
                    val matchedTrack = allTracks.find { track ->
                        track.uri.toString().endsWith(fileName) || track.title.equals(fileName.substringBeforeLast("."), ignoreCase = true)
                    }
                    if (matchedTrack != null) {
                        matchedTrackIds.add(matchedTrack.id)
                    }
                }

                if (matchedTrackIds.isNotEmpty()) {
                    dataStoreRepository.importPlaylist(playlistName, matchedTrackIds.distinct())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun adjustLyricsOffset(deltaMs: Long) {
        _lyricsOffsetMs.value += deltaMs
    }

    fun resetLyricsOffset() {
        _lyricsOffsetMs.value = 0L
    }

    fun deleteLyricsForCurrentTrack() {
        viewModelScope.launch {
            val trackId = _currentTrack.value?.id ?: return@launch
            lyricsRepository.deleteLyrics(trackId)
            _lyrics.value = null
            _lyricsOffsetMs.value = 0L
        }
    }

    fun exportBackup(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = backupManager.exportBackupToUri(uri, _tracks.value)
            onResult(success)
        }
    }

    fun previewBackup(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val preview = backupManager.parseBackupPreview(uri)
            _backupPreview.value = preview
            onResult(preview != null)
        }
    }

    fun executeRestore(mergeMode: Boolean, onResult: (com.example.muplayer.repository.RestoreResultSummary) -> Unit) {
        viewModelScope.launch {
            val preview = _backupPreview.value ?: return@launch
            val summary = backupManager.restoreBackup(preview, _tracks.value, mergeMode)
            _restoreSummary.value = summary
            _backupPreview.value = null
            onResult(summary)
        }
    }

    fun clearBackupPreview() {
        _backupPreview.value = null
    }

    fun clearRestoreSummary() {
        _restoreSummary.value = null
    }

    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch {
            val isFav = _favorites.value.contains(trackId)
            dataStoreRepository.toggleFavorite(trackId, !isFav)
        }
    }

    fun createPlaylist(name: String, initialTrackIds: List<Long> = emptyList()) = viewModelScope.launch { dataStoreRepository.createPlaylist(name, initialTrackIds) }
    fun renamePlaylist(id: String, name: String) = viewModelScope.launch { dataStoreRepository.renamePlaylist(id, name) }
    fun deletePlaylist(id: String) = viewModelScope.launch { dataStoreRepository.deletePlaylist(id) }
    fun addTracksToPlaylist(playlistId: String, trackIds: List<Long>) = viewModelScope.launch { dataStoreRepository.addTracksToPlaylist(playlistId, trackIds) }
    fun removeTrackFromPlaylist(playlistId: String, trackId: Long) = viewModelScope.launch { dataStoreRepository.removeTrackFromPlaylist(playlistId, trackId) }

    fun setTheme(theme: String) = viewModelScope.launch { dataStoreRepository.setTheme(theme) }
    fun setDefaultSort(sort: String) = viewModelScope.launch { dataStoreRepository.setDefaultSort(sort) }
    fun setAmbientModeEnabled(enabled: Boolean) = viewModelScope.launch { dataStoreRepository.setAmbientMode(enabled) }
    fun setAmbientAnimationEnabled(enabled: Boolean) = viewModelScope.launch { dataStoreRepository.setAmbientAnimationEnabled(enabled) }
    fun setLiquidGlassEnabled(enabled: Boolean) = viewModelScope.launch { dataStoreRepository.setLiquidGlassEnabled(enabled) }
    fun setThemeAnimationEnabled(enabled: Boolean) = viewModelScope.launch { dataStoreRepository.setThemeAnimationEnabled(enabled) }
    fun setCrossfadeEnabled(enabled: Boolean) = viewModelScope.launch { dataStoreRepository.setCrossfadeEnabled(enabled) }
    fun setCrossfadeDuration(durationSec: Int) = viewModelScope.launch { dataStoreRepository.setCrossfadeDuration(durationSec) }
    fun setGestureDoubleTapEnabled(enabled: Boolean) = viewModelScope.launch { dataStoreRepository.setGestureDoubleTapEnabled(enabled) }
    fun setGestureDoubleTapAction(action: String) = viewModelScope.launch { dataStoreRepository.setGestureDoubleTapAction(action) }
    fun setGestureSwipeHorizEnabled(enabled: Boolean) = viewModelScope.launch { dataStoreRepository.setGestureSwipeHorizEnabled(enabled) }
    fun setGestureSwipeVertEnabled(enabled: Boolean) = viewModelScope.launch { dataStoreRepository.setGestureSwipeVertEnabled(enabled) }
    fun setGestureLongPressEnabled(enabled: Boolean) = viewModelScope.launch { dataStoreRepository.setGestureLongPressEnabled(enabled) }
    fun setGestureShakeEnabled(enabled: Boolean) = viewModelScope.launch { dataStoreRepository.setGestureShakeEnabled(enabled) }
    fun resetGesturesToDefault() = viewModelScope.launch { dataStoreRepository.resetGesturesToDefault() }
    fun resetThemeToDefault() = viewModelScope.launch { dataStoreRepository.resetThemeToDefault() }
    fun clearAppPreferences() = viewModelScope.launch { dataStoreRepository.clearAppPreferences() }

    fun refreshCurrentTrackMetadata(tags: TrackTagsData) {
        viewModelScope.launch {
            val curr = _currentTrack.value
            if (curr != null && curr.id == tags.trackId) {
                val updatedTrack = curr.copy(
                    title = tags.title,
                    artist = tags.artist,
                    album = tags.album
                )
                _currentTrack.value = updatedTrack

                mediaController?.let { controller ->
                    val currentMediaItem = controller.currentMediaItem
                    if (currentMediaItem != null) {
                        val newMetadata = currentMediaItem.mediaMetadata.buildUpon()
                            .setTitle(tags.title)
                            .setArtist(tags.artist)
                            .setAlbumTitle(tags.album)
                            .build()
                        val updatedMediaItem = currentMediaItem.buildUpon()
                            .setMediaMetadata(newMetadata)
                            .build()
                        controller.replaceMediaItem(controller.currentMediaItemIndex, updatedMediaItem)
                    }
                }
            }
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerTimeRemaining.value = minutes * 60 * 1000L
        sleepTimerJob = viewModelScope.launch {
            while ((_sleepTimerTimeRemaining.value ?: 0) > 0) {
                kotlinx.coroutines.delay(1000)
                _sleepTimerTimeRemaining.value = (_sleepTimerTimeRemaining.value ?: 0) - 1000
            }
            _sleepTimerTimeRemaining.value = null
            mediaController?.pause()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerTimeRemaining.value = null
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        mediaControllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}
