package io.github.videosplitterapp.screens.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.R
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var settings: Settings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = settings.dataStore
        setPreferencesFromResource(R.xml.settings_fragment, rootKey)
        val appVersion = findPreference<Preference>("app_version")
        appVersion?.summary = settings.getAppVersionString()

        val themeToggle = findPreference<SwitchPreferenceCompat>(getString(R.string.key_dark_theme))
        themeToggle?.setOnPreferenceClickListener {
            settings.updateTheme()
            return@setOnPreferenceClickListener false
        }
    }
}