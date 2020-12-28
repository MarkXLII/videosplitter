package io.github.videosplitterapp.screens.manualsplit

import io.github.videosplitterapp.screens.thirtysecsplit.ThirtySecSplitViewInteraction

interface ManualSplitViewInteraction : ThirtySecSplitViewInteraction {

    fun onItemClick(manualSplitModel: ManualSplitModel)

    fun breakAtMarker()

    fun undo()
}