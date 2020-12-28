package io.github.videosplitterapp.screens.thirtysecsplit

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.github.videosplitterapp.BaseTopFragment
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.ThirtySecSplitFragmentBinding
import kotlinx.android.synthetic.main.view_thirty_sec_split.*


class ThirtySecSplitFragment : BaseTopFragment(), ThirtySecSplitViewInteraction {

    private lateinit var player: SimpleExoPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ThirtySecSplitFragmentBinding.inflate(inflater, container, false).also {
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
                val uri = Uri.fromFile(it.sourceFile)
                // This is the MediaSource representing the media to be played.
                val videoSource: MediaSource =
                    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
                // Prepare the player with the source.
                player.prepare(videoSource)
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
        splitsManager.doThirtySecSplits()
        val action = ThirtySecSplitFragmentDirections
            .actionThirtySecSplitFragmentToSplitsFragment()
        findNavController().navigate(action)
    }

    override fun checkProgress() {
        val action = ThirtySecSplitFragmentDirections
            .actionThirtySecSplitFragmentToSplitsFragment()
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