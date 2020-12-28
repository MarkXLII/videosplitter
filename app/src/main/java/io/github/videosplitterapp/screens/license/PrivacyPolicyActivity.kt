package io.github.videosplitterapp.screens.license

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.LicenseActivityBinding

@AndroidEntryPoint
class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val item = LicenseItem(
            name = R.string.privacy_policy,
            rawTextResId = R.raw.video_splitter_privacy_policy
        )
        DataBindingUtil.setContentView<LicenseActivityBinding>(this, R.layout.license_activity)
            .also {
                it.item = item
                it.lifecycleOwner = this
                setSupportActionBar(it.topAppBar)
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    title = getString(item.name)
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}