package com.echo.musicplayer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.echo.musicplayer.domain.model.DownloadStatus
import com.echo.musicplayer.domain.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val audioUrl: String,
    val fileName: String,
    val sizeBytes: Long,
    val updatedAt: Long,
    val fileHash: String,
    val coverArtUri: String?,
    val localPath: String?,
    val isFavorite: Boolean,
    val downloadStatus: String,
    val downloadProgress: Float,
    val downloadFailureReason: String?,
)

fun SongEntity.toDomain(): Song = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    audioUrl = audioUrl,
    fileName = fileName,
    sizeBytes = sizeBytes,
    updatedAt = updatedAt,
    fileHash = fileHash,
    coverArtUri = coverArtUri,
    localPath = localPath,
    isFavorite = isFavorite,
    downloadStatus = runCatching { DownloadStatus.valueOf(downloadStatus) }.getOrDefault(DownloadStatus.NotDownloaded),
    downloadProgress = downloadProgress,
    downloadFailureReason = downloadFailureReason,
)

fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    audioUrl = audioUrl,
    fileName = fileName,
    sizeBytes = sizeBytes,
    updatedAt = updatedAt,
    fileHash = fileHash,
    coverArtUri = coverArtUri,
    localPath = localPath,
    isFavorite = isFavorite,
    downloadStatus = downloadStatus.name,
    downloadProgress = downloadProgress,
    downloadFailureReason = downloadFailureReason,
)
