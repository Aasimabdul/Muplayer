package com.example.muplayer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(songMetadata: SongMetadata)

    @Query("SELECT * FROM song_metadata ORDER BY lastPlayedTimestamp DESC LIMIT 30")
    fun getRecentlyPlayed(): Flow<List<SongMetadata>>

    @Query("SELECT * FROM song_metadata ORDER BY playCount DESC LIMIT 50")
    fun getMostPlayed(): Flow<List<SongMetadata>>

    @Query("SELECT * FROM song_metadata WHERE isLiked = 1 ORDER BY lastPlayedTimestamp DESC")
    fun getLikedSongs(): Flow<List<SongMetadata>>

    @Query("SELECT * FROM song_metadata WHERE playCount >= 5 AND lastPlayedTimestamp >= :thresholdMs ORDER BY playCount DESC")
    fun getFrequentlyPlayed(thresholdMs: Long): Flow<List<SongMetadata>>
}
