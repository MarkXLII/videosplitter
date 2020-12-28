package io.github.videosplitterapp.splitsManager

import androidx.lifecycle.MutableLiveData
import io.github.videosplitterapp.ffmpeg.FFMpegUtil
import java.io.File

data class SliceModel(
    val title: String = "Title",
    val subtitle: String = "Subtitle",
    val splitStart: Long = 0,
    val splitEnd: Long = 0,
    val duration: String = "00:00 - 00:30",
    val outputFilePath: String,
    val sourceFile: File,
    val state: MutableLiveData<FFMpegUtil.State> = MutableLiveData(),
    val thumbPath: MutableLiveData<String> = MutableLiveData(),
    val selected: MutableLiveData<Boolean> = MutableLiveData()
) {

    init {
        state.postValue(FFMpegUtil.State(FFMpegUtil.Status.IN_QUEUE))
        selected.postValue(false)
    }
}