package com.echo.musicplayer.domain.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val audioUrl: String,
    val fileName: String,
    val sizeBytes: Long,
    val updatedAt: Long,
    val fileHash: String,
    val coverArtUri: String? = null,
    val localPath: String? = null,
    val isFavorite: Boolean = false,
    val downloadStatus: DownloadStatus = DownloadStatus.NotDownloaded,
    val downloadProgress: Float = 0f,
    val downloadFailureReason: String? = null,
)

enum class DownloadStatus { NotDownloaded, Queued, Downloading, Downloaded, Failed, Cancelled }

enum class LibraryStatus { Idle, CheckingFirestore, Synced, Empty, OfflineUsingCache, Failed }

enum class ConnectivityStatus { Online, Offline }

data class AppSettings(
    val primaryColorArgb: Long = 0xFFB45CFF,
    val themeMode: ThemeMode = ThemeMode.System,
    val keepScreenOnWhilePlaying: Boolean = true,
)

enum class ThemeMode { Light, Dark, System }

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val progressMs: Long = 0,
    val queue: List<Song> = emptyList(),
)

data class StorageUsage(val downloadedBytes: Long = 0, val cacheBytes: Long = 0, val otherBytes: Long = 0) {
    val totalBytes: Long = downloadedBytes + cacheBytes + otherBytes
}
