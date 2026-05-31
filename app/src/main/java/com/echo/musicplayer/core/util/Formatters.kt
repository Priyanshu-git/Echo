package com.echo.musicplayer.core.util

import java.security.MessageDigest
import java.util.Locale

fun formatDuration(durationMs: Long): String { val s = durationMs / 1000; return String.format(Locale.US, "%d:%02d", s / 60, s % 60) }
fun formatBytes(bytes: Long): String { if (bytes < 1024) return "$bytes B"; val units = listOf("KB", "MB", "GB"); var value = bytes.toDouble(); var i = -1; while (value >= 1024 && i < units.lastIndex) { value /= 1024; i++ }; return String.format(Locale.US, "%.2f %s", value, units[i]) }
object HashUtils {
    fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    fun sha256(read: (ByteArray) -> Int): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = read(buffer)
            if (count == -1) break
            digest.update(buffer, 0, count)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
