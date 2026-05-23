package com.echo.musicplayer.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echo.musicplayer.domain.model.AppSettings
import com.echo.musicplayer.domain.model.DownloadStatus
import com.echo.musicplayer.domain.model.PlaybackState
import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.domain.model.StorageUsage
import com.echo.musicplayer.domain.repository.DownloadRepository
import com.echo.musicplayer.domain.repository.FavoritesRepository
import com.echo.musicplayer.domain.repository.MusicLibraryRepository
import com.echo.musicplayer.domain.repository.PlaybackController
import com.echo.musicplayer.domain.repository.SettingsRepository
import com.echo.musicplayer.domain.repository.StorageRepository
import com.echo.musicplayer.domain.repository.UploadRepository
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
    val searchQuery: String = "",
    val downloadAllProgress: Float = 0f,
    val uploadProgress: Float = 0f,
) {
    val filteredSongs: List<Song>
        get() = songs

    val favorites: List<Song>
        get() = songs.filter { it.isFavorite }

    val downloaded: List<Song>
        get() = songs.filter { it.downloadStatus == DownloadStatus.Downloaded }

    val downloading: List<Song>
        get() = songs.filter { it.downloadStatus == DownloadStatus.Downloading || it.downloadStatus == DownloadStatus.Queued }
}

private data class EchoBaseState(
    val songs: List<Song>,
    val playback: PlaybackState,
    val settings: AppSettings,
    val storageUsage: StorageUsage,
)

@HiltViewModel
class EchoAppViewModel @Inject constructor(
    libraryRepository: MusicLibraryRepository,
    private val uploadRepository: UploadRepository,
    private val favoritesRepository: FavoritesRepository,
    private val downloadRepository: DownloadRepository,
    private val settingsRepository: SettingsRepository,
    private val storageRepository: StorageRepository,
    private val playbackController: PlaybackController,
    private val searchSongs: SearchSongsUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val downloadAllProgress = MutableStateFlow(0f)
    private val uploadProgress = MutableStateFlow(0f)

    private val baseState = combine(
        libraryRepository.songs,
        playbackController.state,
        settingsRepository.settings,
        storageRepository.usage,
    ) { songs, playback, settings, storage ->
        EchoBaseState(songs, playback, settings, storage)
    }

    val state: StateFlow<EchoAppUiState> = combine(
        baseState,
        query,
        downloadAllProgress,
        uploadProgress,
    ) { base, searchQuery, batchProgress, upload ->
        EchoAppUiState(
            songs = searchSongs(base.songs, searchQuery),
            playback = base.playback,
            settings = base.settings,
            storageUsage = base.storageUsage,
            searchQuery = searchQuery,
            downloadAllProgress = batchProgress,
            uploadProgress = upload,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EchoAppUiState())

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
            downloadAllProgress.value = 0f
            downloadRepository.downloadAll(songs) { completed, total ->
                downloadAllProgress.value = if (total == 0) 1f else completed / total.toFloat()
            }
        }
    }

    fun cancelDownloads() {
        viewModelScope.launch { downloadRepository.cancelAll() }
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

    fun setDownloadOverWifiOnly(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDownloadOverWifiOnly(enabled) }
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

    fun uploadSampleSong() {
        viewModelScope.launch {
            val draft = uploadRepository.prepareDraft("content://sample/new-song.mp3")
            uploadRepository.upload(draft) { uploadProgress.value = it }
        }
    }
}
