package com.example.muplayer.repository

import android.content.Context
import android.net.Uri
import com.example.muplayer.model.AudioTrack
import com.example.muplayer.model.Playlist
import com.example.muplayer.service.PlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class BackupPreviewSummary(
    val schemaVersion: Int,
    val timestamp: Long,
    val playlistsCount: Int,
    val totalPlaylistTracks: Int,
    val favoritesCount: Int,
    val themeName: String,
    val sortOption: String,
    val playCountsCount: Int,
    val rawJson: String
)

data class RestoreResultSummary(
    val playlistsRestored: Int,
    val favoritesRestored: Int,
    val matchedTracksCount: Int,
    val unmatchedTracksCount: Int,
    val settingsRestored: Boolean
)

class BackupManager(private val context: Context) {

    private val dataStoreRepository = DataStoreRepository(context)

    suspend fun createBackupJson(allTracks: List<AudioTrack>): String = withContext(Dispatchers.IO) {
        val backupObj = JSONObject()
        backupObj.put("schemaVersion", 1)
        backupObj.put("timestamp", System.currentTimeMillis())
        backupObj.put("appName", "MuPlayer")

        // 1. Settings & Appearance
        val theme = dataStoreRepository.themeFlow.first()
        val defaultSort = dataStoreRepository.defaultSortFlow.first()
        val ambientMode = dataStoreRepository.ambientModeFlow.first()
        val liquidGlass = dataStoreRepository.liquidGlassFlow.first()
        val themeAnimation = dataStoreRepository.themeAnimationFlow.first()

        val settingsObj = JSONObject().apply {
            put("theme", theme)
            put("defaultSort", defaultSort)
            put("ambientMode", ambientMode)
            put("liquidGlass", liquidGlass)
            put("themeAnimation", themeAnimation)
        }
        backupObj.put("settings", settingsObj)

        // 2. Audio & Equalizer Preferences
        val audioFxObj = JSONObject().apply {
            put("isEqEnabled", PlaybackService.isEqEnabled.value)
            put("eqPreset", PlaybackService.currentEqPreset.value.toInt())
            put("isBassBoostEnabled", PlaybackService.isBassBoostEnabled.value)
            put("bassBoostStrength", PlaybackService.bassBoostStrength.value.toInt())
        }
        backupObj.put("audioPreferences", audioFxObj)

        // 3. Favorites with Metadata Fallback
        val favoriteIds = dataStoreRepository.favoritesFlow.first()
        val favsArr = JSONArray()
        for (id in favoriteIds) {
            val track = allTracks.find { it.id == id }
            val itemObj = JSONObject()
            itemObj.put("id", id)
            if (track != null) {
                itemObj.put("title", track.title)
                itemObj.put("artist", track.artist)
                itemObj.put("album", track.album)
            }
            favsArr.put(itemObj)
        }
        backupObj.put("favorites", favsArr)

        // 4. Playlists with Track Order & Metadata
        val playlists = dataStoreRepository.playlistsFlow.first()
        val playlistsArr = JSONArray()
        for (pl in playlists) {
            val plObj = JSONObject()
            plObj.put("id", pl.id)
            plObj.put("name", pl.name)
            val tracksArr = JSONArray()
            for (tId in pl.trackIds) {
                val track = allTracks.find { it.id == tId }
                val tObj = JSONObject()
                tObj.put("id", tId)
                if (track != null) {
                    tObj.put("title", track.title)
                    tObj.put("artist", track.artist)
                    tObj.put("album", track.album)
                }
                tracksArr.put(tObj)
            }
            plObj.put("tracks", tracksArr)
            playlistsArr.put(plObj)
        }
        backupObj.put("playlists", playlistsArr)

        // 5. Statistics (Recently Played & Play Counts)
        val recentlyPlayed = dataStoreRepository.recentlyPlayedFlow.first()
        backupObj.put("recentlyPlayed", JSONArray(recentlyPlayed))

        val playCounts = dataStoreRepository.playCountsFlow.first()
        val countsObj = JSONObject()
        for ((id, count) in playCounts) {
            countsObj.put(id.toString(), count)
        }
        backupObj.put("playCounts", countsObj)

        backupObj.toString(2)
    }

