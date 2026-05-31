package com.echo.musicplayer.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echo.musicplayer.domain.model.AppSettings
import com.echo.musicplayer.domain.model.DownloadStatus
import com.echo.musicplayer.domain.model.LibraryStatus
import com.echo.musicplayer.domain.model.PlaybackState
import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.domain.model.StorageUsage
import com.echo.musicplayer.domain.model.ThemeMode
import com.echo.musicplayer.domain.repository.DownloadRepository
import com.echo.musicplayer.domain.repository.FavoritesRepository
import com.echo.musicplayer.domain.repository.MusicLibraryRepository
import com.echo.musicplayer.domain.repository.PlaybackController
import com.echo.musicplayer.domain.repository.SettingsRepository
import com.echo.musicplayer.domain.repository.StorageRepository
import com.echo.musicplayer.domain.usecase.SearchSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EchoAppUiState(
    val songs: List<Song> = emptyList(),
    val playback: PlaybackState = PlaybackState(),
    val settings: AppSettings = AppSettings(),
    val storageUsage: StorageUsage = StorageUsage(),
    val libraryStatus: LibraryStatus = LibraryStatus.Idle,
    val searchQuery: String = "",
    val batchDownloadIds: Set<String> = emptySet(),
) {
    val filteredSongs: List<Song>
        get() = songs

    val favorites: List<Song>
        get() = songs.filter { it.isFavorite }

    val downloaded: List<Song>
        get() = songs.filter { it.downloadStatus == DownloadStatus.Downloaded }

    val downloading: List<Song>
        get() = songs.filter { it.downloadStatus == DownloadStatus.Downloading || it.downloadStatus == DownloadStatus.Queued }

    val failedDownloads: List<Song>
        get() = songs.filter { it.downloadStatus == DownloadStatus.Failed }

    val batchSongs: List<Song>
        get() = songs.filter { it.id in batchDownloadIds }

    val batchCompletedCount: Int
        get() = batchSongs.count { it.downloadStatus == DownloadStatus.Downloaded || it.downloadStatus == DownloadStatus.Failed || it.downloadStatus == DownloadStatus.Cancelled }

    val batchFailedCount: Int
        get() = batchSongs.count { it.downloadStatus == DownloadStatus.Failed }

    val downloadAllProgress: Float
        get() = if (batchDownloadIds.isEmpty()) 0f else batchCompletedCount / batchDownloadIds.size.toFloat()

    val totalLibraryBytes: Long
        get() = songs.sumOf { it.sizeBytes.coerceAtLeast(0L) }
}

private data class EchoBaseState(
    val songs: List<Song>,
    val playback: PlaybackState,
    val settings: AppSettings,
    val storageUsage: StorageUsage,
    val libraryStatus: LibraryStatus,
)

@HiltViewModel
class EchoAppViewModel @Inject constructor(
    private val libraryRepository: MusicLibraryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val downloadRepository: DownloadRepository,
    private val settingsRepository: SettingsRepository,
    private val storageRepository: StorageRepository,
    private val playbackController: PlaybackController,
    private val searchSongs: SearchSongsUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val batchDownloadIds = MutableStateFlow(emptySet<String>())

    private val baseState = combine(
        libraryRepository.songs,
        libraryRepository.status,
        playbackController.state,
        settingsRepository.settings,
        storageRepository.usage,
    ) { songs, libraryStatus, playback, settings, storage ->
        EchoBaseState(songs, playback, settings, storage, libraryStatus)
    }

    val state: StateFlow<EchoAppUiState> = combine(
        baseState,
        query,
        batchDownloadIds,
    ) { base, searchQuery, batchIds ->
        EchoAppUiState(
            songs = searchSongs(base.songs, searchQuery),
            playback = base.playback,
            settings = base.settings,
            storageUsage = base.storageUsage,
            libraryStatus = base.libraryStatus,
            searchQuery = searchQuery,
            batchDownloadIds = batchIds,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EchoAppUiState())

    fun refreshLibrary() {
        viewModelScope.launch { libraryRepository.refresh() }
    }

    fun setSearchQuery(value: String) {
        query.value = value
    }

    fun play(song: Song) {
        viewModelScope.launch { playbackController.play(song, state.value.songs) }
    }

    fun togglePlayPause() {
        viewModelScope.launch { playbackController.togglePlayPause() }
    }

    fun next() {
        viewModelScope.launch { playbackController.next() }
    }

    fun previous() {
        viewModelScope.launch { playbackController.previous() }
    }

    fun seekTo(positionMs: Long) {
        viewModelScope.launch { playbackController.seekTo(positionMs) }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(song.id) }
    }

    fun download(song: Song) {
        viewModelScope.launch { downloadRepository.download(song) }
    }

    fun deleteDownload(song: Song) {
        viewModelScope.launch { downloadRepository.delete(song) }
    }

    fun downloadAll() {
        viewModelScope.launch {
            val songs = state.value.songs
            batchDownloadIds.value = songs.filterNot { it.downloadStatus == DownloadStatus.Downloaded }.map { it.id }.toSet()
            downloadRepository.downloadAll(songs) { completed, total ->
                if (total == 0 || completed == total) {
                    batchDownloadIds.value = batchDownloadIds.value
                }
            }
        }
    }

    fun cancelDownloads() {
        viewModelScope.launch {
            downloadRepository.cancelAll()
            batchDownloadIds.value = emptySet()
        }
    }

    fun clearDownloads() {
        viewModelScope.launch { storageRepository.clearDownloads() }
    }

    fun clearCache() {
        viewModelScope.launch { storageRepository.clearCache() }
    }

    fun setPrimaryColor(argb: Long) {
        viewModelScope.launch { settingsRepository.setPrimaryColor(argb) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setKeepScreenOnWhilePlaying(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setKeepScreenOnWhilePlaying(enabled) }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch { playbackController.reorderQueue(fromIndex, toIndex) }
    }

    fun removeFromQueue(song: Song) {
        viewModelScope.launch { playbackController.removeFromQueue(song.id) }
    }

    fun clearQueue() {
        viewModelScope.launch { playbackController.clearQueue() }
    }

}
