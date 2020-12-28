package io.github.videosplitterapp.library.project

import android.view.View
import androidx.lifecycle.LiveData
import io.github.videosplitterapp.filemanager.ProjectModel

interface LibraryViewInteraction {

    fun onItemClicked(item: ProjectModel)

    fun onLongClick(item: ProjectModel): Boolean

    fun onOptionMenuClicked(view: View, item: ProjectModel)

    val inEditMode: LiveData<Boolean>

}