    suspend fun exportBackupToUri(uri: Uri, allTracks: List<AudioTrack>): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonString = createBackupJson(allTracks)
            context.contentResolver.openOutputStream(uri)?.use { output ->
                OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun parseBackupPreview(uri: Uri): BackupPreviewSummary? = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext null

            val root = JSONObject(jsonString)
            val version = root.optInt("schemaVersion", 1)
            val timestamp = root.optLong("timestamp", System.currentTimeMillis())

            val settings = root.optJSONObject("settings")
            val themeName = settings?.optString("theme", "navy_blue") ?: "navy_blue"
            val defaultSort = settings?.optString("defaultSort", "TITLE") ?: "TITLE"

            val favoritesArr = root.optJSONArray("favorites") ?: JSONArray()
            val playlistsArr = root.optJSONArray("playlists") ?: JSONArray()

            var totalPlTracks = 0
            for (i in 0 until playlistsArr.length()) {
                val plObj = playlistsArr.getJSONObject(i)
                val tracks = plObj.optJSONArray("tracks")
                totalPlTracks += (tracks?.length() ?: 0)
            }

            val playCountsObj = root.optJSONObject("playCounts")
            val countsCount = playCountsObj?.length() ?: 0

            BackupPreviewSummary(
                schemaVersion = version,
                timestamp = timestamp,
                playlistsCount = playlistsArr.length(),
                totalPlaylistTracks = totalPlTracks,
                favoritesCount = favoritesArr.length(),
                themeName = themeName,
                sortOption = defaultSort,
                playCountsCount = countsCount,
                rawJson = jsonString
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun restoreBackup(
        summary: BackupPreviewSummary,
        allTracks: List<AudioTrack>,
        mergeMode: Boolean
    ): RestoreResultSummary = withContext(Dispatchers.IO) {
        val root = JSONObject(summary.rawJson)

        var matchedCount = 0
        var unmatchedCount = 0

        fun findTrackId(id: Long, title: String?, artist: String?): Long? {
            // Match by exact ID
            val byId = allTracks.find { it.id == id }
            if (byId != null) {
                matchedCount++
                return byId.id
            }
            // Match by Title and Artist
            if (!title.isNullOrBlank()) {
                val byMeta = allTracks.find {
                    it.title.equals(title, ignoreCase = true) &&
                    (artist.isNullOrBlank() || it.artist.equals(artist, ignoreCase = true))
                }
                if (byMeta != null) {
                    matchedCount++
                    return byMeta.id
                }
            }
            unmatchedCount++
            return null
        }

        // 1. Restore Settings
        val settingsObj = root.optJSONObject("settings")
        if (settingsObj != null) {
            val theme = settingsObj.optString("theme", "navy_blue")
            val sort = settingsObj.optString("defaultSort", "TITLE")
            val ambient = settingsObj.optBoolean("ambientMode", true)
            val glass = settingsObj.optBoolean("liquidGlass", true)
            val animation = settingsObj.optBoolean("themeAnimation", true)

            dataStoreRepository.setTheme(theme)
            dataStoreRepository.setDefaultSort(sort)
            dataStoreRepository.setAmbientMode(ambient)
            dataStoreRepository.setLiquidGlassEnabled(glass)
            dataStoreRepository.setThemeAnimationEnabled(animation)
        }

        // 2. Restore Audio Preferences
        val audioFxObj = root.optJSONObject("audioPreferences")
        if (audioFxObj != null) {
            PlaybackService.isEqEnabled.value = audioFxObj.optBoolean("isEqEnabled", false)
            PlaybackService.currentEqPreset.value = audioFxObj.optInt("eqPreset", 0).toShort()
            PlaybackService.isBassBoostEnabled.value = audioFxObj.optBoolean("isBassBoostEnabled", false)
            PlaybackService.bassBoostStrength.value = audioFxObj.optInt("bassBoostStrength", 0).toShort()
        }

        // If replace mode, clear existing preferences before restoring
        if (!mergeMode) {
            dataStoreRepository.clearAppPreferences()
            if (settingsObj != null) {
                dataStoreRepository.setTheme(settingsObj.optString("theme", "navy_blue"))
                dataStoreRepository.setDefaultSort(settingsObj.optString("defaultSort", "TITLE"))
            }
        }

        // 3. Restore Favorites
        val favoritesArr = root.optJSONArray("favorites") ?: JSONArray()
        var favsRestored = 0
        for (i in 0 until favoritesArr.length()) {
            val favItem = favoritesArr.optJSONObject(i)
            val id = favItem?.optLong("id", -1L) ?: -1L
            val title = favItem?.optString("title")
            val artist = favItem?.optString("artist")

            val matchedId = findTrackId(id, title, artist)
            if (matchedId != null) {
                dataStoreRepository.toggleFavorite(matchedId, true)
                favsRestored++
            }
        }

        // 4. Restore Playlists
        val playlistsArr = root.optJSONArray("playlists") ?: JSONArray()
        var plRestored = 0
        for (i in 0 until playlistsArr.length()) {
            val plObj = playlistsArr.optJSONObject(i) ?: continue
            val name = plObj.optString("name", "Restored Playlist")
            val tracksArr = plObj.optJSONArray("tracks") ?: JSONArray()

            dataStoreRepository.createPlaylist(name)
            val currentPlaylists = dataStoreRepository.playlistsFlow.first()
            val newPl = currentPlaylists.find { it.name == name }

            if (newPl != null) {
                for (j in 0 until tracksArr.length()) {
                    val tObj = tracksArr.optJSONObject(j)
                    val tId = tObj?.optLong("id", -1L) ?: -1L
                    val tTitle = tObj?.optString("title")
                    val tArtist = tObj?.optString("artist")

                    val matchedId = findTrackId(tId, tTitle, tArtist)
                    if (matchedId != null) {
                        dataStoreRepository.addTracksToPlaylist(newPl.id, listOf(matchedId))
                    }
                }
                plRestored++
            }
        }

        RestoreResultSummary(
            playlistsRestored = plRestored,
            favoritesRestored = favsRestored,
            matchedTracksCount = matchedCount,
            unmatchedTracksCount = unmatchedCount,
            settingsRestored = true
        )
    }
}
