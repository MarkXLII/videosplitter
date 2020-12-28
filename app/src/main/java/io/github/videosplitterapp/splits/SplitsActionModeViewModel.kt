package io.github.videosplitterapp.splits

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import io.github.videosplitterapp.library.localsplits.ActionModeViewModel
import io.github.videosplitterapp.splitsManager.SliceModel
import io.github.videosplitterapp.splitsManager.SplitsManagerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SplitsActionModeViewModel(
    private val splitsManager: SplitsManagerImpl
) : ActionModeViewModel<SliceModel> {

    val inEditMode = MutableLiveData<Boolean>()

    val isEmpty = Transformations.switchMap(splitsManager.slices) {
        return@switchMap MutableLiveData<Boolean>().apply {
            postValue(it.isEmpty())
        }
    }

    init {
        inEditMode.value = false
    }

    override fun itemLongPressed(item: SliceModel) {
        inEditMode.value = true
        selectItem(item)
    }

    fun selectItem(sliceModel: SliceModel) {
        val old = sliceModel.selected.value ?: false
        sliceModel.selected.value = old.not()
    }

    fun isInEditMode(): Boolean = inEditMode.value ?: false

    override fun cancelEditMode() {
        inEditMode.value = false
        splitsManager.slices.value?.forEach { it.selected.value = false }
    }

    override fun deleteSelectedFiles(callback: () -> Unit) {
        splitsManager.viewModelScope.launch(Dispatchers.IO) {
            val selected = getSelected()
            splitsManager.removeAll(selected)
            selected.forEach {
                File(it.outputFilePath).delete()
            }
            launch(Dispatchers.Main) { callback() }
        }
    }

    private fun getSelected(): List<SliceModel> {
        return splitsManager.slices.value?.filter { it.selected.value ?: false } ?: emptyList()
    }

    override fun getSelectedUris(): ArrayList<Uri> {
        val result = ArrayList<Uri>()
        getSelected().mapTo(result) { it.outputFilePath.toUri() }
        return result
    }
}