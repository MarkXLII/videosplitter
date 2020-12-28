package io.github.videosplitterapp.library.project

import android.net.Uri
import androidx.core.net.toUri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.videosplitterapp.filemanager.FileManager
import io.github.videosplitterapp.filemanager.FileMeta
import io.github.videosplitterapp.filemanager.ProjectModel
import io.github.videosplitterapp.library.localsplits.ActionModeViewModel
import io.github.videosplitterapp.splitsManager.SplitsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibraryViewModel @ViewModelInject constructor(
    private val fileManager: FileManager
) : ViewModel(), ActionModeViewModel<ProjectModel> {

    private var fileMeta: FileMeta? = null
    private var state: SplitsManager.State? = null
    val projects = MutableLiveData<List<ProjectModel>>()

    val inEditMode = MutableLiveData<Boolean>()

    val isEmpty = Transformations.switchMap(projects) {
        return@switchMap MutableLiveData<Boolean>().apply {
            postValue(it.isEmpty())
        }
    }

    init {
        projects.value = emptyList()
    }

    fun isInEditMode(): Boolean = inEditMode.value ?: false

    fun splitManagerStateUpdated(
        state: SplitsManager.State?,
        fileMeta: FileMeta?
    ) {
        this.state = state
        this.fileMeta = fileMeta
        viewModelScope.launch(Dispatchers.IO) {
            loadUnsafe(fileMeta, state)
        }
    }

    private fun loadUnsafe(
        fileMeta: FileMeta?,
        state: SplitsManager.State?
    ) {
        val newProjects = fileManager.getProjects()
        if (fileMeta != null && state == SplitsManager.State.SPLITTING) {
            newProjects.find { it.projectName == fileMeta.titleNoExt }?.busy = true
        }
        projects.postValue(newProjects)
    }

    override fun itemLongPressed(item: ProjectModel) {
        inEditMode.value = true
        selectItem(item)
    }

    fun selectItem(projectModel: ProjectModel) {
        val old = projectModel.selected.value ?: false
        projectModel.selected.value = old.not()
    }

    override fun deleteSelectedFiles(callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            getSelected().forEach {
                it.file.deleteRecursively()
            }
            loadUnsafe(fileMeta, state)
            launch(Dispatchers.Main) { callback() }
        }
    }

    private fun getSelected(): List<ProjectModel> {
        return projects.value?.filter { it.selected.value ?: false } ?: emptyList()
    }

    override fun getSelectedUris(): ArrayList<Uri> {
        val result = ArrayList<Uri>()
        getSelected().mapTo(result) { it.file.toUri() }
        return result
    }

    override fun cancelEditMode() {
        inEditMode.value = false
        projects.value?.forEach { it.selected.value = false }
    }
}