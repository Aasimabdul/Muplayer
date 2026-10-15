package com.example.muplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_listening_stats")
data class DailyListeningStat(
    @PrimaryKey val dateString: String, // "yyyy-MM-dd"
    val totalListeningTimeMs: Long = 0L
)
