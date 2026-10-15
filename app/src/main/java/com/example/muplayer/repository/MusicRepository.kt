package com.example.muplayer.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.example.muplayer.data.AppDatabase
import com.example.muplayer.data.SongMetadata
import com.example.muplayer.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MusicRepository(private val context: Context) {

    private val songDao = AppDatabase.getDatabase(context).songDao()

    val recentlyPlayed: Flow<List<SongMetadata>> = songDao.getRecentlyPlayed()
    val mostPlayed: Flow<List<SongMetadata>> = songDao.getMostPlayed()
    val likedSongs: Flow<List<SongMetadata>> = songDao.getLikedSongs()

    fun getFrequentlyPlayed(): Flow<List<SongMetadata>> {
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
        val threshold = System.currentTimeMillis() - thirtyDaysMs
        return songDao.getFrequentlyPlayed(threshold)
    }

    suspend fun saveSongRecord(song: SongMetadata) = songDao.insertOrUpdate(song)

    suspend fun getAllTracks(): List<AudioTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<AudioTrack>()
        
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val hasQ = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )
        if (hasQ) {
            projection.add(MediaStore.Audio.Media.RELATIVE_PATH)
            projection.add(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
        } else {
            projection.add(MediaStore.Audio.Media.DATA)
        }

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection.toTypedArray(),
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            val relativePathCol = if (hasQ) cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH) else -1
            val bucketNameCol = if (hasQ) cursor.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME) else -1
            val dataCol = if (!hasQ) cursor.getColumnIndex(MediaStore.Audio.Media.DATA) else -1

            val artworkUri = Uri.parse("content://media/external/audio/albumart")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown Title"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)

                var folderPath = ""
                var folderName = "Unknown"

                if (hasQ) {
                    folderPath = if (relativePathCol != -1) cursor.getString(relativePathCol) ?: "" else ""
                    folderName = if (bucketNameCol != -1) cursor.getString(bucketNameCol) ?: "Unknown" else "Unknown"
                } else {
                    val data = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                    val file = java.io.File(data)
                    folderPath = file.parent ?: ""
                    folderName = file.parentFile?.name ?: "Unknown"
                }

                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val albumArtUri = ContentUris.withAppendedId(artworkUri, albumId)

                tracks.add(
                    AudioTrack(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = duration,
                        uri = contentUri,
                        albumArtUri = albumArtUri,
                        dateAdded = dateAdded,
                        folderName = folderName,
                        folderPath = folderPath
                    )
                )
            }
        }
        tracks
    }
}
