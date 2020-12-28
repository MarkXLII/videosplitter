package io.github.videosplitterapp.library.localsplits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.BR
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.LocalSplitsFragmentBinding
import io.github.videosplitterapp.library.ActionModeHelper
import io.github.videosplitterapp.splits.SplitViewInteraction
import io.github.videosplitterapp.splitsManager.SliceModel
import kotlinx.android.synthetic.main.splits_fragment.*
import me.tatarka.bindingcollectionadapter2.ItemBinding

@AndroidEntryPoint
class LocalSplitsFragment :
    Fragment(),
    SplitViewInteraction {

    private val localSplitsViewModel: LocalSplitsViewModel by viewModels()

    private val itemBinding =
        ItemBinding.of<SliceModel>(BR.item, R.layout.item_view_split)
            .bindExtra(BR.viewInteraction, this)

    private val actionModeHelper by lazy {
        ActionModeHelper(this, localSplitsViewModel, R.menu.file_menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = LocalSplitsFragmentArgs.fromBundle(requireArguments())
        localSplitsViewModel.load(args.projectName, args.splitType)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LocalSplitsFragmentBinding.inflate(inflater, container, false).also {
            it.localSplitsViewModel = localSplitsViewModel
            it.itemBinding = itemBinding
            it.lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setLayoutManager(layoutManager)
            addItemDecoration(SplitsDividerItemDecoration(context, layoutManager.orientation))
        }
    }

    override fun onItemClick(item: SliceModel) {
        if (localSplitsViewModel.isInEditMode()) {
            localSplitsViewModel.selectItem(item)
        } else {
            val action = LocalSplitsFragmentDirections.actionLocalSplitsFragmentToPlayerActivity(
                filePath = item.sourceFile.absolutePath
            )
            findNavController().navigate(action)
        }
    }

    override fun onOptionMenuClicked(view: View, item: SliceModel) {
        onLongClick(item)
    }

    override fun onLongClick(item: SliceModel): Boolean {
        return actionModeHelper.onLongClick(item)
    }

    override val inEditMode: LiveData<Boolean>
        get() = localSplitsViewModel.inEditMode
}