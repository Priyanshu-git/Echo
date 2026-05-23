package com.echo.musicplayer.domain.usecase

import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.domain.model.SongMetadataDraft
import javax.inject.Inject

class SearchSongsUseCase @Inject constructor() {
    operator fun invoke(songs: List<Song>, query: String): List<Song> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return songs
        return songs.filter { it.title.lowercase().contains(normalized) || it.artist.lowercase().contains(normalized) || it.album.lowercase().contains(normalized) }
    }
}

class ValidateUploadDraftUseCase @Inject constructor() {
    operator fun invoke(draft: SongMetadataDraft): List<String> = buildList {
        if (draft.title.isBlank()) add("Title is required")
        if (draft.artist.isBlank()) add("Artist is required")
        if (draft.album.isBlank()) add("Album is required")
        if (!draft.fileName.endsWith(".mp3", ignoreCase = true)) add("Only MP3 files are supported")
        if (draft.fileSizeBytes < 0) add("File size is invalid")
    }
}
