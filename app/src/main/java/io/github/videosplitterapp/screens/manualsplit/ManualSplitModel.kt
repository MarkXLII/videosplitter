package io.github.videosplitterapp.screens.manualsplit

import androidx.lifecycle.MutableLiveData

data class ManualSplitModel(
    val startMs: Long,
    val endMs: Long,
    val durationString: MutableLiveData<String> = MutableLiveData(),
    val selected: MutableLiveData<Boolean> = MutableLiveData()
) {
    init {
        durationString.value = "00:00 - 30:00"
        selected.value = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ManualSplitModel

        if (startMs != other.startMs) return false
        if (endMs != other.endMs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startMs.hashCode()
        result = 31 * result + endMs.hashCode()
        return result
    }


}