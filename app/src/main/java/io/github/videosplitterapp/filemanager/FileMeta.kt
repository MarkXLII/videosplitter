package io.github.videosplitterapp.filemanager

import java.io.File

class FileMeta(
    val sourceFile: File,
    val duration: Long
) {
    val title: String = sourceFile.name
    val titleNoExt: String = sourceFile.nameWithoutExtension

    override fun toString(): String {
        return "FileMeta(sourceFile=$sourceFile, duration=$duration, title='$title', titleNoExt='$titleNoExt')"
    }
}