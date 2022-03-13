package io.github.videosplitterapp.screens.landing

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.videosplitterapp.SingleLiveEvent
import io.github.videosplitterapp.splitsManager.SplitsManager
import io.github.videosplitterapp.splitsManager.SplitsManager.State.*
import javax.inject.Inject

/**
 * @author [Swapnil](https://github.com/MarkXLII)
 * @since 3/12/22
 */
@HiltViewModel
class LandingViewModel @Inject constructor(
    private val splitsManager: SplitsManager
) : ViewModel() {

    private val _launchVideoPicker = SingleLiveEvent<Boolean>()
    val launchVideoPicker: LiveData<Boolean> get() = _launchVideoPicker

    private val _launchLibrary = SingleLiveEvent<Boolean>()
    val launchLibrary: LiveData<Boolean> get() = _launchLibrary

    val launchThirtySecSplitFragment: LiveData<Boolean> =
        Transformations.switchMap(splitsManager.importComplete) { importComplete ->
            return@switchMap SingleLiveEvent<Boolean>().also {
                it.value = importComplete
            }
        }

    val showParseError: LiveData<Boolean> =
        Transformations.switchMap(splitsManager.parseFileStatus) { parseError ->
            return@switchMap SingleLiveEvent<Boolean>().also {
                it.value = parseError
            }
        }

    val uiState: LiveData<LandingUiState> =
        Transformations.switchMap(splitsManager.state) { state ->
            return@switchMap MutableLiveData<LandingUiState>().also {
                it.value = when (state) {
                    IDLE -> LandingUiState.Idle
                    PROCESSING_VIDEO -> LandingUiState.ProcessingVideo
                    else -> LandingUiState.Idle
                }
            }
        }

    fun openVideo() {
        _launchVideoPicker.value = true
    }

    fun openLibrary() {
        _launchLibrary.value = true
    }

    fun importFile(uri: Uri) {
        splitsManager.importFile(uri)
    }
}