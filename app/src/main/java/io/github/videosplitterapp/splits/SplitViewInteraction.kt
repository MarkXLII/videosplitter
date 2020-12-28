package io.github.videosplitterapp.splits

import android.view.View
import androidx.lifecycle.LiveData
import io.github.videosplitterapp.splitsManager.SliceModel

interface SplitViewInteraction {

    fun onItemClick(item: SliceModel)

    fun onOptionMenuClicked(view: View, item: SliceModel)

    fun onLongClick(item: SliceModel): Boolean

    val inEditMode: LiveData<Boolean>
}