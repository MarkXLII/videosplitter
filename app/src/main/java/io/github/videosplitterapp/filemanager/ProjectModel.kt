package io.github.videosplitterapp.filemanager

import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import java.io.File

data class ProjectModel(
    val projectName: String,
    val lastModifiedTime: String,
    val file: File,
    var busy: Boolean = false,
    val selected: MutableLiveData<Boolean> = MutableLiveData()
) {

    init {
        selected.postValue(false)
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<ProjectModel>() {

            override fun areItemsTheSame(oldItem: ProjectModel, newItem: ProjectModel): Boolean {
                return oldItem.projectName == newItem.projectName
            }

            override fun areContentsTheSame(oldItem: ProjectModel, newItem: ProjectModel): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectModel

        if (projectName != other.projectName) return false
        if (lastModifiedTime != other.lastModifiedTime) return false
        if (file != other.file) return false
        if (busy != other.busy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = projectName.hashCode()
        result = 31 * result + lastModifiedTime.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + busy.hashCode()
        return result
    }
}