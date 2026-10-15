package com.example.muplayer.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.muplayer.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

val Context.dataStore by preferencesDataStore(name = "muplayer_prefs")

class DataStoreRepository(private val context: Context) {

    private val FAVORITES_KEY = stringPreferencesKey("favorites")
    private val PLAYLISTS_KEY = stringPreferencesKey("playlists")
    private val RECENTLY_PLAYED_KEY = stringPreferencesKey("recently_played")
    private val PLAY_COUNTS_KEY = stringPreferencesKey("play_counts")
    private val THEME_KEY = stringPreferencesKey("theme")
    private val DEFAULT_SORT_KEY = stringPreferencesKey("default_sort")
    private val AMBIENT_MODE_KEY = booleanPreferencesKey("ambient_mode")
    private val AMBIENT_ANIMATION_KEY = booleanPreferencesKey("ambient_animation")
    private val LIQUID_GLASS_KEY = booleanPreferencesKey("liquid_glass_enabled")
    private val THEME_ANIMATION_KEY = booleanPreferencesKey("theme_animation_enabled")
    private val CROSSFADE_ENABLED_KEY = booleanPreferencesKey("crossfade_enabled")
    private val CROSSFADE_DURATION_KEY = intPreferencesKey("crossfade_duration_sec")
    private val APP_VOLUME_KEY = floatPreferencesKey("app_volume")

