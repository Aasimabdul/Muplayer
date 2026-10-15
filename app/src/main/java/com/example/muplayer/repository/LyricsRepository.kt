package com.example.muplayer.repository

import android.content.Context
import android.net.Uri
import com.example.muplayer.model.LyricLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LyricsRepository(private val context: Context) {
    private val lyricsDir = File(context.filesDir, "lyrics").apply { mkdirs() }

    suspend fun importLyrics(trackId: Long, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val file = File(lyricsDir, "$trackId.lrc")
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLyrics(trackId: Long): List<LyricLine>? = withContext(Dispatchers.IO) {
        val file = File(lyricsDir, "$trackId.lrc")
        if (!file.exists()) return@withContext null

        val timestamped = mutableListOf<LyricLine>()
        val plainText = mutableListOf<LyricLine>()
        var globalOffsetMs = 0L

        val timestampPattern = Regex("\\[(\\d{1,2}):(\\d{2})(?:[\\.:](\\d{2,3}))?\\]")
        val offsetPattern = Regex("\\[offset:\\s*([+-]?\\d+)\\]", RegexOption.IGNORE_CASE)

        try {
            file.bufferedReader().useLines { lines ->
                for (rawLine in lines) {
                    val line = rawLine.trim()
                    if (line.isBlank()) continue

                    // Check for global offset tag [offset:+500]
                    val offsetMatch = offsetPattern.find(line)
                    if (offsetMatch != null) {
                        globalOffsetMs = offsetMatch.groupValues[1].toLongOrNull() ?: 0L
                        continue
                    }

                    // Check for metadata tags like [ar:Artist], [ti:Title], [al:Album]
                    if (line.matches(Regex("\\[[a-zA-Z]+:.*\\]"))) continue

                    // Match all timestamps on this line
                    val timestampMatches = timestampPattern.findAll(line).toList()
                    if (timestampMatches.isNotEmpty()) {
                        val text = line.replace(timestampPattern, "").trim()
                        for (match in timestampMatches) {
                            val min = match.groupValues[1].toLongOrNull() ?: 0L
                            val sec = match.groupValues[2].toLongOrNull() ?: 0L
                            val msPart = match.groupValues[3]
                            val ms = when {
                                msPart.length == 2 -> (msPart.toLongOrNull() ?: 0L) * 10
                                msPart.length == 3 -> msPart.toLongOrNull() ?: 0L
                                else -> 0L
                            }
                            val calculatedTimeMs = (min * 60 * 1000L) + (sec * 1000L) + ms + globalOffsetMs
                            if (text.isNotBlank()) {
                                timestamped.add(LyricLine(calculatedTimeMs.coerceAtLeast(0L), text))
                            }
                        }
                    } else {
                        // Plain text line
                        plainText.add(LyricLine(-1L, line))
                    }
                }
            }
        } catch (e: Exception) {
            return@withContext null
        }

        if (timestamped.isNotEmpty()) {
            timestamped.sortBy { it.timeMs }
            return@withContext timestamped
        }
        if (plainText.isNotEmpty()) {
            return@withContext plainText
        }
        return@withContext null
    }

    suspend fun deleteLyrics(trackId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(lyricsDir, "$trackId.lrc")
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            false
        }
    }
}
