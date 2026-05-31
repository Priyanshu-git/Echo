package com.echo.musicplayer.data.remote

import android.util.Log
import com.echo.musicplayer.data.local.SongDao
import com.echo.musicplayer.data.local.SongEntity
import com.echo.musicplayer.data.local.toDomain
import com.echo.musicplayer.data.local.toEntity
import com.echo.musicplayer.domain.model.DownloadStatus
import com.echo.musicplayer.domain.model.LibraryStatus
import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.domain.repository.MusicLibraryRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Instant
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirestoreMusicLibraryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val songDao: SongDao,
) : MusicLibraryRepository {
    private val _status = MutableStateFlow(LibraryStatus.Idle)
    override val status: StateFlow<LibraryStatus> = _status

    override val songs: Flow<List<Song>> = songDao.observeSongs()
        .onStart { refresh() }
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun refresh() {
        Log.d(TAG, "Checking Firestore collection=$COLLECTION_SONGS")
        _status.value = LibraryStatus.CheckingFirestore
        runCatching { refreshFromFirestore() }
            .onSuccess { syncedSongs ->
                _status.value = if (syncedSongs == 0) LibraryStatus.Empty else LibraryStatus.Synced
                Log.i(TAG, "Firestore sync complete songs=$syncedSongs status=${_status.value}")
            }
            .onFailure { error ->
                Log.w(TAG, "Unable to refresh library from Firestore; using local library.", error)
                _status.value = if (songDao.countSongs() > 0) LibraryStatus.OfflineUsingCache else LibraryStatus.Failed
                Log.w(TAG, "Firestore sync failed status=${_status.value} cachedSongs=${songDao.countSongs()}")
            }
    }

    private suspend fun refreshFromFirestore(): Int {
        val snapshot = firestore.collection(COLLECTION_SONGS).get().await()
        Log.d(TAG, "Firestore returned documents=${snapshot.documents.size}")
        val songs = snapshot.documents.mapNotNull { document ->
            val songId = document.songId()
            val fileHash = document.fileHash()
            document.toSong(songDao.findByIdOrHash(songId, fileHash))
        }
        val skipped = snapshot.documents.size - songs.size
        if (skipped > 0) Log.w(TAG, "Skipped invalid Firestore song documents count=$skipped")
        songDao.replaceLibrary(songs.map { it.toEntity() })
        return songs.size
    }

    override suspend fun findByHash(fileHash: String): Song? = songDao.findByHash(fileHash)?.toDomain()

    private fun DocumentSnapshot.toSong(localEntity: SongEntity?): Song? {
        val local = localEntity?.toDomain()
        val title = getString("title")?.takeIf { it.isNotBlank() } ?: return null
        val audioUrl = getString("audioUrl")
            ?.trim()
            ?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
            ?: return null
        val fileHash = fileHash()
        return Song(
            id = songId(),
            title = title,
            artist = getString("artist").orEmpty(),
            album = getString("album").orEmpty(),
            durationMs = getLong("durationMs") ?: 0L,
            audioUrl = audioUrl,
            fileName = getString("fileName").orEmpty(),
            sizeBytes = getLong("sizeBytes") ?: 0L,
            updatedAt = get("updatedAt").toEpochMillis(),
            fileHash = fileHash,
            coverArtUrl = getString("coverArtUrl")
                ?.trim()
                ?.takeIf { it.startsWith("http://") || it.startsWith("https://") },
            coverArtUri = local?.coverArtUri,
            localPath = local?.localPath,
            isFavorite = local?.isFavorite ?: false,
            downloadStatus = local?.downloadStatus ?: DownloadStatus.NotDownloaded,
            downloadProgress = local?.downloadProgress ?: 0f,
            downloadFailureReason = local?.downloadFailureReason,
        )
    }

    private fun DocumentSnapshot.songId(): String = getString("fileHash")?.takeIf { it.isNotBlank() } ?: id

    private fun DocumentSnapshot.fileHash(): String = getString("fileHash")?.takeIf { it.isNotBlank() } ?: id

    private fun Any?.toEpochMillis(): Long = when (this) {
        is Timestamp -> toDate().time
        is Date -> time
        is Number -> toLong()
        is String -> runCatching { Instant.parse(this).toEpochMilli() }.getOrDefault(0L)
        else -> 0L
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { error -> continuation.resumeWithException(error) }
        addOnCanceledListener { continuation.cancel() }
    }

    private companion object {
        const val TAG = "EchoDownload"
        const val COLLECTION_SONGS = "songs"
    }
}
