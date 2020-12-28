package io.github.videosplitterapp.screens.manualsplit

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.videosplitterapp.filemanager.FileMeta
import java.util.*

class ManualSplitViewModel : ViewModel() {

    val items = ObservableArrayList<ManualSplitModel>()
    val lastSelectedItem = MutableLiveData<ManualSplitModel>()
    val undoAvailable = MutableLiveData<Boolean>()
    val undoStack = Stack<Pair<ManualSplitModel, ManualSplitModel>>()

    init {
        items.clear()
        lastSelectedItem.value = null
        undoAvailable.value = false
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
        undoStack.add(Pair(newItem1, newItem2))
        stackUpdated()
    }

    private fun stackUpdated() {
        undoAvailable.value = undoStack.isNotEmpty()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val lastBreak = undoStack.pop()
            stackUpdated()
            val index = items.indexOf(lastBreak.first)
            val newItem = ManualSplitModel(lastBreak.first.startMs, lastBreak.second.endMs).apply {
                durationString.value = getTimeString(this)
            }
            items.remove(lastBreak.first)
            items.remove(lastBreak.second)
            items.add(index, newItem)
            onItemClick(newItem)
        }
    }

    fun getSplits(): List<Pair<Long, Long>> {
        return items.map { Pair(it.startMs, it.endMs) }
    }
}