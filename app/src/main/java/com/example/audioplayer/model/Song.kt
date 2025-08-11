package com.example.audioplayer.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String?,
    val duration: Long,
    val uri: android.net.Uri
)