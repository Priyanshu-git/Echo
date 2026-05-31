package com.echo.musicplayer.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.echo.musicplayer.domain.model.Song
import java.io.File

fun Song.toMediaItem(): MediaItem {
    val uri = localPath?.toPlaybackUri() ?: Uri.parse(audioUrl)
    return MediaItem.Builder()
        .setUri(uri)
        .setMediaId(id)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .build(),
        )
        .build()
}

private fun String.toPlaybackUri(): Uri = when {
    startsWith("asset://") || startsWith("content://") || startsWith("file://") -> Uri.parse(this)
    else -> Uri.fromFile(File(this))
}
