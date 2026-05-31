package com.echo.musicplayer.data

import com.echo.musicplayer.domain.model.AppSettings
import com.echo.musicplayer.domain.model.PlaybackState
import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.domain.model.StorageUsage
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryStore @Inject constructor() {
    val songs = MutableStateFlow(emptyList<Song>())
    val settings = MutableStateFlow(AppSettings())
    val playback = MutableStateFlow(PlaybackState())
    val storageUsage = MutableStateFlow(StorageUsage(downloadedBytes = 10_120_000_000, cacheBytes = 1_320_000_000))

    fun updateSong(songId: String, transform: (Song) -> Song) {
        songs.value = songs.value.map { song -> if (song.id == songId) transform(song) else song }
        playback.value = playback.value.let { state ->
            state.copy(
                currentSong = state.currentSong?.let { if (it.id == songId) transform(it) else it },
                queue = state.queue.map { song -> if (song.id == songId) transform(song) else song }
            )
        }
    }
}
