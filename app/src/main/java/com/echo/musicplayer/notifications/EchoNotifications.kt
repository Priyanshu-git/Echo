package com.echo.musicplayer.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.echo.musicplayer.R
import com.echo.musicplayer.domain.model.Song

object EchoNotificationChannels {
    const val DOWNLOADS = "downloads"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            DOWNLOADS,
            "Downloads",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Download progress and results"
        }
        manager.createNotificationChannel(channel)
    }
}

object EchoDownloadNotifications {
    fun notificationId(songId: String): Int = 2_000 + (songId.hashCode() and 0x0FFFFFFF)

    fun active(
        context: Context,
        song: Song,
        progress: Float,
        indeterminate: Boolean,
        cancelIntent: PendingIntent,
    ): Notification {
        val percent = (progress * 100).toInt().coerceIn(0, 100)
        return baseBuilder(context, song)
            .setContentTitle("Downloading ${song.title}")
            .setContentText(if (indeterminate) song.artist else "$percent% complete")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, percent, indeterminate)
            .addAction(0, "Cancel", cancelIntent)
            .build()
    }

    fun complete(context: Context, song: Song): Notification = baseBuilder(context, song)
        .setContentTitle("Downloaded ${song.title}")
        .setContentText(song.artist)
        .setAutoCancel(true)
        .build()

    fun failed(context: Context, song: Song, reason: String): Notification = baseBuilder(context, song)
        .setContentTitle("Download failed")
        .setContentText("${song.title}: $reason")
        .setAutoCancel(true)
        .build()

    fun cancelled(context: Context, song: Song): Notification = baseBuilder(context, song)
        .setContentTitle("Download cancelled")
        .setContentText(song.title)
        .setAutoCancel(true)
        .build()

    fun notifyIfAllowed(context: Context, notificationId: Int, notification: Notification) {
        if (!canPostNotifications(context)) return
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun baseBuilder(context: Context, song: Song): NotificationCompat.Builder =
        NotificationCompat.Builder(context, EchoNotificationChannels.DOWNLOADS)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(launchIntent(context))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentText(song.artist)

    private fun launchIntent(context: Context): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            } ?: return null
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
}
