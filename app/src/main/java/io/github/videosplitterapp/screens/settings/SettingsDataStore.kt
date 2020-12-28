package io.github.videosplitterapp.screens.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceManager
import io.github.videosplitterapp.BuildConfig
import io.github.videosplitterapp.R

class SettingsDataStore(context: Context) : PreferenceDataStore(), Settings {

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private val keyDarkTheme = context.getString(R.string.key_dark_theme)
    private val keyVideoQuality = context.getString(R.string.key_video_quality)
    private val keyAudioQuality = context.getString(R.string.key_video_quality)

    private val darkModeDefault = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    private val videoQualityDefault = context.getString(R.string.video_quality_value_high)
    private val audioQualityDefault = context.getString(R.string.audio_quality_value_high)

    companion object {
        private val TAG = SettingsDataStore::class.java.name
    }

    override fun updateTheme() {
        changeThemeToDark(getBoolean(keyDarkTheme, darkModeDefault))
    }

    override fun putBoolean(key: String?, value: Boolean) {
        Log.d(TAG, "putBoolean() called with: key = $key, value = $value")
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        Log.d(TAG, "getBoolean() called with: key = $key, defValue = $defValue")
        val newDef = when (key) {
            keyDarkTheme -> darkModeDefault
            else -> defValue
        }
        val boolean = sharedPreferences.getBoolean(key, newDef)
        Log.d(TAG, "getBoolean: boolean = $boolean")
        return boolean
    }

    override fun putString(key: String?, value: String?) {
        Log.d(TAG, "putString() called with: key = $key, value = $value")
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getString(key: String?, defValue: String?): String? {
        Log.d(TAG, "getString() called with: key = $key, defValue = $defValue")
        val newDef = when (key) {
            keyVideoQuality -> videoQualityDefault
            keyAudioQuality -> audioQualityDefault
            else -> defValue
        }
        return sharedPreferences.getString(key, newDef)
    }

    override val dataStore: SettingsDataStore
        get() = this

    override fun getVideoQuality(): String {
        return getString(keyVideoQuality, videoQualityDefault) ?: videoQualityDefault
    }

    override fun getAudioQuality(): String {
        return getString(keyAudioQuality, audioQualityDefault) ?: audioQualityDefault
    }

    override fun getAppVersionString(): String {
        return BuildConfig.VERSION_NAME
    }

    private fun changeThemeToDark(dark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }
}