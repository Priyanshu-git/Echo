package com.echo.musicplayer.data

import com.echo.musicplayer.domain.model.AppSettings
import com.echo.musicplayer.domain.model.DownloadStatus
import com.echo.musicplayer.domain.model.PlaybackState
import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.domain.model.SongMetadataDraft
import com.echo.musicplayer.domain.model.StorageUsage
import com.echo.musicplayer.domain.repository.DownloadRepository
import com.echo.musicplayer.domain.repository.FavoritesRepository
import com.echo.musicplayer.domain.repository.MusicLibraryRepository
import com.echo.musicplayer.domain.repository.PlaybackController
import com.echo.musicplayer.domain.repository.SettingsRepository
import com.echo.musicplayer.domain.repository.StorageRepository
import com.echo.musicplayer.domain.repository.UploadRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryMusicLibraryRepository @Inject constructor(
    private val store: InMemoryStore,
) : MusicLibraryRepository {
    override val songs: Flow<List<Song>> = store.songs
    override suspend fun refresh() = Unit
    override suspend fun findByHash(fileHash: String): Song? = store.songs.value.firstOrNull { it.fileHash == fileHash }
}

@Singleton
class InMemoryUploadRepository @Inject constructor(
    private val store: InMemoryStore,
) : UploadRepository {
    override suspend fun prepareDraft(sourceUri: String): SongMetadataDraft = SongMetadataDraft(
        sourceUri = sourceUri,
        title = "New Song",
        artist = "Unknown Artist",
        album = "Single",
        durationMs = 194_000,
        fileSizeBytes = 5_200_000,
        fileName = "New Song.mp3",
    )

    override fun validateDraft(draft: SongMetadataDraft): List<String> = buildList {
        if (draft.title.isBlank()) add("Title is required")
        if (draft.artist.isBlank()) add("Artist is required")
        if (draft.album.isBlank()) add("Album is required")
        if (!draft.fileName.endsWith(".mp3", ignoreCase = true)) add("Only MP3 files are supported")
    }

    override suspend fun upload(draft: SongMetadataDraft, onProgress: (Float) -> Unit): Result<Song> {
        repeat(5) { index ->
            delay(80)
            onProgress((index + 1) / 5f)
        }
        val hash = "hash-${UUID.randomUUID()}"
        val song = Song(
            id = hash,
            title = draft.title,
            artist = draft.artist,
            album = draft.album,
            durationMs = draft.durationMs,
            storagePath = "songs/$hash.mp3",
            fileName = draft.fileName,
            sizeBytes = draft.fileSizeBytes,
            uploadedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            fileHash = hash,
        )
        store.songs.value = store.songs.value + song
        return Result.success(song)
    }
}

@Singleton
class InMemoryFavoritesRepository @Inject constructor(
    private val store: InMemoryStore,
) : FavoritesRepository {
    override val favoriteIds: Flow<Set<String>> = store.songs.map { songs ->
        songs.filter { it.isFavorite }.map { it.id }.toSet()
    }

    override suspend fun toggleFavorite(songId: String) {
        store.updateSong(songId) { song -> song.copy(isFavorite = !song.isFavorite) }
    }
}

@Singleton
class InMemoryDownloadRepository @Inject constructor(
    private val store: InMemoryStore,
) : DownloadRepository {
    override val downloadSongs: Flow<List<Song>> = store.songs

    override suspend fun download(song: Song) {
        store.updateSong(song.id) { it.copy(downloadStatus = DownloadStatus.Downloading, downloadProgress = 0.2f) }
        delay(150)
        store.updateSong(song.id) { it.copy(downloadStatus = DownloadStatus.Downloaded, downloadProgress = 1f, localPath = "offline/${song.fileName}") }
    }

    override suspend fun downloadAll(songs: List<Song>, onProgress: (completed: Int, total: Int) -> Unit) {
        val pending = songs.filterNot { it.downloadStatus == DownloadStatus.Downloaded }
        pending.forEachIndexed { index, item ->
            download(item)
            onProgress(index + 1, pending.size)
        }
    }

    override suspend fun cancelAll() {
        store.songs.value = store.songs.value.map { song ->
            if (song.downloadStatus == DownloadStatus.Downloading || song.downloadStatus == DownloadStatus.Queued) {
                song.copy(downloadStatus = DownloadStatus.NotDownloaded, downloadProgress = 0f)
            } else {
                song
            }
        }
    }

    override suspend fun delete(song: Song) {
        store.updateSong(song.id) { it.copy(downloadStatus = DownloadStatus.NotDownloaded, downloadProgress = 0f, localPath = null) }
    }
}

