package io.github.videosplitterapp.hilt

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import io.github.videosplitterapp.ffmpeg.FFMpegUtil
import io.github.videosplitterapp.ffmpeg.FFMpegUtilImpl
import io.github.videosplitterapp.filemanager.FileManager
import io.github.videosplitterapp.filemanager.FileManagerImpl
import io.github.videosplitterapp.screens.settings.Settings
import io.github.videosplitterapp.screens.settings.SettingsImpl
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(ApplicationComponent::class)
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
}