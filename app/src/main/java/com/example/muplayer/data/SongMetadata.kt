package com.example.muplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_metadata")
data class SongMetadata(
    @PrimaryKey val songId: Long,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val isLiked: Boolean = false
)
