package io.github.videosplitterapp.filemanager

import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import io.github.videosplitterapp.splitsManager.SplitsManager
import java.io.File

data class ProjectSplitModel(
    val projectName: String,
    val splitType: SplitsManager.SplitType,
    val lastModifiedTime: String,
    val file: File,
    var busy: Boolean = false,
    val selected: MutableLiveData<Boolean> = MutableLiveData()
) {

    init {
        selected.postValue(false)
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<ProjectSplitModel>() {
            override fun areItemsTheSame(
                oldItem: ProjectSplitModel,
                newItem: ProjectSplitModel
            ): Boolean {
                return oldItem.splitType == newItem.splitType
            }

            override fun areContentsTheSame(
                oldItem: ProjectSplitModel,
                newItem: ProjectSplitModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectSplitModel

        if (projectName != other.projectName) return false
        if (splitType != other.splitType) return false
        if (lastModifiedTime != other.lastModifiedTime) return false
        if (file != other.file) return false
        if (busy != other.busy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = projectName.hashCode()
        result = 31 * result + splitType.hashCode()
        result = 31 * result + lastModifiedTime.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + busy.hashCode()
        return result
    }
}