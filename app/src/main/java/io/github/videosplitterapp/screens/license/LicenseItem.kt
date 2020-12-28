package io.github.videosplitterapp.screens.license

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LicenseItem(val name: String, val text: String) : Parcelable