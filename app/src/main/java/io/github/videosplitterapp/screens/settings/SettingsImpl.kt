package io.github.videosplitterapp.screens.settings

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SettingsImpl @Inject constructor(
    @ApplicationContext context: Context
) : Settings by SettingsDataStore(context)