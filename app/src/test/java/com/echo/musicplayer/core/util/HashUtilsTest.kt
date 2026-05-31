package com.echo.musicplayer.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class HashUtilsTest {
    @Test
    fun `sha256 hashes bytes`() {
        assertEquals(
            "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
            HashUtils.sha256("hello".toByteArray()),
        )
    }

    @Test
    fun `sha256 hashes stream reader`() {
        val input = "hello".byteInputStream()
        val hash = HashUtils.sha256 { buffer -> input.read(buffer) }

        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", hash)
    }
}
