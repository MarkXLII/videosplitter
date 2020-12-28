package io.github.videosplitterapp.screens.license

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.BR
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.LicenseListActivityBinding
import me.tatarka.bindingcollectionadapter2.ItemBinding

@AndroidEntryPoint
class LicenseListActivity : AppCompatActivity(), ViewInteraction {

    private val licenseViewModel: LicenseViewModel by viewModels()

    private val itemBinding =
        ItemBinding.of<LicenseItem>(BR.item, R.layout.item_view_license).bindExtra(
            BR.viewInteraction, this
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<LicenseListActivityBinding>(
            this,
            R.layout.license_list_activity
        )
            .also {
                it.itemBinding = itemBinding
                it.licenseViewModel = licenseViewModel
                it.lifecycleOwner = this
                setSupportActionBar(it.topAppBar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onItemClick(licenseItem: LicenseItem) {
        startActivity(LicenseActivity.newIntent(this, licenseItem))
    }
}