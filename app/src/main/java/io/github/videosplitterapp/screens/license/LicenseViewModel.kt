package io.github.videosplitterapp.screens.license

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.ViewModel
import io.github.videosplitterapp.screens.license.licenses.*

class LicenseViewModel : ViewModel() {

    val items = ObservableArrayList<LicenseItem>()

    init {
        items.add(licenseMaterialComp)
        items.add(licenseMobileFFmpeg)
        items.add(licenseBindingAdapter)
        items.add(licenseExoPlayer)
        items.add(licenseGlide)
        items.add(licenseMaterialIcons)

        items.add(licenseVideoSplitterApp)
        items.add(licenseVideoSplitterAppSource)
    }
}