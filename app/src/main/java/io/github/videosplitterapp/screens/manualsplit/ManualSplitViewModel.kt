package io.github.videosplitterapp.screens.manualsplit

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.videosplitterapp.filemanager.FileMeta
import io.github.videosplitterapp.screens.manualsplit.ManualSplitOp.BreakOperation
import java.util.*

class ManualSplitViewModel : ViewModel() {

    private val undoStack = Stack<ManualSplitOp>()
    val items = ObservableArrayList<ManualSplitModel>()
    val lastSelectedItem = MutableLiveData<ManualSplitModel>()
    val undoAvailable = MutableLiveData<Boolean>()
    val undoOperation = MutableLiveData<ManualSplitOp>()

    init {
        items.clear()
        lastSelectedItem.value = null
        undoAvailable.value = false
        undoOperation.value = null
    }

    fun onItemClick(manualSplitModel: ManualSplitModel) {
        lastSelectedItem.value?.selected?.value = false
        manualSplitModel.selected.value = true
        lastSelectedItem.value = manualSplitModel
    }

    fun load(fileMeta: FileMeta) {
        val item = ManualSplitModel(0, fileMeta.duration).apply {
            durationString.value = getTimeString(this)
        }
        items.clear()
        items.add(item)
        onItemClick(item)
    }

    private fun getTimeString(item: ManualSplitModel): String {
        return String.format("%s - %s", getTimeString(item.startMs), getTimeString(item.endMs))
    }

    private fun getTimeString(duration: Long): String {
        val minutes: Long = duration / 1000 / 60
        val seconds: Long = duration / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun breakAt(position: Long) {
        val item = lastSelectedItem.value
        if (item != null) {
            val index = items.indexOf(item)
            items.remove(item)
            val newItem1 = ManualSplitModel(item.startMs, item.startMs + position).apply {
                durationString.value = getTimeString(this)
            }
            val newItem2 = ManualSplitModel(item.startMs + position, item.endMs).apply {
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

    fun undo() {
        if (undoStack.isNotEmpty()) {
            when (val lastOp = undoStack.pop()) {
                is BreakOperation -> {
                    stackUpdated()
                    val index = items.indexOf(lastOp.partOne)
                    val newItem =
                        ManualSplitModel(lastOp.partOne.startMs, lastOp.partTwo.endMs).apply {
                            durationString.value = getTimeString(this)
                        }
                    items.remove(lastOp.partOne)
                    items.remove(lastOp.partTwo)
                    items.add(index, newItem)
                    onItemClick(newItem)
                }
            }

        }
    }

    fun getSplits(): List<Pair<Long, Long>> {
        return items.map { Pair(it.startMs, it.endMs) }
    }
}