    private val GESTURE_DOUBLE_TAP_KEY = booleanPreferencesKey("gesture_double_tap")
    private val GESTURE_DOUBLE_TAP_ACTION_KEY = stringPreferencesKey("gesture_double_tap_action")
    private val GESTURE_SWIPE_HORIZ_KEY = booleanPreferencesKey("gesture_swipe_horiz")
    private val GESTURE_SWIPE_VERT_KEY = booleanPreferencesKey("gesture_swipe_vert")
    private val GESTURE_LONG_PRESS_KEY = booleanPreferencesKey("gesture_long_press")
    private val GESTURE_SHAKE_KEY = booleanPreferencesKey("gesture_shake")

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_KEY] ?: "navy_blue"
    }

    val defaultSortFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DEFAULT_SORT_KEY] ?: "TITLE"
    }

    val ambientModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AMBIENT_MODE_KEY] ?: true
    }

    val ambientAnimationFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AMBIENT_ANIMATION_KEY] ?: true
    }

    val liquidGlassFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[LIQUID_GLASS_KEY] ?: true
    }

    val themeAnimationFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[THEME_ANIMATION_KEY] ?: true
    }

    val crossfadeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[CROSSFADE_ENABLED_KEY] ?: false
    }

    val crossfadeDurationFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[CROSSFADE_DURATION_KEY] ?: 4
    }

    val appVolumeFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[APP_VOLUME_KEY] ?: 1.0f
    }

    val gestureDoubleTapFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[GESTURE_DOUBLE_TAP_KEY] ?: true
    }

    val gestureDoubleTapActionFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[GESTURE_DOUBLE_TAP_ACTION_KEY] ?: "PLAY_PAUSE"
    }

    val gestureSwipeHorizFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[GESTURE_SWIPE_HORIZ_KEY] ?: true
    }

    val gestureSwipeVertFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[GESTURE_SWIPE_VERT_KEY] ?: true
    }

    val gestureLongPressFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[GESTURE_LONG_PRESS_KEY] ?: true
    }

    val gestureShakeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[GESTURE_SHAKE_KEY] ?: false
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme
        }
    }

    suspend fun setDefaultSort(sort: String) {
        context.dataStore.edit { prefs ->
            prefs[DEFAULT_SORT_KEY] = sort
        }
    }

    suspend fun setAmbientMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AMBIENT_MODE_KEY] = enabled
        }
    }

    suspend fun setAmbientAnimationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AMBIENT_ANIMATION_KEY] = enabled
        }
    }

    suspend fun setLiquidGlassEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[LIQUID_GLASS_KEY] = enabled
        }
    }

    suspend fun setThemeAnimationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[THEME_ANIMATION_KEY] = enabled
        }
    }

    suspend fun setCrossfadeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[CROSSFADE_ENABLED_KEY] = enabled
        }
    }

    suspend fun setCrossfadeDuration(durationSec: Int) {
        context.dataStore.edit { prefs ->
            prefs[CROSSFADE_DURATION_KEY] = durationSec
        }
    }

    suspend fun setAppVolume(volume: Float) {
        context.dataStore.edit { prefs ->
            prefs[APP_VOLUME_KEY] = volume
        }
    }

    suspend fun setGestureDoubleTapEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[GESTURE_DOUBLE_TAP_KEY] = enabled
        }
    }

    suspend fun setGestureDoubleTapAction(action: String) {
        context.dataStore.edit { prefs ->
            prefs[GESTURE_DOUBLE_TAP_ACTION_KEY] = action
        }
    }

    suspend fun setGestureSwipeHorizEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[GESTURE_SWIPE_HORIZ_KEY] = enabled
        }
    }

    suspend fun setGestureSwipeVertEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[GESTURE_SWIPE_VERT_KEY] = enabled
        }
    }

    suspend fun setGestureLongPressEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[GESTURE_LONG_PRESS_KEY] = enabled
        }
    }

    suspend fun setGestureShakeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[GESTURE_SHAKE_KEY] = enabled
        }
    }

    suspend fun resetGesturesToDefault() {
        context.dataStore.edit { prefs ->
            prefs[GESTURE_DOUBLE_TAP_KEY] = true
            prefs[GESTURE_DOUBLE_TAP_ACTION_KEY] = "PLAY_PAUSE"
            prefs[GESTURE_SWIPE_HORIZ_KEY] = true
            prefs[GESTURE_SWIPE_VERT_KEY] = true
            prefs[GESTURE_LONG_PRESS_KEY] = true
            prefs[GESTURE_SHAKE_KEY] = false
        }
    }

    suspend fun resetThemeToDefault() {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = "navy_blue"
            prefs[LIQUID_GLASS_KEY] = true
            prefs[THEME_ANIMATION_KEY] = true
            prefs[AMBIENT_MODE_KEY] = true
            prefs[AMBIENT_ANIMATION_KEY] = true
        }
    }

    val favoritesFlow: Flow<List<Long>> = context.dataStore.data.map { prefs ->
        val jsonString = prefs[FAVORITES_KEY] ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<Long>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getLong(i))
        }
        list
    }

    val playlistsFlow: Flow<List<Playlist>> = context.dataStore.data.map { prefs ->
        val jsonString = prefs[PLAYLISTS_KEY] ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<Playlist>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val tracksArr = obj.getJSONArray("tracks")
            val trackIds = mutableListOf<Long>()
            for (j in 0 until tracksArr.length()) {
                trackIds.add(tracksArr.getLong(j))
            }
            list.add(Playlist(obj.getString("id"), obj.getString("name"), trackIds))
        }
        list
    }

    suspend fun toggleFavorite(trackId: Long, isFavorite: Boolean) {
        context.dataStore.edit { prefs ->
            val jsonString = prefs[FAVORITES_KEY] ?: "[]"
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Long>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getLong(i))
            }
            if (isFavorite && !list.contains(trackId)) {
                list.add(trackId)
            } else if (!isFavorite && list.contains(trackId)) {
                list.remove(trackId)
            }
            prefs[FAVORITES_KEY] = JSONArray(list).toString()
        }
    }

    suspend fun createPlaylist(name: String, initialTrackIds: List<Long> = emptyList()) {
        context.dataStore.edit { prefs ->
            val jsonString = prefs[PLAYLISTS_KEY] ?: "[]"
            val jsonArray = JSONArray(jsonString)
            
            val tracksArray = JSONArray()
            initialTrackIds.forEach { tracksArray.put(it) }
            
            val newPlaylist = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("name", name)
                put("tracks", tracksArray)
            }
            jsonArray.put(newPlaylist)
            prefs[PLAYLISTS_KEY] = jsonArray.toString()
        }
    }

    suspend fun importPlaylist(name: String, trackIds: List<Long>) {
        context.dataStore.edit { prefs ->
            val jsonString = prefs[PLAYLISTS_KEY] ?: "[]"
            val jsonArray = JSONArray(jsonString)
            val tracksArray = JSONArray()
            trackIds.forEach { tracksArray.put(it) }
            
            val newPlaylist = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("name", name)
                put("tracks", tracksArray)
            }
            jsonArray.put(newPlaylist)
            prefs[PLAYLISTS_KEY] = jsonArray.toString()
        }
    }

    suspend fun deletePlaylist(playlistId: String) {
        context.dataStore.edit { prefs ->
            val jsonString = prefs[PLAYLISTS_KEY] ?: "[]"
            val jsonArray = JSONArray(jsonString)
            val newArray = JSONArray()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getString("id") != playlistId) {
                    newArray.put(obj)
                }
            }
            prefs[PLAYLISTS_KEY] = newArray.toString()
        }
    }

    suspend fun renamePlaylist(playlistId: String, newName: String) {
        context.dataStore.edit { prefs ->
            val jsonString = prefs[PLAYLISTS_KEY] ?: "[]"
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getString("id") == playlistId) {
                    obj.put("name", newName)
                    break
                }
            }
            prefs[PLAYLISTS_KEY] = jsonArray.toString()
        }
    }

    suspend fun addTracksToPlaylist(playlistId: String, trackIds: List<Long>) {
        context.dataStore.edit { prefs ->
            val jsonString = prefs[PLAYLISTS_KEY] ?: "[]"
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getString("id") == playlistId) {
                    val tracks = obj.getJSONArray("tracks")
                    for (trackId in trackIds) {
                        var exists = false
                        for (j in 0 until tracks.length()) {
                            if (tracks.getLong(j) == trackId) exists = true
                        }
                        if (!exists) {
                            tracks.put(trackId)
                        }
                    }
                    obj.put("tracks", tracks)
                    jsonArray.put(i, obj)
                    break
                }
            }
            prefs[PLAYLISTS_KEY] = jsonArray.toString()
        }
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: Long) {
        context.dataStore.edit { prefs ->
            val jsonString = prefs[PLAYLISTS_KEY] ?: "[]"
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getString("id") == playlistId) {
                    val tracks = obj.getJSONArray("tracks")
                    val newTracks = JSONArray()
                    for (j in 0 until tracks.length()) {
                        if (tracks.getLong(j) != trackId) {
                            newTracks.put(tracks.getLong(j))
                        }
                    }
                    obj.put("tracks", newTracks)
                    break
                }
            }
            prefs[PLAYLISTS_KEY] = jsonArray.toString()
        }
    }

    val recentlyPlayedFlow: Flow<List<Long>> = context.dataStore.data.map { prefs ->
        val jsonString = prefs[RECENTLY_PLAYED_KEY] ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<Long>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getLong(i))
        }
        list
    }

    val playCountsFlow: Flow<Map<Long, Int>> = context.dataStore.data.map { prefs ->
        val jsonString = prefs[PLAY_COUNTS_KEY] ?: "{}"
        val jsonObj = JSONObject(jsonString)
        val map = mutableMapOf<Long, Int>()
        val keys = jsonObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key.toLong()] = jsonObj.getInt(key)
        }
        map
    }

    suspend fun recordPlay(trackId: Long) {
        context.dataStore.edit { prefs ->
            // Update recently played
            val recentString = prefs[RECENTLY_PLAYED_KEY] ?: "[]"
            val recentArray = JSONArray(recentString)
            val recentList = mutableListOf<Long>()
            for (i in 0 until recentArray.length()) {
                val id = recentArray.getLong(i)
                if (id != trackId) recentList.add(id)
            }
            recentList.add(0, trackId) // Add to top
            if (recentList.size > 30) recentList.removeAt(recentList.size - 1) // Keep top 30
            prefs[RECENTLY_PLAYED_KEY] = JSONArray(recentList).toString()

            // Update play counts
            val countsString = prefs[PLAY_COUNTS_KEY] ?: "{}"
            val countsObj = JSONObject(countsString)
            val currentCount = if (countsObj.has(trackId.toString())) countsObj.getInt(trackId.toString()) else 0
            countsObj.put(trackId.toString(), currentCount + 1)
            prefs[PLAY_COUNTS_KEY] = countsObj.toString()
        }
    }

    suspend fun clearAppPreferences() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
