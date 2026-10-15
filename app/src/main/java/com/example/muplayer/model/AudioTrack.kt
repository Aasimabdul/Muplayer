package com.example.muplayer.model

import android.net.Uri

data class AudioTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: Uri,
    val albumArtUri: Uri?,
    val dateAdded: Long = 0L,
    val folderName: String = "",
    val folderPath: String = ""
)
