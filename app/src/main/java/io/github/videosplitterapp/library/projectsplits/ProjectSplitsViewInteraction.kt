package io.github.videosplitterapp.library.projectsplits

import android.view.View
import androidx.lifecycle.LiveData
import io.github.videosplitterapp.filemanager.ProjectModel
import io.github.videosplitterapp.filemanager.ProjectSplitModel

interface ProjectSplitsViewInteraction {

    fun onItemClicked(item: ProjectSplitModel)

    fun onLongClick(item: ProjectSplitModel): Boolean

    fun onOptionMenuClicked(view: View, item: ProjectSplitModel)

    val inEditMode: LiveData<Boolean>
}