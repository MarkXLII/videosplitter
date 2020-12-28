package io.github.videosplitterapp.library.projectsplits

import android.net.Uri
import androidx.core.net.toUri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.videosplitterapp.filemanager.FileManager
import io.github.videosplitterapp.filemanager.FileMeta
import io.github.videosplitterapp.filemanager.ProjectSplitModel
import io.github.videosplitterapp.library.localsplits.ActionModeViewModel
import io.github.videosplitterapp.splitsManager.SplitsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProjectSplitsViewModel @ViewModelInject constructor(
    private val fileManager: FileManager
) : ViewModel(), ActionModeViewModel<ProjectSplitModel> {

    //val projectSplits = DiffObservableList(ProjectSplitModel.DIFF_UTIL)
    val projectSplits = MutableLiveData<List<ProjectSplitModel>>()

    private var projectName: String = ""
    private var state: SplitsManager.State? = null
    private var fileMeta: FileMeta? = null

    val inEditMode = MutableLiveData<Boolean>()

    fun isInEditMode(): Boolean = inEditMode.value ?: false

    val isEmpty = Transformations.switchMap(projectSplits) {
        return@switchMap MutableLiveData<Boolean>().apply {
            postValue(it.isEmpty())
        }
    }

    fun load(
        projectName: String,
        state: SplitsManager.State?,
        fileMeta: FileMeta?
    ) {
        this.projectName = projectName
        this.state = state
        this.fileMeta = fileMeta
        viewModelScope.launch(Dispatchers.Main) {
            loadUnsafe(projectName, fileMeta, state)
        }
    }

    private fun loadUnsafe(
        projectName: String,
        fileMeta: FileMeta?,
        state: SplitsManager.State?
    ) {
        val newProjectSplits = fileManager.getProjectSplits(projectName)
        if (fileMeta != null && state == SplitsManager.State.SPLITTING) {
            newProjectSplits.find {
                it.projectName == fileMeta.titleNoExt && it.splitType == state.splitType
            }?.busy = true
        }
        projectSplits.postValue(newProjectSplits)
        /*val diff = projectSplits.calculateDiff(newProjectSplits)
        projectSplits.update(newProjectSplits, diff)*/
    }

    override fun itemLongPressed(item: ProjectSplitModel) {
        inEditMode.value = true
        selectItem(item)
    }

    fun selectItem(projectSplitModel: ProjectSplitModel) {
        val old = projectSplitModel.selected.value ?: false
        projectSplitModel.selected.value = old.not()
    }

    override fun deleteSelectedFiles(callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            getSelected().forEach {
                fileManager.delete(projectName, it.splitType)
            }
            loadUnsafe(projectName, fileMeta, state)
            launch(Dispatchers.Main) { callback() }
        }
    }

    private fun getSelected(): List<ProjectSplitModel> {
        return projectSplits.value?.filter { it.selected.value ?: false } ?: emptyList()
    }

    override fun getSelectedUris(): ArrayList<Uri> {
        val result = ArrayList<Uri>()
        getSelected().mapTo(result) { it.file.toUri() }
        return result
    }

    override fun cancelEditMode() {
        inEditMode.value = false
        projectSplits.value?.forEach { it.selected.value = false }
    }
}