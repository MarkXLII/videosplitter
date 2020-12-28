package io.github.videosplitterapp.screens.license

import android.os.Parcelable
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LicenseItem(@StringRes val name: Int, @RawRes val rawTextResId: Int) : Parcelable