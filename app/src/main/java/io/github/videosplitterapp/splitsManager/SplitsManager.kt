package io.github.videosplitterapp.splitsManager

import android.net.Uri
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import io.github.videosplitterapp.SingleLiveEvent
import io.github.videosplitterapp.filemanager.FileMeta

interface SplitsManager {

    companion object {
        private const val DIR_THIRTY_SEC = "30 sec splits"
        private const val DIR_CUSTOM_TIME = "Custom time splits"
        private const val DIR_ONE = "One splits"
        private const val DIR_MANUAL = "Manual splits"
    }

    @UiThread
    fun importFile(uri: Uri)

    val state: LiveData<State>

    val parseFileStatus: SingleLiveEvent<Boolean>

    val fileMeta: LiveData<FileMeta?>

    val slices: LiveData<List<SliceModel>>

    val importComplete: SingleLiveEvent<Boolean>

    @UiThread
    fun doThirtySecSplits()

    @UiThread
    fun doCustomSplits(time: Long)

    @UiThread
    fun doOneSplit(startTime: Long, endTime: Long)

    @UiThread
    fun doManualSplits(times: List<Pair<Long, Long>>)

    @UiThread
    fun abort()

    fun startOver()

    fun migrateStorageToPublicDir()

    enum class State(var splitType: SplitType? = null) {
        IDLE,
        PROCESSING_VIDEO,
        READY_TO_SPLIT,
        SPLITTING,
        SPLITTING_DONE,
        SPLITTING_ERROR,
    }

    enum class SplitType(val dirName: String) {
        THIRTY_SEC(dirName = DIR_THIRTY_SEC),
        CUSTOM_TIME(dirName = DIR_CUSTOM_TIME),
        ONE(dirName = DIR_ONE),
        MANUAL(dirName = DIR_MANUAL)
    }
}

fun SplitsManager.State.ofType(splitType: SplitsManager.SplitType): SplitsManager.State {
    return this.also { it.splitType = splitType }
}