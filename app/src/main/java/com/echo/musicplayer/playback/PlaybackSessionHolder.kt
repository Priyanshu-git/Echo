package com.echo.musicplayer.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.echo.musicplayer.domain.model.PlaybackState
import com.echo.musicplayer.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackSessionHolder @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var queue: List<Song> = emptyList()

    val player: ExoPlayer = ExoPlayer.Builder(context).build()
    val mediaSession: MediaSession = MediaSession.Builder(context, player).build()

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state

    init {
        player.addListener(
            object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    publishState()
                }
            },
        )
        scope.launch {
            while (isActive) {
                publishState()
                delay(500)
            }
        }
    }

    fun play(song: Song, songs: List<Song>) {
        queue = songs.ifEmpty { listOf(song) }
        val startIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        player.setMediaItems(queue.map(::mediaItem), startIndex, 0L)
        player.prepare()
        player.play()
        publishState()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
        publishState()
    }

    fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else if (queue.isNotEmpty()) {
            player.seekTo(0, 0L)
        }
        player.play()
        publishState()
    }

    fun previous() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(0L)
        }
        player.play()
        publishState()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0))
        publishState()
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in queue.indices || toIndex !in queue.indices) return
        val currentId = currentSong()?.id
        queue = queue.toMutableList().apply {
            val item = removeAt(fromIndex)
            add(toIndex, item)
        }
        resetQueueKeeping(currentId)
    }

    fun removeFromQueue(songId: String) {
        val currentId = currentSong()?.id
        queue = queue.filterNot { it.id == songId }
        resetQueueKeeping(currentId)
    }

    fun clearQueue() {
        queue = currentSong()?.let(::listOf).orEmpty()
        resetQueueKeeping(currentSong()?.id)
    }

    private fun resetQueueKeeping(songId: String?) {
        val index = queue.indexOfFirst { it.id == songId }.coerceAtLeast(0)
        val position = player.currentPosition
        player.setMediaItems(queue.map(::mediaItem), index, position)
        player.prepare()
        publishState()
    }

    private fun publishState() {
        _state.value = PlaybackState(
            currentSong = currentSong(),
            isPlaying = player.isPlaying,
            progressMs = player.currentPosition.coerceAtLeast(0),
            queue = queue,
        )
    }

    private fun currentSong(): Song? = queue.getOrNull(player.currentMediaItemIndex)

    private fun mediaItem(song: Song): MediaItem {
        val uri = song.localPath?.toPlaybackUri() ?: Uri.parse(song.audioUrl)
        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(song.id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .build(),
            )
            .build()
    }

    private fun String.toPlaybackUri(): Uri = when {
        startsWith("asset://") || startsWith("content://") || startsWith("file://") -> Uri.parse(this)
        else -> Uri.fromFile(File(this))
    }
}
