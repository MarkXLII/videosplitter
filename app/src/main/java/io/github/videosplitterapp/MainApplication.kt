package io.github.videosplitterapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.videosplitterapp.screens.settings.Settings
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    @Inject
    lateinit var settings: Settings

    override fun onCreate() {
        super.onCreate()
        settings.updateTheme()
    }
}