@Singleton
class InMemorySettingsRepository @Inject constructor(
    private val store: InMemoryStore,
) : SettingsRepository {
    override val settings: Flow<AppSettings> = store.settings

    override suspend fun setPrimaryColor(argb: Long) {
        store.settings.value = store.settings.value.copy(primaryColorArgb = argb)
    }

    override suspend fun setDownloadOverWifiOnly(enabled: Boolean) {
        store.settings.value = store.settings.value.copy(downloadOverWifiOnly = enabled)
    }

    override suspend fun setKeepScreenOnWhilePlaying(enabled: Boolean) {
        store.settings.value = store.settings.value.copy(keepScreenOnWhilePlaying = enabled)
    }
}

@Singleton
class InMemoryStorageRepository @Inject constructor(
    private val store: InMemoryStore,
) : StorageRepository {
    override val usage: Flow<StorageUsage> = store.storageUsage

    override suspend fun clearDownloads() {
        store.storageUsage.value = store.storageUsage.value.copy(downloadedBytes = 0)
        store.songs.value = store.songs.value.map {
            it.copy(downloadStatus = DownloadStatus.NotDownloaded, downloadProgress = 0f, localPath = null)
        }
    }

    override suspend fun clearCache() {
        store.storageUsage.value = store.storageUsage.value.copy(cacheBytes = 0)
    }
}

@Singleton
class InMemoryPlaybackController @Inject constructor(
    private val store: InMemoryStore,
) : PlaybackController {
    override val state: Flow<PlaybackState> = store.playback

    override suspend fun play(song: Song, queue: List<Song>) {
        store.playback.value = PlaybackState(
            currentSong = song,
            isPlaying = true,
            progressMs = 62_000,
            queue = queue.ifEmpty { store.songs.value },
        )
    }

    override suspend fun togglePlayPause() {
        store.playback.value = store.playback.value.let { it.copy(isPlaying = !it.isPlaying) }
    }

    override suspend fun next() {
        val state = store.playback.value
        val queue = state.queue.ifEmpty { store.songs.value }
        val currentIndex = queue.indexOfFirst { it.id == state.currentSong?.id }
        val next = queue.getOrNull(currentIndex + 1) ?: queue.firstOrNull()
        store.playback.value = state.copy(currentSong = next, isPlaying = next != null, progressMs = 0, queue = queue)
    }

    override suspend fun previous() {
        val state = store.playback.value
        val queue = state.queue.ifEmpty { store.songs.value }
        val currentIndex = queue.indexOfFirst { it.id == state.currentSong?.id }
        val previous = queue.getOrNull(currentIndex - 1) ?: queue.lastOrNull()
        store.playback.value = state.copy(currentSong = previous, isPlaying = previous != null, progressMs = 0, queue = queue)
    }

    override suspend fun seekTo(positionMs: Long) {
        store.playback.value = store.playback.value.copy(progressMs = positionMs.coerceAtLeast(0))
    }

    override suspend fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val queue = store.playback.value.queue.toMutableList()
        if (fromIndex in queue.indices && toIndex in queue.indices) {
            val item = queue.removeAt(fromIndex)
            queue.add(toIndex, item)
            store.playback.value = store.playback.value.copy(queue = queue)
        }
    }

    override suspend fun removeFromQueue(songId: String) {
        store.playback.value = store.playback.value.let { state ->
            state.copy(queue = state.queue.filterNot { it.id == songId })
        }
    }

    override suspend fun clearQueue() {
        store.playback.value = store.playback.value.copy(queue = store.playback.value.currentSong?.let(::listOf).orEmpty())
    }
}
