package io.github.videosplitterapp.library.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.BR
import io.github.videosplitterapp.BaseTopFragment
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.LibraryFragmentBinding
import io.github.videosplitterapp.filemanager.ProjectModel
import io.github.videosplitterapp.library.ActionModeHelper
import kotlinx.android.synthetic.main.library_fragment.*
import me.tatarka.bindingcollectionadapter2.ItemBinding

@AndroidEntryPoint
class LibraryFragment : BaseTopFragment(),
    LibraryViewInteraction {

    private val libraryViewModel: LibraryViewModel by viewModels()

    private val itemBinding =
        ItemBinding.of<ProjectModel>(BR.item, R.layout.item_view_project)
            .bindExtra(BR.viewInteraction, this)

    private val actionModeHelper by lazy {
        ActionModeHelper(this, libraryViewModel, R.menu.dir_menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splitsManager.fileMeta.observe(this, Observer {
            libraryViewModel.splitManagerStateUpdated(
                state = splitsManager.state.value,
                fileMeta = it
            )
        })
        splitsManager.state.observe(this, Observer {
            libraryViewModel.splitManagerStateUpdated(
                state = it,
                fileMeta = splitsManager.fileMeta.value
            )
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LibraryFragmentBinding.inflate(inflater, container, false).also {
            it.itemBinding = itemBinding
            it.viewModel = libraryViewModel
            it.lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setLayoutManager(layoutManager)
            addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
        }
    }

    override fun onItemClicked(item: ProjectModel) {
        if (libraryViewModel.isInEditMode()) {
            if (item.busy) {
                Snackbar.make(
                    requireView(),
                    "Cannot select this item right now",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                libraryViewModel.selectItem(item)
            }
        } else {
            val action = LibraryFragmentDirections
                .actionLibraryFragmentToProjectSplitsFragment(
                    projectName = item.projectName
                )
            findNavController().navigate(action)
        }
    }

    override fun onLongClick(item: ProjectModel): Boolean {
        if (item.busy) return false
        return actionModeHelper.onLongClick(item)
    }

    override fun onOptionMenuClicked(view: View, item: ProjectModel) {
        onLongClick(item)
    }

    override val inEditMode: LiveData<Boolean>
        get() = libraryViewModel.inEditMode

    override fun checkProgress() {
        val action = LibraryFragmentDirections.actionLibraryFragmentToSplitsFragment()
        findNavController().navigate(action)
    }
}