package io.github.videosplitterapp.screens.manualsplit

sealed class ManualSplitOp {

    data class BreakOperation(
        val partOne: ManualSplitModel,
        val partTwo: ManualSplitModel
    ) : ManualSplitOp()

    data class DeleteOperation(
        val index: Int,
        val part: ManualSplitModel
    ) : ManualSplitOp()
}