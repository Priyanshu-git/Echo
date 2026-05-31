package com.echo.musicplayer.data.local

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.echo.musicplayer.download.DownloadSongWorker
import com.echo.musicplayer.domain.model.DownloadStatus
import com.echo.musicplayer.domain.model.LibraryStatus
import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.domain.model.StorageUsage
import com.echo.musicplayer.domain.repository.DownloadRepository
import com.echo.musicplayer.domain.repository.FavoritesRepository
import com.echo.musicplayer.domain.repository.MusicLibraryRepository
import com.echo.musicplayer.domain.repository.StorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "EchoDownload"

@Singleton
class RoomMusicLibraryRepository @Inject constructor(
    private val songDao: SongDao,
) : MusicLibraryRepository {
    override val songs: Flow<List<Song>> = songDao.observeSongs()
        .map { entities -> entities.map { it.toDomain() } }
    override val status: Flow<LibraryStatus> = flowOf(LibraryStatus.Synced)

    override suspend fun refresh() = Unit

    override suspend fun findByHash(fileHash: String): Song? = songDao.findByHash(fileHash)?.toDomain()
}

@Singleton
class RoomFavoritesRepository @Inject constructor(
    private val songDao: SongDao,
) : FavoritesRepository {
    override val favoriteIds: Flow<Set<String>> = songDao.observeSongs().map { songs ->
        songs.filter { it.isFavorite }.map { it.id }.toSet()
    }

    override suspend fun toggleFavorite(songId: String) {
        songDao.toggleFavorite(songId)
    }
}

@Singleton
class LocalDownloadRepository @Inject constructor(
    private val songDao: SongDao,
    private val workManager: WorkManager,
) : DownloadRepository {
    override val downloadSongs: Flow<List<Song>> = songDao.observeSongs().map { songs -> songs.map { it.toDomain() } }

    override suspend fun download(song: Song) {
        if (song.downloadStatus == DownloadStatus.Downloaded) {
            Log.d(TAG, "Download skipped; already downloaded songId=${song.id} file=${song.fileName}")
            return
        }
        songDao.updateDownload(song.id, DownloadStatus.Queued.name, 0f, song.localPath, null)
        val networkType = NetworkType.CONNECTED
        val uniqueName = DownloadSongWorker.uniqueName(song.id)
        Log.i(
            TAG,
            "Queueing download songId=${song.id} title=${song.title} networkType=$networkType workName=$uniqueName url=${song.audioUrl.safeUrlForLog()}",
        )
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()
        val request = OneTimeWorkRequestBuilder<DownloadSongWorker>()
            .setInputData(DownloadSongWorker.input(song.id))
            .setConstraints(constraints)
            .addTag(DownloadSongWorker.TAG_DOWNLOAD)
            .build()
        workManager.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
        Log.d(TAG, "Enqueued WorkManager download workName=$uniqueName workId=${request.id} songId=${song.id}")
    }

    override suspend fun downloadAll(songs: List<Song>, onProgress: (completed: Int, total: Int) -> Unit) {
        val pending = songs.filterNot { it.downloadStatus == DownloadStatus.Downloaded }
        Log.i(TAG, "Download all requested total=${songs.size} pending=${pending.size} alreadyDownloaded=${songs.size - pending.size}")
        pending.forEachIndexed { index, song ->
            runCatching { download(song) }
                .onFailure { error -> Log.w(TAG, "Unable to enqueue songId=${song.id} during download all", error) }
            onProgress(index + 1, pending.size)
        }
    }

    override suspend fun cancelAll() {
        Log.i(TAG, "Cancelling active downloads")
        workManager.cancelAllWorkByTag(DownloadSongWorker.TAG_DOWNLOAD)
        songDao.updateDownloadsWithStatuses(
            currentStatuses = listOf(DownloadStatus.Queued.name, DownloadStatus.Downloading.name),
            status = DownloadStatus.Cancelled.name,
            progress = 0f,
            failureReason = "Cancelled",
        )
    }

    override suspend fun delete(song: Song) {
        Log.i(TAG, "Deleting download songId=${song.id} localPath=${song.localPath}")
        workManager.cancelUniqueWork(DownloadSongWorker.uniqueName(song.id))
        song.localPath?.let { File(it).delete() }
        songDao.updateDownload(song.id, DownloadStatus.NotDownloaded.name, 0f, null, null)
    }
}

@Singleton
class LocalStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
) : StorageRepository {
    override val usage: Flow<StorageUsage> = songDao.observeSongs().map {
        StorageUsage(downloadedBytes = downloadDir().sizeRecursive(), cacheBytes = 0, otherBytes = 0)
    }

    override suspend fun clearDownloads() {
        withContext(Dispatchers.IO) {
            downloadDir().deleteRecursively()
        }
        songDao.updateAllDownloads(DownloadStatus.NotDownloaded.name, 0f, null)
    }

    override suspend fun clearCache() = Unit

    private fun downloadDir(): File = File(context.filesDir, "downloads")

    private fun File.sizeRecursive(): Long {
        if (!exists()) return 0L
        return walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

}

private fun String.safeUrlForLog(): String = runCatching {
    val uri = URI(this)
    buildString {
        append(uri.scheme ?: "unknown")
        append("://")
        append(uri.host ?: "unknown-host")
        append(uri.rawPath.orEmpty())
    }
}.getOrDefault("<invalid-url>")
