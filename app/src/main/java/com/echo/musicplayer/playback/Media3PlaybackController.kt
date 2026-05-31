package com.echo.musicplayer.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.echo.musicplayer.domain.model.PlaybackState
import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.domain.repository.PlaybackController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class Media3PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionHolder: PlaybackSessionHolder,
) : PlaybackController {
    override val state: Flow<PlaybackState> = sessionHolder.state
    private var mediaController: MediaController? = null

    override suspend fun play(song: Song, queue: List<Song>) {
        val playbackQueue = queue.ifEmpty { listOf(song) }
        val startIndex = playbackQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        sessionHolder.setQueue(playbackQueue)
        controller().run {
            setMediaItems(playbackQueue.map { it.toMediaItem() }, startIndex, 0L)
            prepare()
            play()
        }
    }

    override suspend fun togglePlayPause() {
        controller().run {
            if (isPlaying) pause() else play()
        }
    }

    override suspend fun next() {
        controller().run {
            if (hasNextMediaItem()) {
                seekToNextMediaItem()
            } else if (mediaItemCount > 0) {
                seekToDefaultPosition(0)
            }
            play()
        }
    }

    override suspend fun previous() {
        controller().run {
            if (hasPreviousMediaItem()) {
                seekToPreviousMediaItem()
            } else {
                seekTo(0L)
            }
            play()
        }
    }

    override suspend fun seekTo(positionMs: Long) {
        controller().seekTo(positionMs.coerceAtLeast(0))
    }

    override suspend fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val queue = sessionHolder.currentQueue
        if (fromIndex !in queue.indices || toIndex !in queue.indices) return
        sessionHolder.setQueue(
            queue.toMutableList().apply {
                val item = removeAt(fromIndex)
                add(toIndex, item)
            },
        )
        controller().moveMediaItem(fromIndex, toIndex)
    }

    override suspend fun removeFromQueue(songId: String) {
        val queue = sessionHolder.currentQueue
        val index = queue.indexOfFirst { it.id == songId }
        if (index == -1) return
        sessionHolder.setQueue(queue.filterNot { it.id == songId })
        controller().removeMediaItem(index)
    }

    override suspend fun clearQueue() {
        val controller = controller()
        val currentIndex = controller.currentMediaItemIndex
        val currentSong = sessionHolder.currentQueue.getOrNull(currentIndex)
        sessionHolder.setQueue(currentSong?.let(::listOf).orEmpty())
        if (currentIndex >= 0 && controller.mediaItemCount > 0) {
            for (index in controller.mediaItemCount - 1 downTo 0) {
                if (index != currentIndex) controller.removeMediaItem(index)
            }
        }
    }

    private suspend fun controller(): MediaController {
        mediaController?.let { return it }
        val token = SessionToken(context, ComponentName(context, EchoMediaSessionService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        return suspendCancellableCoroutine { continuation ->
            future.addListener(
                {
                    val controller = future.get()
                    mediaController = controller
                    continuation.resume(controller)
                },
                ContextCompat.getMainExecutor(context),
            )
            continuation.invokeOnCancellation { future.cancel(true) }
        }
    }
}
