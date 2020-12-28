package io.github.videosplitterapp.library.localsplits

import android.net.Uri

interface ActionModeViewModel<T> {

    fun itemLongPressed(item: T)

    fun deleteSelectedFiles(callback: () -> Unit)

    fun getSelectedUris(): ArrayList<Uri>

    fun cancelEditMode()
}