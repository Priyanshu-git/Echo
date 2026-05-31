package com.echo.musicplayer.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.echo.musicplayer.domain.model.Song
import java.io.File

fun Song.toMediaItem(): MediaItem {
    val uri = localPath?.toPlaybackUri() ?: Uri.parse(audioUrl)
    val artworkUri = coverArtUri
        ?.takeIf { it.isNotBlank() }
        ?.toArtworkUri()
        ?: coverArtUrl
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)

    return MediaItem.Builder()
        .setUri(uri)
        .setMediaId(id)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(artworkUri)
                .build(),
        )
        .build()
}

private fun String.toPlaybackUri(): Uri = when {
    startsWith("asset://") || startsWith("content://") || startsWith("file://") -> Uri.parse(this)
    else -> Uri.fromFile(File(this))
}

private fun String.toArtworkUri(): Uri = when {
    startsWith("http://") || startsWith("https://") || startsWith("content://") || startsWith("file://") -> Uri.parse(this)
    else -> Uri.fromFile(File(this))
}
