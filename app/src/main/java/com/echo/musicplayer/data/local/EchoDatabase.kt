package com.echo.musicplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SongEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class EchoDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}
