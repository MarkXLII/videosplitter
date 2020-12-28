package io.github.videosplitterapp.ffmpeg

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData

interface FFMpegUtil {

    @WorkerThread
    fun split(
        inputFilePath: String,
        outputFilePath: String,
        startTime: Long,
        endTime: Long,
        state: MutableLiveData<State>
    )

    fun getOutputExt(): String

    fun abort()

    data class State(
        val status: Status,
        val progress: Int = 0,
        val total: Int = 100
    )

    enum class Status {
        IN_QUEUE,
        SUCCESS,
        IN_PROGRESS,
        FAILED
    }
}