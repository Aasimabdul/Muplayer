package com.example.muplayer.repository

import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.muplayer.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TrackTagsData(
    val trackId: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String = "",
    val trackNumber: String = "",
    val discNumber: String = "",
    val genre: String = "",
    val year: String = "",
    val albumArtUri: Uri? = null,
    val customArtBitmap: Bitmap? = null,
    val removeArtwork: Boolean = false
)

sealed class TagSaveResult {
    object Success : TagSaveResult()
    data class PermissionRequired(val intentSender: IntentSender) : TagSaveResult()
    data class Error(val message: String) : TagSaveResult()
}

class TagEditorManager(private val context: Context) {

    suspend fun readTags(track: AudioTrack): TrackTagsData = withContext(Dispatchers.IO) {
        var albumArtist = ""
        var trackNumber = ""
        var discNumber = ""
        var genre = ""
        var year = ""

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, track.uri)

            albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: ""
            trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER) ?: ""
            discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER) ?: ""
            genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: ""
            year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE) ?: ""

            retriever.release()
        } catch (_: Exception) {}

        TrackTagsData(
            trackId = track.id,
            uri = track.uri,
            title = track.title,
            artist = track.artist,
            album = track.album,
            albumArtist = albumArtist,
            trackNumber = trackNumber,
            discNumber = discNumber,
            genre = genre,
            year = year,
            albumArtUri = track.albumArtUri
        )
    }

    suspend fun saveTags(tags: TrackTagsData): TagSaveResult = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.TITLE, tags.title.trim())
                put(MediaStore.Audio.Media.ARTIST, tags.artist.trim())
                put(MediaStore.Audio.Media.ALBUM, tags.album.trim())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    put(MediaStore.Audio.Media.ALBUM_ARTIST, tags.albumArtist.trim())
                    put(MediaStore.Audio.Media.GENRE, tags.genre.trim())
                    put(MediaStore.Audio.Media.DISC_NUMBER, tags.discNumber.trim())
                }

                val trackNum = tags.trackNumber.trim().toIntOrNull()
                if (trackNum != null) {
                    put(MediaStore.Audio.Media.TRACK, trackNum)
                }

                val yearVal = tags.year.trim().toIntOrNull()
                if (yearVal != null) {
                    put(MediaStore.Audio.Media.YEAR, yearVal)
                }
            }

            val updatedRows = context.contentResolver.update(tags.uri, values, null, null)
            if (updatedRows > 0) {
                TagSaveResult.Success
            } else {
                TagSaveResult.Error("Failed to update MediaStore tags")
            }
        } catch (secEx: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val pendingIntent = MediaStore.createWriteRequest(context.contentResolver, listOf(tags.uri))
                    TagSaveResult.PermissionRequired(pendingIntent.intentSender)
                } catch (e: Exception) {
                    TagSaveResult.Error("Write permission request failed: ${e.localizedMessage}")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (secEx is android.app.RecoverableSecurityException) {
                    TagSaveResult.PermissionRequired(secEx.userAction.actionIntent.intentSender)
                } else {
                    TagSaveResult.Error("Permission required to edit file")
                }
            } else {
                TagSaveResult.Error("Permission denied: ${secEx.localizedMessage}")
            }
        } catch (e: Exception) {
            TagSaveResult.Error("Error writing tags: ${e.localizedMessage}")
        }
    }
}
