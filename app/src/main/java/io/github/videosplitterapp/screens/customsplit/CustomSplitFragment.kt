package io.github.videosplitterapp.screens.customsplit

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.BaseTopFragment
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.CustomSplitFragmentBinding
import io.github.videosplitterapp.ktx.getDurationString
import io.github.videosplitterapp.screens.thirtysecsplit.ThirtySecSplitViewInteraction
import kotlinx.android.synthetic.main.view_custom_split_main.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong


@AndroidEntryPoint
class CustomSplitFragment : BaseTopFragment(), ThirtySecSplitViewInteraction {

    private lateinit var player: SimpleExoPlayer
    private val customSplitViewModel: CustomSplitViewModel by viewModels()

    companion object {
        private val TAG = CustomSplitFragment::class.java.name
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return CustomSplitFragmentBinding.inflate(inflater, container, false).also {
            it.customSplitViewModel = customSplitViewModel
            it.splitsManager = splitsManager
            it.viewInteraction = this
            it.lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = player
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            requireContext(),
            Util.getUserAgent(requireContext(), getString(R.string.app_name))
        )
        splitsManager.fileMeta.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                val max = TimeUnit.MILLISECONDS.toSeconds(it.duration).toFloat()
                customSplitViewModel.minValue.value = 1f
                customSplitViewModel.maxValue.value = max
                timeChooser.value = (max * 0.3f).toInt().toFloat()

                val uri = Uri.fromFile(it.sourceFile)
                // This is the MediaSource representing the media to be played.
                val videoSource: MediaSource =
                    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
                // Prepare the player with the source.
                player.prepare(videoSource)
            }
        })
        timeChooser.addOnChangeListener { _, value, _ ->
            customSplitViewModel.selectedValue.value = value
        }
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

    override fun splitVideo() {
        val time: Long = (customSplitViewModel.selectedValue.value ?: 0f).toLong()
        if (time > 0) {
            splitsManager.doCustomSplits(time = TimeUnit.SECONDS.toMillis(time))
            val action = CustomSplitFragmentDirections.actionCustomSplitFragmentToSplitsFragment()
            findNavController().navigate(action)
        }
    }

    override fun checkProgress() {
        val action = CustomSplitFragmentDirections.actionCustomSplitFragmentToSplitsFragment()
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

@BindingAdapter(value = ["time"])
fun TextView.setTimeChooserHintText(time: Float?) {
    val duration = (time?.roundToLong() ?: 0) * 1000
    text = String.format("Split every %s", duration.getDurationString())
}