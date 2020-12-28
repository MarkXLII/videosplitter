package io.github.videosplitterapp.library.projectsplits

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.ProjectSplitsFragmentBinding
import io.github.videosplitterapp.filemanager.ProjectSplitModel
import io.github.videosplitterapp.library.ActionModeHelper
import io.github.videosplitterapp.splitsManager.SplitsManager
import io.github.videosplitterapp.splitsManager.SplitsManager.SplitType.*
import io.github.videosplitterapp.splitsManager.SplitsManagerImpl
import kotlinx.android.synthetic.main.library_fragment.*
import me.tatarka.bindingcollectionadapter2.ItemBinding

@AndroidEntryPoint
class ProjectSplitsFragment :
    Fragment(),
    ProjectSplitsViewInteraction {

    private val projectSplitsViewModel: ProjectSplitsViewModel by viewModels()
    private val splitsManager: SplitsManagerImpl by activityViewModels()

    private val itemBinding =
        ItemBinding.of<ProjectSplitModel>(BR.item, R.layout.item_view_project_split)
            .bindExtra(BR.viewInteraction, this)

    private val actionModeHelper by lazy {
        ActionModeHelper(this, projectSplitsViewModel, R.menu.dir_menu)
    }

    companion object {
        private val TAG = ProjectSplitsFragment::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = ProjectSplitsFragmentArgs.fromBundle(requireArguments())
        splitsManager.fileMeta.observe(this, Observer {
            projectSplitsViewModel.load(
                projectName = args.projectName,
                state = splitsManager.state.value,
                fileMeta = it
            )
        })
        splitsManager.state.observe(this, Observer {
            projectSplitsViewModel.load(
                projectName = args.projectName,
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
        return ProjectSplitsFragmentBinding.inflate(inflater, container, false).also {
            it.itemBinding = itemBinding
            it.viewModel = projectSplitsViewModel
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

    override fun onItemClicked(item: ProjectSplitModel) {
        Log.d(TAG, "onItemClicked() called with: item = $item")
        if (projectSplitsViewModel.isInEditMode()) {
            if (item.busy) {
                Snackbar.make(
                    requireView(),
                    "Cannot select this item right now",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                projectSplitsViewModel.selectItem(item)
            }
        } else {
            val action = if (item.busy) {
                ProjectSplitsFragmentDirections.actionProjectSplitsFragmentToSplitsFragment()
            } else {
                ProjectSplitsFragmentDirections.actionProjectSplitsFragmentToLocalSplitsFragment(
                    projectName = item.projectName,
                    splitType = item.splitType
                )
            }
            findNavController().navigate(action)
        }
    }

    override fun onLongClick(item: ProjectSplitModel): Boolean {
        if (item.busy) return false
        return actionModeHelper.onLongClick(item)
    }

    override fun onOptionMenuClicked(view: View, item: ProjectSplitModel) {
        onLongClick(item)
    }

    override val inEditMode: LiveData<Boolean>
        get() = projectSplitsViewModel.inEditMode

}

@BindingAdapter(value = ["splitDirIcon"])
fun AppCompatImageView.setSplitDirIcon(type: SplitsManager.SplitType) {
    setImageResource(
        when (type) {
            THIRTY_SEC -> R.drawable.ic_30s
            CUSTOM_TIME -> R.drawable.ic_custom
            ONE -> R.drawable.ic_one_split
            MANUAL -> R.drawable.ic_manual
        }
    )
}