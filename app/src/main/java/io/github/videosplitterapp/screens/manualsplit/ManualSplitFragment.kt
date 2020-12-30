package io.github.videosplitterapp.screens.manualsplit

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.BR
import io.github.videosplitterapp.BaseTopFragment
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.ManualSplitFragmentBinding
import kotlinx.android.synthetic.main.view_custom_split_main.playerView
import kotlinx.android.synthetic.main.view_manual_split_main.*
import me.tatarka.bindingcollectionadapter2.ItemBinding
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class ManualSplitFragment : BaseTopFragment(), ManualSplitViewInteraction {

    private lateinit var player: SimpleExoPlayer
    private val manualSplitViewModel: ManualSplitViewModel by viewModels()
    private val itemBinding =
        ItemBinding.of<ManualSplitModel>(BR.item, R.layout.item_view_manual_split)
            .bindExtra(BR.viewInteraction, this)

    companion object {
        private val TAG = ManualSplitFragment::class.java.name
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ManualSplitFragmentBinding.inflate(inflater, container, false).also {
            it.itemBinding = itemBinding
            it.manualSplitViewModel = manualSplitViewModel
            it.splitsManager = splitsManager
            it.viewInteraction = this
            it.lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        splits.apply {
            val layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            setLayoutManager(layoutManager)
            addItemDecoration(
                HorizontalSpaceItemDecoration(
                    resources.getDimensionPixelOffset(R.dimen.default_margin_half)
                )
            )
        }

        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = player
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            requireContext(),
            Util.getUserAgent(requireContext(), getString(R.string.app_name))
        )
        splitsManager.fileMeta.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                manualSplitViewModel.load(it)
                manualSplitViewModel.lastSelectedItem.observe(viewLifecycleOwner, Observer { item ->
                    if (item != null) {
                        val index = manualSplitViewModel.items.indexOf(item)
                        splits.smoothScrollToPosition(index)
                        player.release()
                        player = SimpleExoPlayer.Builder(requireContext()).build()
                        playerView.player = player
                        val uri = Uri.fromFile(it.sourceFile)
                        // This is the MediaSource representing the media to be played.
                        val videoSource: MediaSource =
                            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
                        val clippingMediaSource: MediaSource =
                            ClippingMediaSource(
                                videoSource,
                                TimeUnit.MILLISECONDS.toMicros(item.startMs),
                                TimeUnit.MILLISECONDS.toMicros(item.endMs)
                            )
                        // Prepare the player with the source.
                        player.prepare(clippingMediaSource)
                    }
                })
            }
        })
    }

    override fun onPause() {
        super.onPause()
        if (player.isPlaying) {
            player.playWhenReady = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
    }

    override fun onItemClick(manualSplitModel: ManualSplitModel) {
        manualSplitViewModel.onItemClick(manualSplitModel)
    }

    override fun breakAtMarker() {
        if (::player.isInitialized) {
            val position = player.currentPosition
            Log.d(TAG, "breakAtMarker() called position = $position")
            manualSplitViewModel.breakAt(position)
        }
    }

    override fun deletePart(manualSplitModel: ManualSplitModel) {
        manualSplitViewModel.deletePart(manualSplitModel)
    }

    override fun undo() {
        manualSplitViewModel.undo()
    }

    override fun splitVideo() {
        splitsManager.doManualSplits(manualSplitViewModel.getSplits())
        val action = ManualSplitFragmentDirections.actionManualSplitFragmentToSplitsFragment()
        findNavController().navigate(action)
    }

    override fun checkProgress() {
        val action = ManualSplitFragmentDirections.actionManualSplitFragmentToSplitsFragment()
        findNavController().navigate(action)
    }

    override fun startOver() {
        splitsManager.startOver()
    }

    override fun newProject() {
        splitsManager.abort()
        findNavController().navigateUp()
    }
}

@BindingAdapter(value = ["selected"])
fun View.setSelected(selected: Boolean?) {
    isSelected = (selected == true)
}

@BindingAdapter(value = ["undoOperation"])
fun TextView.setUndoOperation(undoOperation: ManualSplitOp?) {
    text = when (undoOperation) {
        is ManualSplitOp.BreakOperation -> resources.getString(R.string.undo_last_break)
        is ManualSplitOp.DeleteOperation -> resources.getString(R.string.undo_last_delete)
        else -> resources.getString(R.string.undo)
    }
}