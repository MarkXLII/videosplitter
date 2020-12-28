package io.github.videosplitterapp.splitsManager

import android.net.Uri
import android.util.Log
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.videosplitterapp.SingleLiveEvent
import io.github.videosplitterapp.ffmpeg.FFMpegUtil
import io.github.videosplitterapp.filemanager.FileManager
import io.github.videosplitterapp.filemanager.FileMeta
import io.github.videosplitterapp.splitsManager.SplitsManager.SplitType
import io.github.videosplitterapp.splitsManager.SplitsManager.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class SplitsManagerImpl @ViewModelInject constructor(
    private val ffMpegUtil: FFMpegUtil,
    private val fileManager: FileManager
) : ViewModel(), SplitsManager {

    companion object {
        private val TAG = SplitsManagerImpl::class.java.name
    }

    private var currentJob: Job? = null
    private val aborted = AtomicBoolean(false)
    private val _fileMeta = MutableLiveData<FileMeta?>()
    private val _state = MutableLiveData<State>()
    private val _parseFileStatus = SingleLiveEvent<Boolean>()
    private val _slices = MutableLiveData<List<SliceModel>>()
    private val _importComplete = SingleLiveEvent<Boolean>()
    private val _libraryMigration = MutableLiveData<Boolean>()

    init {
        reset()
    }

    override fun migrateStorageToPublicDir() {
        viewModelScope.launch(Dispatchers.IO) {
            _libraryMigration.postValue(true)
            fileManager.migrateStorageToPublicDir()
            _libraryMigration.postValue(false)
        }
    }

    @UiThread
    override fun importFile(uri: Uri) {
        Log.d(TAG, "importFile() called with: uri = $uri")
        viewModelScope.launch(Dispatchers.IO) {
            _state.postValue(State.PROCESSING_VIDEO)
            val displayName = fileManager.checkIfValidFile(uri)
            if (displayName.isBlank()) {
                _parseFileStatus.postValue(true)
                resetUnsafe()
            } else {
                val fileMetaTemp = fileManager.loadFile(uri, displayName)
                if (fileMetaTemp == null) {
                    _parseFileStatus.postValue(true)
                    resetUnsafe()
                } else {
                    _fileMeta.postValue(fileMetaTemp)
                    _state.postValue(State.READY_TO_SPLIT)
                    _importComplete.postValue(true)
                }
            }
        }
    }

    override val state: LiveData<State> get() = _state
    override val parseFileStatus: SingleLiveEvent<Boolean> get() = _parseFileStatus
    override val fileMeta: LiveData<FileMeta?> get() = _fileMeta
    override val slices: LiveData<List<SliceModel>> get() = _slices
    override val importComplete: SingleLiveEvent<Boolean> get() = _importComplete
    override val libraryMigration: LiveData<Boolean> get() = _libraryMigration

    @UiThread
    override fun doThirtySecSplits() {
        Log.d(TAG, "doThirtySecSplits() called")
        val meta = _fileMeta.value
        if (meta != null) {
            currentJob = viewModelScope.launch(Dispatchers.IO) {
                _state.postValue(State.SPLITTING.ofType(SplitType.THIRTY_SEC))
                val outputDir = fileManager.createSplitDirs(SplitType.THIRTY_SEC, meta)
                if (outputDir.isBlank()) {
                    // TODO
                    _state.postValue(State.SPLITTING_ERROR.ofType(SplitType.THIRTY_SEC))
                } else {
                    Log.d(TAG, "doThirtySecSplits: createDirs() success")
                    val slices = makeSlices(
                        meta = meta,
                        sliceDurationMs = TimeUnit.SECONDS.toMillis(30),
                        outputDir = outputDir
                    )
                    _slices.postValue(slices)
                    ffMpegSplit(slices)
                    if (aborted.get().not()) {
                        _state.postValue(State.SPLITTING_DONE.ofType(SplitType.THIRTY_SEC))
                    }
                }
            }
        }
    }

    @UiThread
    override fun doCustomSplits(time: Long) {
        Log.d(TAG, "doCustomSplits() called with: time = $time")
        val meta = _fileMeta.value
        if (meta != null) {
            currentJob = viewModelScope.launch(Dispatchers.IO) {
                _state.postValue(State.SPLITTING.ofType(SplitType.CUSTOM_TIME))
                val outputDir = fileManager.createSplitDirs(SplitType.CUSTOM_TIME, meta)
                if (outputDir.isBlank()) {
                    // TODO
                    _state.postValue(State.SPLITTING_ERROR.ofType(SplitType.CUSTOM_TIME))
                } else {
                    Log.d(TAG, "doCustomSplits: createDirs() success")
                    val slices = makeSlices(
                        meta = meta,
                        sliceDurationMs = time,
                        outputDir = outputDir
                    )
                    _slices.postValue(slices)
                    ffMpegSplit(slices)
                    if (aborted.get().not()) {
                        _state.postValue(State.SPLITTING_DONE.ofType(SplitType.CUSTOM_TIME))
                    }
                }
            }
        }
    }

    @UiThread
    override fun doOneSplit(startTime: Long, endTime: Long) {
        Log.d(TAG, "doOneSplit() called with: startTime = $startTime, endTime = $endTime")
        val meta = _fileMeta.value
        if (meta != null) {
            currentJob = viewModelScope.launch(Dispatchers.IO) {
                _state.postValue(State.SPLITTING.ofType(SplitType.ONE))
                val outputDir = fileManager.createSplitDirs(SplitType.ONE, meta)
                if (outputDir.isBlank()) {
                    // TODO
                    _state.postValue(State.SPLITTING_ERROR.ofType(SplitType.ONE))
                } else {
                    Log.d(TAG, "doOneSplit: createDirs() success")
                    val slice = makeSlice(
                        meta = meta,
                        outputDir = outputDir,
                        splitStart = startTime,
                        splitEnd = endTime,
                        splitName = ""
                    )
                    val slices = listOf(slice)
                    _slices.postValue(slices)
                    ffMpegSplit(slices)
                    if (aborted.get().not()) {
                        _state.postValue(State.SPLITTING_DONE.ofType(SplitType.ONE))
                    }
                }
            }
        }
    }

    @UiThread
    override fun doManualSplits(times: List<Pair<Long, Long>>) {
        Log.d(TAG, "doManualSplits() called with: times = $times")
        val meta = _fileMeta.value
        if (meta != null) {
            currentJob = viewModelScope.launch(Dispatchers.IO) {
                _state.postValue(State.SPLITTING.ofType(SplitType.MANUAL))
                val outputDir = fileManager.createSplitDirs(SplitType.MANUAL, meta)
                if (outputDir.isBlank()) {
                    // TODO
                    _state.postValue(State.SPLITTING_ERROR.ofType(SplitType.MANUAL))
                } else {
                    Log.d(TAG, "doManualSplits: createDirs() success")
                    val slices = times.map {
                        makeSlice(
                            meta = meta,
                            outputDir = outputDir,
                            splitStart = it.first,
                            splitEnd = it.second,
                            splitName = ""
                        )
                    }
                    _slices.postValue(slices)
                    ffMpegSplit(slices)
                    if (aborted.get().not()) {
                        _state.postValue(State.SPLITTING_DONE.ofType(SplitType.MANUAL))
                    }
                }
            }
        }
    }

    @UiThread
    override fun abort() {
        Log.d(TAG, "abort() called")
        viewModelScope.launch(Dispatchers.IO) {
            aborted.set(true)
            ffMpegUtil.abort()
            currentJob?.cancelAndJoin()
            resetUnsafe()
        }
    }

    override fun startOver() {
        _state.postValue(State.READY_TO_SPLIT)
    }

    private fun reset() {
        Log.d(TAG, "reset() called")
        viewModelScope.launch(Dispatchers.IO) {
            resetUnsafe()
        }
    }

    @WorkerThread
    private fun resetUnsafe() {
        Log.d(TAG, "resetUnsafe() called")
        _state.postValue(State.IDLE)
        fileManager.clearCache()
        aborted.set(false)
    }

    @WorkerThread
    private fun makeSlices(
        meta: FileMeta,
        sliceDurationMs: Long,
        outputDir: String
    ): ArrayList<SliceModel> {
        Log.d(
            TAG,
            "makeSlices() called with: " +
                    "meta = $meta, " +
                    "sliceDurationMs = $sliceDurationMs, " +
                    "outputDir = $outputDir"
        )
        val tempItems = ArrayList<SliceModel>()
        val duration: Long = meta.duration
        val parts: Int = (duration / sliceDurationMs +
                (if (duration % sliceDurationMs == 0L) 0 else 1)).toInt()
        for (i in 0 until parts) {
            val splitName = "Split_${i + 1}"
            val splitStart = i * sliceDurationMs
            val splitEnd = if (i != (parts - 1)) splitStart + sliceDurationMs else meta.duration
            val slice = makeSlice(
                meta = meta,
                outputDir = outputDir,
                splitStart = splitStart,
                splitEnd = splitEnd,
                splitName = splitName
            )
            Log.d(TAG, "makeSlices: slice = $slice")
            tempItems.add(slice)
        }
        return tempItems
    }

    @WorkerThread
    private fun makeSlice(
        meta: FileMeta,
        outputDir: String,
        splitStart: Long,
        splitEnd: Long,
        splitName: String
    ): SliceModel {
        val splitNamePart = if (splitName.isBlank()) "" else "_${splitName}_"
        val startStr = getTimeString(splitStart)
        val endStr = getTimeString(splitEnd)
        val outputFileName = "${meta.titleNoExt}${splitNamePart}${splitStart}_${splitEnd}"
        val outputFilePath = fileManager.generatePath(
            outputDir,
            "$outputFileName${ffMpegUtil.getOutputExt()}"
        )
        return SliceModel(
            title = outputFileName,
            subtitle = splitName,
            splitStart = splitStart,
            splitEnd = splitEnd,
            duration = "$startStr - $endStr",
            outputFilePath = outputFilePath,
            sourceFile = meta.sourceFile
        )
    }

    private fun getTimeString(duration: Long): String {
        val minutes: Long = duration / 1000 / 60
        val seconds: Long = duration / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun ffMpegSplit(slices: List<SliceModel>) {
        for (it in slices) {
            if (aborted.get()) {
                break
            }
            Log.d(TAG, "ffMpegSplit: Splitting $it")
            ffMpegUtil.split(
                inputFilePath = it.sourceFile.absolutePath,
                outputFilePath = it.outputFilePath,
                startTime = it.splitStart,
                endTime = it.splitEnd,
                state = it.state
            )
            it.thumbPath.postValue(it.outputFilePath)
        }
    }

    fun removeAll(selected: List<SliceModel>) {
        val list = ArrayList(_slices.value.orEmpty())
        list.removeAll(selected)
        _slices.postValue(list)
    }
}