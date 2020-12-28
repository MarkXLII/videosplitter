package io.github.videosplitterapp.screens.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.SettingsActivityBinding

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<SettingsActivityBinding>(this, R.layout.settings_activity)
            .also {
                it.lifecycleOwner = this
                setSupportActionBar(it.topAppBar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}