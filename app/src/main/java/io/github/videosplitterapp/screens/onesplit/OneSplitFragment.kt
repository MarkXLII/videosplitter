package io.github.videosplitterapp.screens.onesplit

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
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.slider.RangeSlider
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.BaseTopFragment
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.OneSplitFragmentBinding
import io.github.videosplitterapp.ktx.getDurationString
import io.github.videosplitterapp.screens.thirtysecsplit.ThirtySecSplitViewInteraction
import kotlinx.android.synthetic.main.view_one_split_main.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong


@AndroidEntryPoint
class OneSplitFragment : BaseTopFragment(), ThirtySecSplitViewInteraction {

    private lateinit var player: SimpleExoPlayer
    private val oneSplitViewModel: OneSplitViewModel by viewModels()

    companion object {
        private val TAG = OneSplitFragment::class.java.name
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return OneSplitFragmentBinding.inflate(inflater, container, false).also {
            it.oneSplitViewModel = oneSplitViewModel
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
                val max = TimeUnit.MILLISECONDS.toSeconds(it.duration)
                oneSplitViewModel.setMinMax(0, max)
                timeChooser.values = listOf(0f, max.toFloat())
                val uri = Uri.fromFile(it.sourceFile)
                // This is the MediaSource representing the media to be played.
                var videoSource: MediaSource =
                    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
                // Prepare the player with the source.
                player.prepare(videoSource)

                timeChooser.addOnChangeListener { slider, _, _ ->
                    val values = slider.values
                    oneSplitViewModel.setValues(values)
                }

                timeChooser.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: RangeSlider) {
                        Log.d(TAG, "onStartTrackingTouch() called with: slider = $slider")
                    }

                    override fun onStopTrackingTouch(slider: RangeSlider) {
                        Log.d(TAG, "onStopTrackingTouch() called with: slider = $slider")
                        val start = slider.values[0].roundToLong()
                        val end = slider.values[1].roundToLong()
                        Log.d(TAG, "onViewCreated() called with: start = $start, end = $end")
                        if (end - start > 0) {
                            // This is the MediaSource representing the media to be played.
                            player.release()
                            videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(uri)
                            player = SimpleExoPlayer.Builder(requireContext()).build()
                            playerView.player = player
                            player.prepare(videoSource)
                            val clippingMediaSource: MediaSource =
                                ClippingMediaSource(
                                    videoSource,
                                    TimeUnit.SECONDS.toMicros(start),
                                    TimeUnit.SECONDS.toMicros(end)
                                )
                            // Prepare the player with the source.
                            player.prepare(clippingMediaSource)
                        }
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

    override fun splitVideo() {
        oneSplitViewModel.selectedValues.value.let {
            if (it != null && it.size >= 2) {
                val start = it[0].toLong()
                val end = it[1].toLong()
                splitsManager.doOneSplit(
                    startTime = TimeUnit.SECONDS.toMillis(start),
                    endTime = TimeUnit.SECONDS.toMillis(end)
                )
                val action = OneSplitFragmentDirections.actionOneSplitFragmentToSplitsFragment()
                findNavController().navigate(action)
            }
        }
    }

    override fun checkProgress() {
        val action = OneSplitFragmentDirections.actionOneSplitFragmentToSplitsFragment()
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

@BindingAdapter(value = ["timeRange"])
fun TextView.setTimeRangeChooserHintText(timeRange: List<Float>?) {
    if (timeRange == null || timeRange.size < 2) return
    val start = timeRange[0].roundToLong() * 1000
    val end = timeRange[1].roundToLong() * 1000
    text = String.format(
        "Create a split from %s to %s",
        start.getDurationString(),
        end.getDurationString()
    )
}