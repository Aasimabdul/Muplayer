package com.example.muplayer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class TopItemCount(
    val name: String,
    val totalCount: Int,
    val totalTimeMs: Long
)

@Dao
interface StatisticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryEntry(entry: PlayHistoryEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyStat(stat: DailyListeningStat)

    @Query("SELECT * FROM daily_listening_stats WHERE dateString = :dateString")
    suspend fun getDailyStat(dateString: String): DailyListeningStat?

    @Query("SELECT * FROM play_history ORDER BY timestamp DESC LIMIT 100")
    fun getAllHistory(): Flow<List<PlayHistoryEntry>>

    @Query("SELECT * FROM play_history WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getHistorySince(sinceTimestamp: Long): Flow<List<PlayHistoryEntry>>

    @Query("SELECT * FROM daily_listening_stats WHERE dateString >= :startDateString ORDER BY dateString ASC")
    fun getDailyStatsSince(startDateString: String): Flow<List<DailyListeningStat>>

    @Query("SELECT SUM(durationListenedMs) FROM play_history")
    fun getTotalListeningTimeMs(): Flow<Long?>

    @Query("SELECT SUM(durationListenedMs) FROM play_history WHERE timestamp >= :sinceTimestamp")
    fun getTotalListeningTimeMsSince(sinceTimestamp: Long): Flow<Long?>

    @Query("SELECT artist as name, COUNT(*) as totalCount, SUM(durationListenedMs) as totalTimeMs FROM play_history WHERE timestamp >= :sinceTimestamp AND artist != '' AND artist != 'Unknown Artist' GROUP BY artist ORDER BY totalTimeMs DESC LIMIT 10")
    fun getTopArtistsSince(sinceTimestamp: Long): Flow<List<TopItemCount>>

    @Query("SELECT album as name, COUNT(*) as totalCount, SUM(durationListenedMs) as totalTimeMs FROM play_history WHERE timestamp >= :sinceTimestamp AND album != '' AND album != 'Unknown Album' GROUP BY album ORDER BY totalTimeMs DESC LIMIT 10")
    fun getTopAlbumsSince(sinceTimestamp: Long): Flow<List<TopItemCount>>

    @Query("DELETE FROM play_history")
    suspend fun clearHistory()

    @Query("DELETE FROM daily_listening_stats")
    suspend fun clearDailyStats()
}
