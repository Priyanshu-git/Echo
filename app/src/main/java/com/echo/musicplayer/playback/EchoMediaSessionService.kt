package com.echo.musicplayer.playback

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class EchoMediaSessionService : MediaSessionService() {
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = null
}
