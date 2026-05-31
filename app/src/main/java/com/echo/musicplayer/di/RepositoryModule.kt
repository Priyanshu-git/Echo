package com.echo.musicplayer.di

import com.echo.musicplayer.data.local.LocalDownloadRepository
import com.echo.musicplayer.data.local.LocalStorageRepository
import com.echo.musicplayer.data.local.RoomFavoritesRepository
import com.echo.musicplayer.data.remote.FirestoreMusicLibraryRepository
import com.echo.musicplayer.data.settings.DataStoreSettingsRepository
import com.echo.musicplayer.domain.repository.DownloadRepository
import com.echo.musicplayer.domain.repository.FavoritesRepository
import com.echo.musicplayer.domain.repository.MusicLibraryRepository
import com.echo.musicplayer.domain.repository.PlaybackController
import com.echo.musicplayer.domain.repository.SettingsRepository
import com.echo.musicplayer.domain.repository.StorageRepository
import com.echo.musicplayer.playback.Media3PlaybackController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMusicLibraryRepository(repository: FirestoreMusicLibraryRepository): MusicLibraryRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(repository: RoomFavoritesRepository): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(repository: LocalDownloadRepository): DownloadRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(repository: DataStoreSettingsRepository): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(repository: LocalStorageRepository): StorageRepository

    @Binds
    @Singleton
    abstract fun bindPlaybackController(controller: Media3PlaybackController): PlaybackController
}
