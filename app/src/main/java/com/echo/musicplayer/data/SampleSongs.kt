package com.echo.musicplayer.data

import com.echo.musicplayer.domain.model.DownloadStatus
import com.echo.musicplayer.domain.model.Song

object SampleSongs {
    val songs = listOf(
        Song("1", "Believer", "Imagine Dragons", "Evolve", 204000, "songs/believer.mp3", "Believer.mp3", 5242880, 0, 0, "hash-believer", downloadStatus = DownloadStatus.Downloaded, downloadProgress = 1f),
        Song("2", "Thunder", "Imagine Dragons", "Evolve", 187000, "songs/thunder.mp3", "Thunder.mp3", 4812800, 0, 0, "hash-thunder"),
        Song("3", "Shape of You", "Ed Sheeran", "Divide", 233000, "songs/shape.mp3", "Shape of You.mp3", 6120000, 0, 0, "hash-shape", isFavorite = true),
        Song("4", "Someone You Loved", "Lewis Capaldi", "Divinely Uninspired", 182000, "songs/someone.mp3", "Someone You Loved.mp3", 5010000, 0, 0, "hash-someone"),
        Song("5", "Blinding Lights", "The Weeknd", "After Hours", 200000, "songs/blinding.mp3", "Blinding Lights.mp3", 5600000, 0, 0, "hash-blinding", isFavorite = true),
        Song("6", "Counting Stars", "OneRepublic", "Native", 257000, "songs/counting.mp3", "Counting Stars.mp3", 6800000, 0, 0, "hash-counting")
    )
}
