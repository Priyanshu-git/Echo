package com.echo.musicplayer.domain.repository

import com.echo.musicplayer.domain.model.AppSettings
import com.echo.musicplayer.domain.model.ConnectivityStatus
import com.echo.musicplayer.domain.model.LibraryStatus
import com.echo.musicplayer.domain.model.PlaybackState
import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.domain.model.StorageUsage
import com.echo.musicplayer.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface MusicLibraryRepository { val songs: Flow<List<Song>>; val status: Flow<LibraryStatus>; suspend fun refresh(); suspend fun findByHash(fileHash: String): Song? }
interface FavoritesRepository { val favoriteIds: Flow<Set<String>>; suspend fun toggleFavorite(songId: String) }
interface DownloadRepository { val downloadSongs: Flow<List<Song>>; suspend fun download(song: Song); suspend fun downloadAll(songs: List<Song>, onProgress: (completed: Int, total: Int) -> Unit); suspend fun cancelAll(); suspend fun delete(song: Song) }
interface SettingsRepository { val settings: Flow<AppSettings>; suspend fun setPrimaryColor(argb: Long); suspend fun setThemeMode(mode: ThemeMode); suspend fun setKeepScreenOnWhilePlaying(enabled: Boolean) }
interface StorageRepository { val usage: Flow<StorageUsage>; suspend fun clearDownloads(); suspend fun clearCache() }
interface PlaybackController { val state: Flow<PlaybackState>; suspend fun play(song: Song, queue: List<Song> = emptyList()); suspend fun togglePlayPause(); suspend fun next(); suspend fun previous(); suspend fun seekTo(positionMs: Long); suspend fun reorderQueue(fromIndex: Int, toIndex: Int); suspend fun removeFromQueue(songId: String); suspend fun clearQueue() }
interface ConnectivityRepository { val status: Flow<ConnectivityStatus> }
