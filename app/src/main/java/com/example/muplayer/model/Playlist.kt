package com.example.muplayer.model

data class Playlist(
    val id: String,
    val name: String,
    val trackIds: List<Long>
)
