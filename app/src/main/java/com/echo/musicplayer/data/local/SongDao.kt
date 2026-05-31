package com.echo.musicplayer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    fun observeSongs(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun countSongs(): Int

    @Query("SELECT * FROM songs WHERE fileHash = :fileHash LIMIT 1")
    suspend fun findByHash(fileHash: String): SongEntity?

    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    suspend fun findById(songId: String): SongEntity?

    @Query("SELECT * FROM songs WHERE id = :songId OR fileHash = :fileHash LIMIT 1")
    suspend fun findByIdOrHash(songId: String, fileHash: String): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id NOT IN (:songIds)")
    suspend fun deleteSongsNotIn(songIds: List<String>)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Transaction
    suspend fun replaceLibrary(songs: List<SongEntity>) {
        if (songs.isEmpty()) {
            deleteAllSongs()
            return
        }
        songs.forEach { upsert(it) }
        deleteSongsNotIn(songs.map { it.id })
    }

    @Query("UPDATE songs SET isFavorite = CASE WHEN isFavorite THEN 0 ELSE 1 END WHERE id = :songId")
    suspend fun toggleFavorite(songId: String)

    @Query("UPDATE songs SET downloadStatus = :status, downloadProgress = :progress, localPath = :localPath, downloadFailureReason = :failureReason WHERE id = :songId")
    suspend fun updateDownload(songId: String, status: String, progress: Float, localPath: String?, failureReason: String?)

    @Query("UPDATE songs SET downloadStatus = :status, downloadProgress = :progress, localPath = NULL, downloadFailureReason = :failureReason")
    suspend fun updateAllDownloads(status: String, progress: Float, failureReason: String?)

    @Query("UPDATE songs SET downloadStatus = :status, downloadProgress = :progress, localPath = NULL, downloadFailureReason = :failureReason WHERE downloadStatus IN (:currentStatuses)")
    suspend fun updateDownloadsWithStatuses(currentStatuses: List<String>, status: String, progress: Float, failureReason: String?)
}
