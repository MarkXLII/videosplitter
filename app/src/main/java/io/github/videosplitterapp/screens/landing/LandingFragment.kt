package io.github.videosplitterapp.screens.landing

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.LandingFragmentBinding

@AndroidEntryPoint
class LandingFragment : Fragment() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private var _binding: LandingFragmentBinding? = null
    private val binding: LandingFragmentBinding get() = _binding!!
    private val viewModel: LandingViewModel by viewModels()

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
        if (isGranted.values.all { it }) {
            allPermissionGranted()
        } else {
            permissionsDenied()
        }
    }

    private val requestOpenVideo = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) viewModel.importFile(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = LandingFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initInteractions()
        initListeners()
    }

    private fun initInteractions() {
        binding.viewIdle.rootView.setOnClickListener { viewModel.openVideo() }
        binding.viewIdle.buttonGoToLibrary.setOnClickListener { viewModel.openLibrary() }
    }

    private fun initListeners() {
        viewModel.uiState.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            when (state) {
                LandingUiState.Idle -> {
                    binding.viewIdle.root.visibility = View.VISIBLE
                    binding.viewProcessingVideo.root.visibility = View.GONE
                }
                LandingUiState.ProcessingVideo -> {
                    binding.viewIdle.root.visibility = View.GONE
                    binding.viewProcessingVideo.root.visibility = View.VISIBLE
                }
            }
        })
        viewModel.launchVideoPicker.observe(viewLifecycleOwner, Observer {
            val launch = it ?: return@Observer
            if (launch) {
                openVideo()
            }
        })
        viewModel.launchLibrary.observe(viewLifecycleOwner, Observer {
            val launch = it ?: return@Observer
            if (launch) {
                openLibrary()
            }
        })
        viewModel.launchThirtySecSplitFragment.observe(viewLifecycleOwner) {
            requireActivity().invalidateOptionsMenu()
            if (it == true) {
                val action = LandingFragmentDirections
                    .actionLandingFragmentToThirtySecSplitFragment()
                findNavController().navigate(action)
            }
        }
        viewModel.showParseError.observe(viewLifecycleOwner) {
            if (it == true) Snackbar.make(
                binding.root,
                getString(R.string.unable_to_process_file),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun openVideo() {
        checkPermissionsAndOpenVideo()
    }

    private fun openLibrary() {
        val action = LandingFragmentDirections.actionLandingFragmentToLibraryFragment()
        findNavController().navigate(action)
    }

    private fun checkPermissionsAndOpenVideo() {
        when {
            grantedAllPermissions -> {
                allPermissionGranted()
            }

            showRequestPermissionRationale -> {
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
        permissionRequest.launch(requiredPermissions)
    }

    private val grantedAllPermissions: Boolean
        get() {
            return requiredPermissions.all {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

    private val showRequestPermissionRationale: Boolean
        get() {
            return requiredPermissions.any {
                shouldShowRequestPermissionRationale(it)
            }
        }

    private fun allPermissionGranted() {
        requestOpenVideo.launch("video/*")
    }

    private fun permissionsDenied() {
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
}
