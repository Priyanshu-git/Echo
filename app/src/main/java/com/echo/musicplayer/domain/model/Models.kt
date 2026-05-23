package com.echo.musicplayer.domain.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val storagePath: String,
    val fileName: String,
    val sizeBytes: Long,
    val uploadedAt: Long,
    val updatedAt: Long,
    val fileHash: String,
    val coverArtUri: String? = null,
    val localPath: String? = null,
    val isFavorite: Boolean = false,
    val downloadStatus: DownloadStatus = DownloadStatus.NotDownloaded,
    val downloadProgress: Float = 0f,
)

enum class DownloadStatus { NotDownloaded, Queued, Downloading, Downloaded, Failed }

data class SongMetadataDraft(
    val sourceUri: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val fileName: String,
    val coverArtUri: String? = null,
)

data class AppSettings(
    val primaryColorArgb: Long = 0xFFB45CFF,
    val downloadOverWifiOnly: Boolean = true,
    val keepScreenOnWhilePlaying: Boolean = true,
)

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val progressMs: Long = 0,
    val queue: List<Song> = emptyList(),
)

data class StorageUsage(val downloadedBytes: Long = 0, val cacheBytes: Long = 0, val otherBytes: Long = 0) {
    val totalBytes: Long = downloadedBytes + cacheBytes + otherBytes
}
