package io.github.videosplitterapp.library.localsplits

import android.net.Uri
import androidx.core.net.toUri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.videosplitterapp.filemanager.FileManager
import io.github.videosplitterapp.splitsManager.SliceModel
import io.github.videosplitterapp.splitsManager.SplitsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class LocalSplitsViewModel @ViewModelInject constructor(
    private val fileManager: FileManager
) : ViewModel(), ActionModeViewModel<SliceModel> {

    private lateinit var splitType: SplitsManager.SplitType
    private lateinit var projectName: String
    val slices = MutableLiveData<List<SliceModel>>()

    val inEditMode = MutableLiveData<Boolean>()

    val isEmpty = Transformations.switchMap(slices) {
        return@switchMap MutableLiveData<Boolean>().apply {
            postValue(it.isEmpty())
        }
    }

    init {
        slices.value = emptyList()
        inEditMode.value = false
    }

    fun load(
        projectName: String,
        splitType: SplitsManager.SplitType
    ) {
        this.projectName = projectName
        this.splitType = splitType
        viewModelScope.launch(Dispatchers.IO) {
            loadUnsafe(projectName, splitType)
        }
    }

    private fun loadUnsafe(
        projectName: String,
        splitType: SplitsManager.SplitType
    ) {
        val slicesDisk = fileManager.getProjectSlices(projectName, splitType)
        slices.postValue(slicesDisk)
    }

    fun isInEditMode(): Boolean = inEditMode.value ?: false

    override fun itemLongPressed(item: SliceModel) {
        inEditMode.value = true
        selectItem(item)
    }

    fun selectItem(sliceModel: SliceModel) {
        val old = sliceModel.selected.value ?: false
        sliceModel.selected.value = old.not()
    }

    override fun cancelEditMode() {
        inEditMode.value = false
        slices.value?.forEach { it.selected.value = false }
    }

    override fun deleteSelectedFiles(callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            getSelected().forEach {
                File(it.outputFilePath).delete()
            }
            loadUnsafe(projectName, splitType)
            launch(Dispatchers.Main) { callback() }
        }
    }

    private fun getSelected(): List<SliceModel> {
        return slices.value?.filter { it.selected.value ?: false } ?: emptyList()
    }

    override fun getSelectedUris(): ArrayList<Uri> {
        val result = ArrayList<Uri>()
        getSelected().mapTo(result) { it.outputFilePath.toUri() }
        return result
    }
}