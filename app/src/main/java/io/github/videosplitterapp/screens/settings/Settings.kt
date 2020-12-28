package io.github.videosplitterapp.screens.settings

interface Settings {

    val dataStore: SettingsDataStore

    fun updateTheme()

    fun getVideoQuality(): String

    fun getAudioQuality(): String

    fun getAppVersionString(): String
}