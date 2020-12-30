package io.github.videosplitterapp.filemanager

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.WorkerThread
import com.arthenica.mobileffmpeg.FFprobe
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.videosplitterapp.R
import io.github.videosplitterapp.ffmpeg.FFMpegUtil
import io.github.videosplitterapp.ktx.getMinSecString
import io.github.videosplitterapp.splitsManager.SliceModel
import io.github.videosplitterapp.splitsManager.SplitsManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToLong

class FileManagerImpl @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) : FileManager {

    private val cacheDir: File = applicationContext.cacheDir
    private val fileTimeDateFormatter: SimpleDateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val splitDirNames = SplitsManager.SplitType.values().associateBy { it.dirName }

    companion object {
        private val TAG = FileManagerImpl::class.java.name
        private const val READ_BUFFER_SIZE = 4 * 1024
    }

    @WorkerThread
    override fun clearCache() {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }

    @WorkerThread
    override fun checkIfValidFile(uri: Uri): String {
        Log.d(TAG, "checkIfValidFile() called with: uri = $uri")
        try {
            applicationContext.contentResolver.query(
                uri, null, null, null, null, null
            )?.use {
                if (it.moveToFirst()) {
                    val displayName: String? =
                        it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                    val size: String? = if (!it.isNull(sizeIndex)) {
                        it.getString(sizeIndex)
                    } else {
                        null
                    }
                    Log.d(TAG, "checkIfValidFile: displayName = $displayName size = $size")
                    return if (displayName.isNullOrBlank() || size.isNullOrBlank()) "" else displayName
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkIfValidFile", e)
        }
        return ""
    }

    @WorkerThread
    override fun loadFile(uri: Uri, displayName: String): FileMeta? {
        Log.d(TAG, "loadFile() called with: uri = $uri, displayName = $displayName")
        val inputStream = applicationContext.contentResolver.openInputStream(uri)
        val fileOutput = File(cacheDir, displayName)
        val outputStream = FileOutputStream(fileOutput)
        val buffer = ByteArray(READ_BUFFER_SIZE)
        var read: Int
        if (inputStream != null) {
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            inputStream.close()
            outputStream.close()
            val ffMpegInfo = FFprobe.getMediaInformation(fileOutput.path)
            val durationSec = try {
                ffMpegInfo.duration.toDouble()
            } catch (e: Exception) {
                0.0
            }
            val duration = (durationSec * TimeUnit.SECONDS.toMillis(1)).roundToLong()
            Log.d(TAG, "duration: $duration")
            if (ffMpegInfo != null && duration > 0.0) {
                return FileMeta(
                    sourceFile = fileOutput,
                    duration = duration
                )
            }
        }
        return null
    }

    @WorkerThread
    override fun createSplitDirs(splitType: SplitsManager.SplitType, meta: FileMeta): String {
        Log.d(TAG, "createDirs() called with: splitType = $splitType, meta = $meta")
        val rootOutputDir: File = appStorageRoot
        val splitsDir = generatePath(
            rootOutputDir.absolutePath,
            meta.titleNoExt,
            splitType.dirName
        )
        Log.d(TAG, "createDirs: splitsDir = $splitsDir")
        val dir = File(splitsDir)
        dir.mkdirs()
        return if (dir.exists()) dir.absolutePath else ""
    }

    override fun generatePath(vararg parts: String): String {
        return parts.joinToString(separator = File.separator)
    }

    @WorkerThread
    override fun getProjects(): List<ProjectModel> {
        Log.d(TAG, "getProjects() called")
        val rootOutputDir: File = appStorageRoot
        val result = ArrayList<ProjectModel>()
        if (rootOutputDir.isDirectory) {
            val files = rootOutputDir.listFiles()
            files?.forEach {
                if (it.exists() && it.isDirectory) {
                    result.add(
                        ProjectModel(
                            projectName = it.name,
                            lastModifiedTime = fileTimeToString(it.lastModified()),
                            file = it
                        )
                    )
                }
            }
        }
        return result
    }

    @WorkerThread
    override fun getProjectSplits(projectName: String): List<ProjectSplitModel> {
        Log.d(TAG, "getProjectSplits() called with: projectName = $projectName")
        val rootOutputDir: File = appStorageRoot
        val path = generatePath(rootOutputDir.absolutePath, projectName)
        val projectDir = File(path)
        if (projectDir.exists().not()) return emptyList()

        val result = ArrayList<ProjectSplitModel>()
        val acceptableNames = SplitsManager.SplitType.values().map { it.dirName }.toHashSet()
        val files = projectDir.listFiles { _, name ->
            acceptableNames.contains(name)
        }
        files?.forEach {
            if (it.exists() && it.isDirectory) {
                result.add(
                    ProjectSplitModel(
                        projectName = projectName,
                        splitType = splitDirNames[it.name]
                            ?: error("Unknown dir type ${it.name}"),
                        lastModifiedTime = fileTimeToString(it.lastModified()),
                        file = it
                    )
                )
            }
        }
        return result
    }

    @WorkerThread
    override fun getProjectSlices(
        projectName: String,
        splitType: SplitsManager.SplitType
    ): List<SliceModel> {
        Log.d(
            TAG,
            "getProjectSlices() called with: " +
                    "projectName = $projectName, " +
                    "splitType = $splitType"
        )
        val rootOutputDir: File = appStorageRoot
        val path = generatePath(rootOutputDir.absolutePath, projectName, splitType.dirName)
        val slicesDir = File(path)
        if (slicesDir.exists().not()) return emptyList()

        val result = ArrayList<SliceModel>()
        slicesDir.listFiles()?.sortedBy { it.lastModified() }?.forEach {
            if (it.exists() && it.isFile) {
                val durationSec = try {
                    FFprobe.getMediaInformation(it.absolutePath)?.duration?.toDouble() ?: 0.0
                } catch (e: Exception) {
                    0.0
                }
                val duration = (durationSec * TimeUnit.SECONDS.toMillis(1)).roundToLong()
                Log.d(TAG, "duration: $duration")
                result.add(
                    SliceModel(
                        title = it.nameWithoutExtension,
                        subtitle = "",
                        splitStart = 0L,
                        splitEnd = 0L,
                        duration = duration.getMinSecString(),
                        outputFilePath = it.absolutePath,
                        sourceFile = it
                    ).also { model ->
                        model.thumbPath.postValue(it.absolutePath)
                        model.state.postValue(FFMpegUtil.State(FFMpegUtil.Status.SUCCESS))
                    }
                )
            }
        }
        return result
    }

    @WorkerThread
    override fun delete(projectName: String, splitType: SplitsManager.SplitType) {
        Log.d(TAG, "delete() called with: projectName = $projectName, splitType = $splitType")
        val rootOutputDir: File = appStorageRoot
        val path = generatePath(rootOutputDir.absolutePath, projectName, splitType.dirName)
        val slicesDir = File(path)
        slicesDir.deleteRecursively()
    }

    @WorkerThread
    override fun migrateStorageToPublicDir() {
        val oldRootOutputDir: File =
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                ?: return
        oldRootOutputDir.listFiles()?.forEach {
            if (it.exists() && it.isDirectory) {
                val projectName: String = it.name
                Log.d(TAG, "migrateStorageToPublicDir: moving project $projectName")
                val newProjectDirPath = generatePath(appStorageRoot.absolutePath, projectName)
                val newProjectDir = File(newProjectDirPath)
                if (!newProjectDir.exists() || !newProjectDir.isDirectory) {
                    newProjectDir.mkdir()
                }
                moveProject(it, newProjectDir)
                it.delete()
            }
        }
    }

    private fun moveProject(projectPath: File?, newProjectDir: File) {
        projectPath?.listFiles()?.forEach {
            if (it.exists() && it.isDirectory) {
                val splitDirPath: String = it.name
                Log.d(TAG, "moveProject: moving splits $splitDirPath")
                val newSplitDirPath = generatePath(newProjectDir.absolutePath, splitDirPath)
                val newSplitDir = File(newSplitDirPath)
                if (!newProjectDir.exists() || !newProjectDir.isDirectory) {
                    newProjectDir.mkdir()
                }
                moveSplits(it, newSplitDir)
                it.delete()
            }
        }
    }

    private fun moveSplits(splitDirPath: File?, newSplitDir: File) {
        splitDirPath?.listFiles()?.forEach {
            if (it.exists() && it.isFile) {
                Log.d(TAG, "moveSplits: moving file ${it.name} to ${newSplitDir.absolutePath}")
                it.let { sourceFile ->
                    val destFile = File(generatePath(newSplitDir.absolutePath, sourceFile.name))
                    destFile.delete()
                    sourceFile.copyTo(destFile)
                    sourceFile.delete()
                }
            }
        }
    }

    override val appStorageRoot: File
        get() {
            val externalStoragePublicDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val appStorageRootPath = generatePath(
                externalStoragePublicDirectory.absolutePath,
                applicationContext.getString(R.string.app_name)
            )
            val appStorageRoot = File(appStorageRootPath)
            if (!appStorageRoot.exists() || !appStorageRoot.isDirectory) {
                appStorageRoot.mkdir()
            }
            return appStorageRoot
        }

    private fun fileTimeToString(time: Long): String {
        return fileTimeDateFormatter.format(Date(time))
    }
}