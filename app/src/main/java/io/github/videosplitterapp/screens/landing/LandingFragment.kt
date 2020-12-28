package io.github.videosplitterapp.screens.landing

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.LandingFragmentBinding
import io.github.videosplitterapp.splitsManager.SplitsManagerImpl


class LandingFragment : Fragment(), LandingViewInteraction {

    private val splitsManager: SplitsManagerImpl by activityViewModels()

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
        if (isGranted[Manifest.permission.READ_EXTERNAL_STORAGE] == true
            && isGranted[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
        ) {
            allPermissionGranted()
        } else {
            permissionsDenied()
        }
    }

    private val requestOpenVideo = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) splitsManager.importFile(uri)
    }

    companion object {
        private val TAG = LandingFragment::class.java.name
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return LandingFragmentBinding.inflate(inflater, container, false).also {
            it.viewInteraction = this
            it.splitsManager = splitsManager
            it.lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        splitsManager.parseFileStatus.observe(viewLifecycleOwner, Observer {
            if (it == true) Snackbar.make(
                requireView(),
                "Unable to process file",
                Snackbar.LENGTH_SHORT
            ).show()
        })
        splitsManager.importComplete.observe(viewLifecycleOwner, Observer {
            requireActivity().invalidateOptionsMenu()
            if (it == true) {
                val action = LandingFragmentDirections
                    .actionLandingFragmentToThirtySecSplitFragment()
                findNavController().navigate(action)
            }
        })
    }

    override fun openVideo() {
        Log.d(TAG, "openVideo() called")
        checkPermissions()
    }

    override fun openLibrary() {
        Log.d(TAG, "openLibrary() called")
        val action = LandingFragmentDirections.actionLandingFragmentToLibraryFragment()
        findNavController().navigate(action)
    }

    private fun allPermissionGranted() {
        Log.d(TAG, "allPermissionGranted() called")
        requestOpenVideo.launch("video/*")
    }

    private fun permissionsDenied() {
        Log.d(TAG, "permissionsDenied() called")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.permission_deny_title))
            .setMessage(resources.getString(R.string.permission_deny_message))
            .setNegativeButton(resources.getString(R.string.settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts(
                    "package",
                    requireActivity().applicationContext.packageName,
                    null
                )
                startActivity(intent)
            }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
            }
            .show()
    }

    private fun checkPermissions() {
        Log.d(TAG, "checkPermissions() called")
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                allPermissionGranted()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.permission_deny_title))
                    .setMessage(resources.getString(R.string.why_permission_files))
                    .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                        requestPermissions()
                    }
                    .show()
            }

            else -> {
                requestPermissions()
            }
        }
    }

    private fun requestPermissions() {
        Log.d(TAG, "requestPermissions() called")
        permissionRequest.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }
}
