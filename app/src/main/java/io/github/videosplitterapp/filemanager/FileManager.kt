package io.github.videosplitterapp.filemanager

import android.net.Uri
import androidx.annotation.WorkerThread
import io.github.videosplitterapp.splitsManager.SliceModel
import io.github.videosplitterapp.splitsManager.SplitsManager

interface FileManager {

    @WorkerThread
    fun clearCache()

    @WorkerThread
    fun checkIfValidFile(uri: Uri): String

    @WorkerThread
    fun loadFile(uri: Uri, displayName: String): FileMeta?

    @WorkerThread
    fun createSplitDirs(splitType: SplitsManager.SplitType, meta: FileMeta): String

    fun generatePath(vararg parts: String): String

    @WorkerThread
    fun getProjects(): List<ProjectModel>

    @WorkerThread
    fun getProjectSplits(projectName: String): List<ProjectSplitModel>

    @WorkerThread
    fun getProjectSlices(projectName: String, splitType: SplitsManager.SplitType): List<SliceModel>

    @WorkerThread
    fun delete(projectName: String, splitType: SplitsManager.SplitType)

    @WorkerThread
    fun migrateStorageToPublicDir()
}