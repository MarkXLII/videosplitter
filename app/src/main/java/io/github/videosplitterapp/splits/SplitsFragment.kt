package io.github.videosplitterapp.splits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.BR
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.SplitsFragmentBinding
import io.github.videosplitterapp.ffmpeg.FFMpegUtil
import io.github.videosplitterapp.library.ActionModeHelper
import io.github.videosplitterapp.library.localsplits.SplitsDividerItemDecoration
import io.github.videosplitterapp.splitsManager.SliceModel
import io.github.videosplitterapp.splitsManager.SplitsManager
import kotlinx.android.synthetic.main.splits_fragment.*
import me.tatarka.bindingcollectionadapter2.ItemBinding
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SplitsFragment : Fragment(), SplitViewInteraction {

    @Inject
    lateinit var splitsManager: SplitsManager

    private val itemBinding =
        ItemBinding.of<SliceModel>(BR.item, R.layout.item_view_split)
            .bindExtra(BR.viewInteraction, this)

    private val splitsActionModeViewModel
            by lazy { SplitsActionModeViewModel(splitsManager) }

    private val actionModeHelper by lazy {
        ActionModeHelper(this, splitsActionModeViewModel, R.menu.file_menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return SplitsFragmentBinding.inflate(inflater, container, false).also {
            it.splitsActionModeViewModel = splitsActionModeViewModel
            it.splitsManager = splitsManager
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
        if (item.state.value?.status == FFMpegUtil.Status.SUCCESS) {
            if (splitsActionModeViewModel.isInEditMode()) {
                splitsActionModeViewModel.selectItem(item)
            } else {
                val action =
                    SplitsFragmentDirections.actionSplitsFragmentToPlayerActivity(item.outputFilePath)
                findNavController().navigate(action)
            }
        }
    }

    override fun onOptionMenuClicked(view: View, item: SliceModel) {
        onLongClick(item)
    }

    override val inEditMode: LiveData<Boolean>
        get() = splitsActionModeViewModel.inEditMode

    override fun onLongClick(item: SliceModel): Boolean {
        if (item.state.value?.status != FFMpegUtil.Status.SUCCESS) return false
        return actionModeHelper.onLongClick(item)
    }
}

@BindingAdapter(value = ["progress"])
fun MaterialTextView.setProgress(state: FFMpegUtil.State) {
    if (state.total != 0 && state.progress < state.total) {
        text = String.format("%d%%", (state.progress * 100f / state.total).roundToInt())
    }
}

@BindingAdapter(value = ["thumbPath"])
fun AppCompatImageView.setThumb(thumbPath: String?) {
    Glide.with(context).load(thumbPath.orEmpty()).into(this)
}