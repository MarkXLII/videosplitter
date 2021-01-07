package io.github.videosplitterapp.ktx

fun Long?.getDurationString(): String {
    var duration = if (this == null || this < 0) 0 else this
    val hours: Long = duration / (1000 * 60 * 60)
    duration %= (1000 * 60 * 60)
    val minutes: Long = duration / 1000 / 60
    val seconds: Long = duration / 1000 % 60
    return when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}