package io.github.videosplitterapp.screens.manualsplit

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.videosplitterapp.filemanager.FileMeta
import io.github.videosplitterapp.ktx.getDurationString
import io.github.videosplitterapp.screens.manualsplit.ManualSplitOp.BreakOperation
import io.github.videosplitterapp.screens.manualsplit.ManualSplitOp.DeleteOperation
import java.util.*

class ManualSplitViewModel : ViewModel() {

    private val listLock = Any()
    private val undoStack = Stack<ManualSplitOp>()
    private val deleteAvailable = MutableLiveData<Boolean>()
    val items = ObservableArrayList<ManualSplitModel>()
    val lastSelectedItem = MutableLiveData<ManualSplitModel>()
    val undoAvailable = MutableLiveData<Boolean>()
    val undoOperation = MutableLiveData<ManualSplitOp>()

    private val onListChangedCallback =
        object : ObservableList.OnListChangedCallback<ObservableArrayList<ManualSplitModel>>() {
            override fun onChanged(sender: ObservableArrayList<ManualSplitModel>?) {
            }

            override fun onItemRangeChanged(
                sender: ObservableArrayList<ManualSplitModel>?,
                positionStart: Int,
                itemCount: Int
            ) {
            }

            override fun onItemRangeInserted(
                sender: ObservableArrayList<ManualSplitModel>?,
                positionStart: Int,
                itemCount: Int
            ) {
                listUpdated()
            }

            override fun onItemRangeMoved(
                sender: ObservableArrayList<ManualSplitModel>?,
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) {
            }

            override fun onItemRangeRemoved(
                sender: ObservableArrayList<ManualSplitModel>?,
                positionStart: Int,
                itemCount: Int
            ) {
                listUpdated()
            }
        }

    init {
        synchronized(listLock) {
            items.clear()
        }
        items.addOnListChangedCallback(onListChangedCallback)
        lastSelectedItem.value = null
        undoAvailable.value = false
        deleteAvailable.value = false
        undoOperation.value = null
    }

    fun onItemClick(manualSplitModel: ManualSplitModel) {
        lastSelectedItem.value?.selected?.value = false
        manualSplitModel.selected.value = true
        lastSelectedItem.value = manualSplitModel
    }

    fun load(fileMeta: FileMeta) {
        val item = ManualSplitModel(0, fileMeta.duration, deleteAvailable).apply {
            durationString.value = getTimeString(this)
        }
        synchronized(listLock) {
            items.clear()
            items.add(item)
        }
        onItemClick(item)
    }

    private fun listUpdated() {
        deleteAvailable.value = items.size != 1
    }

    private fun getTimeString(item: ManualSplitModel): String {
        return String.format(
            "%s - %s",
            item.startMs.getDurationString(),
            item.endMs.getDurationString()
        )
    }

    @Synchronized
    fun breakAt(position: Long) {
        val item = lastSelectedItem.value
        if (item != null) {
            val index = items.indexOf(item)
            items.remove(item)
            val newItem1 =
                ManualSplitModel(
                    startMs = item.startMs,
                    endMs = item.startMs + position,
                    deleteAvailable = deleteAvailable
                ).apply {
                    durationString.value = getTimeString(this)
                }
            val newItem2 =
                ManualSplitModel(
                    startMs = item.startMs + position,
                    endMs = item.endMs,
                    deleteAvailable = deleteAvailable
                ).apply {
                    durationString.value = getTimeString(this)
                }
            items.add(index, newItem1)
            items.add(index + 1, newItem2)
            onItemClick(newItem2)
            addToStack(newItem1, newItem2)
        }
    }

    private fun addToStack(
        newItem1: ManualSplitModel,
        newItem2: ManualSplitModel
    ) {
        undoStack.add(BreakOperation(newItem1, newItem2))
        stackUpdated()
    }

    private fun stackUpdated() {
        val notEmpty = undoStack.isNotEmpty()
        undoAvailable.value = notEmpty
        undoOperation.value = if (notEmpty) undoStack.peek() else null
    }

    @Synchronized
    fun undo() {
        if (undoStack.isNotEmpty()) {
            when (val lastOp = undoStack.pop()) {
                is BreakOperation -> {
                    stackUpdated()
                    val index = items.indexOf(lastOp.partOne)
                    val newItem =
                        ManualSplitModel(
                            startMs = lastOp.partOne.startMs,
                            endMs = lastOp.partTwo.endMs,
                            deleteAvailable = deleteAvailable
                        ).apply {
                            durationString.value = getTimeString(this)
                        }
                    items.remove(lastOp.partOne)
                    items.remove(lastOp.partTwo)
                    items.add(index, newItem)
                    onItemClick(newItem)
                }
                is DeleteOperation -> {
                    stackUpdated()
                    with(lastOp) {
                        items.add(index, part)
                    }
                }
            }

        }
    }

    fun getSplits(): List<Pair<Long, Long>> {
        return items.map { Pair(it.startMs, it.endMs) }
    }

    @Synchronized
    fun deletePart(manualSplitModel: ManualSplitModel) {
        val index = items.indexOf(manualSplitModel)
        items.remove(manualSplitModel)
        undoStack.add(DeleteOperation(index, manualSplitModel))
        stackUpdated()
    }
}