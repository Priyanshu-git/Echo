package com.echo.musicplayer.playback

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EchoMediaSessionService : MediaSessionService() {
    @Inject
    lateinit var sessionHolder: PlaybackSessionHolder

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = sessionHolder.mediaSession

    override fun onDestroy() {
        sessionHolder.release()
        super.onDestroy()
    }
}
