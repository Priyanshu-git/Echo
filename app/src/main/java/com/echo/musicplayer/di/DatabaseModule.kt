package com.echo.musicplayer.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.echo.musicplayer.data.local.EchoDatabase
import com.echo.musicplayer.data.local.SongDao
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideEchoDatabase(@ApplicationContext context: Context): EchoDatabase = Room.databaseBuilder(
        context,
        EchoDatabase::class.java,
        "echo.db",
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideSongDao(database: EchoDatabase): SongDao = database.songDao()

    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
