package com.echo.musicplayer.playback

import com.echo.musicplayer.domain.model.PlaybackState
import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.domain.repository.PlaybackController
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Media3PlaybackController @Inject constructor(
    private val sessionHolder: PlaybackSessionHolder,
) : PlaybackController {
    override val state: Flow<PlaybackState> = sessionHolder.state

    override suspend fun play(song: Song, queue: List<Song>) {
        sessionHolder.play(song, queue.ifEmpty { listOf(song) })
    }

    override suspend fun togglePlayPause() {
        sessionHolder.togglePlayPause()
    }

    override suspend fun next() {
        sessionHolder.next()
    }

    override suspend fun previous() {
        sessionHolder.previous()
    }

    override suspend fun seekTo(positionMs: Long) {
        sessionHolder.seekTo(positionMs)
    }

    override suspend fun reorderQueue(fromIndex: Int, toIndex: Int) {
        sessionHolder.reorderQueue(fromIndex, toIndex)
    }

    override suspend fun removeFromQueue(songId: String) {
        sessionHolder.removeFromQueue(songId)
    }

    override suspend fun clearQueue() {
        sessionHolder.clearQueue()
    }
}
