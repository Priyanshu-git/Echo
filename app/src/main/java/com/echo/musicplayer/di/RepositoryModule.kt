package com.echo.musicplayer.di

import com.echo.musicplayer.data.InMemoryDownloadRepository
import com.echo.musicplayer.data.InMemoryFavoritesRepository
import com.echo.musicplayer.data.InMemoryMusicLibraryRepository
import com.echo.musicplayer.data.InMemoryPlaybackController
import com.echo.musicplayer.data.InMemorySettingsRepository
import com.echo.musicplayer.data.InMemoryStorageRepository
import com.echo.musicplayer.data.InMemoryUploadRepository
import com.echo.musicplayer.domain.repository.DownloadRepository
import com.echo.musicplayer.domain.repository.FavoritesRepository
import com.echo.musicplayer.domain.repository.MusicLibraryRepository
import com.echo.musicplayer.domain.repository.PlaybackController
import com.echo.musicplayer.domain.repository.SettingsRepository
import com.echo.musicplayer.domain.repository.StorageRepository
import com.echo.musicplayer.domain.repository.UploadRepository
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
    abstract fun bindMusicLibraryRepository(repository: InMemoryMusicLibraryRepository): MusicLibraryRepository

    @Binds
    @Singleton
    abstract fun bindUploadRepository(repository: InMemoryUploadRepository): UploadRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(repository: InMemoryFavoritesRepository): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(repository: InMemoryDownloadRepository): DownloadRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(repository: InMemorySettingsRepository): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(repository: InMemoryStorageRepository): StorageRepository

    @Binds
    @Singleton
    abstract fun bindPlaybackController(controller: InMemoryPlaybackController): PlaybackController
}
