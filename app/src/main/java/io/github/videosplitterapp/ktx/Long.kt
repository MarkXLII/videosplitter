package io.github.videosplitterapp.ktx

import android.util.Log

fun Long?.getMinSecString(): String {
    val duration = if (this == null || this < 0) 0 else this
    val minutes: Long = duration / 1000 / 60
    val seconds: Long = duration / 1000 % 60
    return String.format("%02d:%02d", minutes, seconds)
}