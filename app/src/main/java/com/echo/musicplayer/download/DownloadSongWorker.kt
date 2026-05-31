package com.echo.musicplayer.download

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.echo.musicplayer.data.local.SongDao
import com.echo.musicplayer.data.local.toDomain
import com.echo.musicplayer.domain.model.DownloadStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

@HiltWorker
class DownloadSongWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val songDao: SongDao,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val songId = inputData.getString(KEY_SONG_ID) ?: run {
            Log.e(TAG_LOG, "Download worker failed; missing song id")
            return Result.failure()
        }
        Log.d(TAG_LOG, "Download worker started songId=$songId attempt=$runAttemptCount")
        val song = songDao.findById(songId)?.toDomain() ?: run {
            Log.e(TAG_LOG, "Download worker failed; song not found songId=$songId")
            return Result.failure()
        }
        val target = File(downloadDir(), "${song.fileHash}.mp3")
        val partial = File(downloadDir(), "${song.fileHash}.tmp")

        return runCatching {
            Log.i(TAG_LOG, "Downloading MP3 songId=${song.id} title=${song.title} expectedBytes=${song.sizeBytes} target=${target.absolutePath}")
            Log.d(TAG_LOG, "Setting download status songId=${song.id} status=${DownloadStatus.Downloading.name}")
            songDao.updateDownload(song.id, DownloadStatus.Downloading.name, 0f, null, null)
            var lastLoggedPercent = -1
            copySong(song.audioUrl, partial, song.sizeBytes) { progress ->
                setProgress(workDataOf(KEY_PROGRESS to progress))
                songDao.updateDownload(song.id, DownloadStatus.Downloading.name, progress, null, null)
                val percent = (progress * 100).toInt()
                if (percent >= 0 && percent / 25 > lastLoggedPercent / 25) {
                    lastLoggedPercent = percent
                    Log.d(TAG_LOG, "Download progress songId=${song.id} progress=$percent%")
                }
            }
            if (isStopped) {
                partial.delete()
                songDao.updateDownload(song.id, DownloadStatus.Cancelled.name, 0f, null, "Cancelled")
                Log.i(TAG_LOG, "Download cancelled songId=${song.id}")
                Result.failure()
            } else {
                if (partial.length() == 0L) error("Downloaded file is empty")
                target.delete()
                if (!partial.renameTo(target)) error("Could not save downloaded file")
                songDao.updateDownload(song.id, DownloadStatus.Downloaded.name, 1f, target.absolutePath, null)
                Log.i(TAG_LOG, "Download complete songId=${song.id} bytes=${target.length()} localPath=${target.absolutePath}")
                Result.success()
            }
        }.getOrElse { error ->
            partial.delete()
            target.delete()
            val reason = error.message?.takeIf { it.isNotBlank() } ?: "Download failed"
            songDao.updateDownload(song.id, DownloadStatus.Failed.name, 0f, null, reason)
            Log.e(TAG_LOG, "Download failed songId=${song.id} reason=$reason url=${song.audioUrl.safeUrlForLog()}", error)
            Result.failure()
        }
    }

    private suspend fun copySong(source: String, target: File, expectedBytes: Long, onProgress: suspend (Float) -> Unit) = withContext(Dispatchers.IO) {
        target.parentFile?.mkdirs()
        val connection = openConnection(source)
        try {
            Log.d(TAG_LOG, "Opening HTTP connection url=${source.safeUrlForLog()}")
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) error("HTTP $responseCode")
            val totalBytes = when {
                expectedBytes > 0 -> expectedBytes
                connection.contentLengthLong > 0 -> connection.contentLengthLong
                else -> 0L
            }
            Log.d(TAG_LOG, "HTTP response ok code=$responseCode contentLength=${connection.contentLengthLong} expectedBytes=$expectedBytes totalBytes=$totalBytes")
            connection.inputStream.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var copied = 0L
                    while (!isStopped) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        copied += read
                        val progress = if (totalBytes > 0) {
                            (copied.toFloat() / totalBytes).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                        onProgress(progress)
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(source: String): HttpURLConnection = when {
        source.startsWith("http://") || source.startsWith("https://") -> {
            val connection = URL(source).openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.instanceFollowRedirects = true
            connection
        }
        else -> error("Only network audio URLs are supported: $source")
    }

    private fun downloadDir(): File = File(applicationContext.filesDir, "downloads")

    companion object {
        const val KEY_SONG_ID = "song_id"
        const val KEY_PROGRESS = "progress"
        const val TAG_DOWNLOAD = "song-download"
        private const val TAG_LOG = "EchoDownload"

        fun input(songId: String): Data = workDataOf(KEY_SONG_ID to songId)
        fun uniqueName(songId: String): String = "download-$songId"
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
