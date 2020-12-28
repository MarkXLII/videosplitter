package io.github.videosplitterapp.screens.license

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.LicenseActivityBinding
import java.io.InputStream


@AndroidEntryPoint
class LicenseActivity : AppCompatActivity() {

    companion object {

        private const val KEY_ITEM = "KEY_ITEM"

        fun newIntent(context: Context, licenseItem: LicenseItem): Intent {
            return Intent(context, LicenseActivity::class.java).also {
                it.putExtra(KEY_ITEM, licenseItem)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val item: LicenseItem = intent.getParcelableExtra(KEY_ITEM) ?: return
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

@BindingAdapter("rawTextResId")
fun TextView.setFromRaw(rawTextResId: Int) {
    text = try {
        val inputStream: InputStream = resources.openRawResource(rawTextResId)
        val inputAsString = inputStream.bufferedReader().use { it.readText() }
        inputAsString
    } catch (e: Exception) {
        context.getString(R.string.generic_error)
    }
}