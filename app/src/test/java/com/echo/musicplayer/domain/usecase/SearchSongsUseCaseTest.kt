package com.echo.musicplayer.domain.usecase

import com.echo.musicplayer.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchSongsUseCaseTest {
    private val useCase = SearchSongsUseCase()
    private val songs = listOf(
        song("1", "On My Way", "Alan Walker", "Single"),
        song("2", "Run Down The City", "Monica Dhurandhar", "Dhurandhar"),
        song("3", "Title Track", "Dhurandhar", "Dhurandhar"),
    )

    @Test
    fun `blank query returns all songs`() {
        assertEquals(songs, useCase(songs, " "))
    }

    @Test
    fun `search matches title artist and album`() {
        assertEquals(listOf(songs[0]), useCase(songs, "alan"))
        assertEquals(listOf(songs[1]), useCase(songs, "city"))
        assertEquals(listOf(songs[1], songs[2]), useCase(songs, "dhurandhar"))
    }

    private fun song(id: String, title: String, artist: String, album: String) = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = 1,
        audioUrl = "https://github.com/example/echo/releases/download/music/$id.mp3",
        fileName = "$id.mp3",
        sizeBytes = 1,
        updatedAt = 0,
        fileHash = id,
    )
}
