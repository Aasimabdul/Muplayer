package com.example.muplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_history")
data class PlayHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationListenedMs: Long = 0L
)
