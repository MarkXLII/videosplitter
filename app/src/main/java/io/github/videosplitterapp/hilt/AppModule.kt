package io.github.videosplitterapp.hilt

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.videosplitterapp.ffmpeg.FFMpegUtil
import io.github.videosplitterapp.ffmpeg.FFMpegUtilImpl
import io.github.videosplitterapp.filemanager.FileManager
import io.github.videosplitterapp.filemanager.FileManagerImpl
import io.github.videosplitterapp.screens.settings.Settings
import io.github.videosplitterapp.screens.settings.SettingsImpl
import io.github.videosplitterapp.splitsManager.SplitsManager
import io.github.videosplitterapp.splitsManager.SplitsManagerImpl
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindFFMpegUtil(ffMpegUtilImpl: FFMpegUtilImpl): FFMpegUtil

    @Binds
    @Singleton
    abstract fun bindFileManager(fileManagerImpl: FileManagerImpl): FileManager

    @Binds
    @Singleton
    abstract fun bindSettings(settingsImpl: SettingsImpl): Settings

    @Binds
    @Singleton
    abstract fun bindSplitsManager(splitsManagerImpl: SplitsManagerImpl): SplitsManager
}