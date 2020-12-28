package io.github.videosplitterapp.library

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.github.videosplitterapp.R
import io.github.videosplitterapp.library.localsplits.ActionModeViewModel

class ActionModeHelper<T>(
    private val fragment: Fragment,
    private val actionModeViewModel: ActionModeViewModel<T>,
    @MenuRes private val menuRes: Int
) : ActionMode.Callback {

    private var actionMode: ActionMode? = null

    fun onLongClick(item: T): Boolean {
        actionModeViewModel.itemLongPressed(item)
        if (actionMode == null) {
            actionMode =
                (fragment.requireActivity() as AppCompatActivity).startSupportActionMode(this)
            return true
        }
        return false
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(menuRes, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                if (actionModeViewModel.getSelectedUris().isEmpty()) {
                    Snackbar.make(
                        fragment.requireView(),
                        "No files selected",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    MaterialAlertDialogBuilder(fragment.requireContext())
                        .setTitle("Delete selected files?")
                        .setMessage(
                            "This is permanent and cannot be undone."
                        )
                        .setNegativeButton("Cancel") { _, _ ->

                        }
                        .setPositiveButton("Delete") { _, _ ->
                            actionModeViewModel.deleteSelectedFiles() {
                                actionMode?.finish()
                            }
                        }
                        .show()
                }
                true
            }
            R.id.share -> {
                if (actionModeViewModel.getSelectedUris().isEmpty()) {
                    Snackbar.make(
                        fragment.requireView(),
                        "No files selected",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    val shareIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND_MULTIPLE
                        putParcelableArrayListExtra(
                            Intent.EXTRA_STREAM,
                            actionModeViewModel.getSelectedUris()
                        )
                        type = "video/*"
                    }
                    fragment.startActivity(Intent.createChooser(shareIntent, "Share videos to.."))
                }
                true
            }
            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionModeViewModel.cancelEditMode()
        actionMode = null
    }
}