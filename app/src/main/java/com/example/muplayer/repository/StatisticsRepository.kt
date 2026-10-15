package com.example.muplayer.repository

import android.content.Context
import com.example.muplayer.data.AppDatabase
import com.example.muplayer.data.DailyListeningStat
import com.example.muplayer.data.PlayHistoryEntry
import com.example.muplayer.data.SongMetadata
import com.example.muplayer.data.TopItemCount
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val statisticsDao = db.statisticsDao()
    private val songDao = db.songDao()

    fun getAllHistory(): Flow<List<PlayHistoryEntry>> = statisticsDao.getAllHistory()

    fun getHistorySince(sinceTimestamp: Long): Flow<List<PlayHistoryEntry>> =
        statisticsDao.getHistorySince(sinceTimestamp)

    fun getDailyStatsForPastDays(days: Int): Flow<List<DailyListeningStat>> {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        return statisticsDao.getDailyStatsSince(dateString)
    }

    fun getTotalListeningTimeMsSince(sinceTimestamp: Long): Flow<Long?> =
        if (sinceTimestamp <= 0L) statisticsDao.getTotalListeningTimeMs()
        else statisticsDao.getTotalListeningTimeMsSince(sinceTimestamp)

    fun getTopArtistsSince(sinceTimestamp: Long): Flow<List<TopItemCount>> =
        statisticsDao.getTopArtistsSince(sinceTimestamp)

    fun getTopAlbumsSince(sinceTimestamp: Long): Flow<List<TopItemCount>> =
        statisticsDao.getTopAlbumsSince(sinceTimestamp)

    suspend fun recordListeningSession(
        trackId: Long,
        title: String,
        artist: String,
        album: String,
        timestamp: Long,
        durationListenedMs: Long,
        isCountedPlay: Boolean
    ) {
        if (durationListenedMs < 1000L) return

        val entry = PlayHistoryEntry(
            trackId = trackId,
            title = title,
            artist = artist,
            album = album,
            timestamp = timestamp,
            durationListenedMs = durationListenedMs
        )
        statisticsDao.insertHistoryEntry(entry)

        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
        val currentStat = statisticsDao.getDailyStat(dateString)
        val newTime = (currentStat?.totalListeningTimeMs ?: 0L) + durationListenedMs
        statisticsDao.insertOrUpdateDailyStat(DailyListeningStat(dateString, newTime))

        if (isCountedPlay && trackId > 0) {
            val metadata = SongMetadata(
                songId = trackId,
                playCount = 1,
                lastPlayedTimestamp = timestamp
            )
            songDao.insertOrUpdate(metadata)
        }
    }

    suspend fun clearListeningStatistics() {
        statisticsDao.clearHistory()
        statisticsDao.clearDailyStats()
    }
}
