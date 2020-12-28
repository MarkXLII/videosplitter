package io.github.videosplitterapp

interface SplitDoneListener {

    fun checkProgress()

    fun startOver()

    fun newProject()
}