package com.echo.musicplayer.ui.app

import com.echo.musicplayer.domain.model.DownloadStatus
import com.echo.musicplayer.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EchoAppUiStateTest {
    @Test
    fun `download batch progress follows terminal statuses`() {
        val songs = listOf(
            song("ready", DownloadStatus.Queued),
            song("done", DownloadStatus.Downloaded),
            song("failed", DownloadStatus.Failed),
            song("other", DownloadStatus.NotDownloaded),
        )

        val state = EchoAppUiState(
            songs = songs,
            batchDownloadIds = setOf("ready", "done", "failed"),
        )

        assertEquals(1, state.downloading.size)
        assertEquals(1, state.failedDownloads.size)
        assertEquals(2, state.batchCompletedCount)
        assertEquals(1, state.batchFailedCount)
        assertEquals(2f / 3f, state.downloadAllProgress, 0.0001f)
    }

    @Test
    fun `total library bytes ignores invalid negative sizes`() {
        val state = EchoAppUiState(
            songs = listOf(
                song("one", sizeBytes = 10),
                song("bad", sizeBytes = -5),
            ),
        )

        assertEquals(10L, state.totalLibraryBytes)
    }

    @Test
    fun `offline mode shows only downloaded songs with local paths`() {
        val downloaded = song("downloaded", DownloadStatus.Downloaded, localPath = "/downloads/downloaded.mp3")
        val missingPath = song("missing-path", DownloadStatus.Downloaded, localPath = "")
        val streamingOnly = song("streaming", DownloadStatus.NotDownloaded)
        val state = EchoAppUiState(
            songs = listOf(downloaded, missingPath, streamingOnly),
            isOfflineMode = true,
        )

        assertEquals(listOf(downloaded), state.availableOfflineSongs)
        assertEquals(listOf(downloaded), state.visibleLibrarySongs)
    }

    @Test
    fun `normal mode shows full searched library`() {
        val songs = listOf(
            song("downloaded", DownloadStatus.Downloaded, localPath = "/downloads/downloaded.mp3"),
            song("streaming", DownloadStatus.NotDownloaded),
        )
        val state = EchoAppUiState(songs = songs, isOfflineMode = false)

        assertEquals(songs, state.visibleLibrarySongs)
    }

    @Test
    fun `online resume is available only while offline mode has online network`() {
        assertTrue(EchoAppUiState(isOfflineMode = true, isNetworkOnline = true).hasOnlineResumeAvailable)
        assertFalse(EchoAppUiState(isOfflineMode = true, isNetworkOnline = false).hasOnlineResumeAvailable)
        assertFalse(EchoAppUiState(isOfflineMode = false, isNetworkOnline = true).hasOnlineResumeAvailable)
    }

    private fun song(
        id: String,
        status: DownloadStatus = DownloadStatus.NotDownloaded,
        sizeBytes: Long = 1,
        localPath: String? = null,
    ) = Song(
        id = id,
        title = id,
        artist = "Artist",
        album = "Album",
        durationMs = 1,
        audioUrl = "https://example.com/$id.mp3",
        fileName = "$id.mp3",
        sizeBytes = sizeBytes,
        updatedAt = 0,
        fileHash = id,
        localPath = localPath,
        downloadStatus = status,
    )
}
