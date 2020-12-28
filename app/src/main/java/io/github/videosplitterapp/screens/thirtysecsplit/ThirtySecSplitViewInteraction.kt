package io.github.videosplitterapp.screens.thirtysecsplit

import io.github.videosplitterapp.CheckProgressListener
import io.github.videosplitterapp.SplitDoneListener

interface ThirtySecSplitViewInteraction : CheckProgressListener, SplitDoneListener {

    fun splitVideo()
}