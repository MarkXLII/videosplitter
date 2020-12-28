package io.github.videosplitterapp.screens.settings

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.R
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        private const val TAG = "SettingsFragment"
    }

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

        val onQualityPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue == getString(R.string.video_quality_value_original)) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Original Quality (copy from source)")
                        .setMessage(
                            "Choosing this option would be very fast but may cause frozen video/audio at the beginning and end of the splits. Sometimes it could cause inaccurate timed splits as well!\n" +
                                    "Do not use this option if you want accurate splits."
                        )
                        .setPositiveButton("Ok") { _, _ ->

                        }
                        .setCancelable(false)
                        .show()
                }
                return@OnPreferenceChangeListener true
            }
        val videoQuality = findPreference<ListPreference>(getString(R.string.key_video_quality))
        videoQuality?.onPreferenceChangeListener = onQualityPreferenceChangeListener

        val audioQuality = findPreference<ListPreference>(getString(R.string.key_audio_quality))
        audioQuality?.onPreferenceChangeListener = onQualityPreferenceChangeListener
    }
}