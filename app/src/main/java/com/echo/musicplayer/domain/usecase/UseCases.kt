package com.echo.musicplayer.domain.usecase

import com.echo.musicplayer.domain.model.Song
import javax.inject.Inject

class SearchSongsUseCase @Inject constructor() {
    operator fun invoke(songs: List<Song>, query: String): List<Song> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return songs
        return songs.filter { it.title.lowercase().contains(normalized) || it.artist.lowercase().contains(normalized) || it.album.lowercase().contains(normalized) }
    }
}
