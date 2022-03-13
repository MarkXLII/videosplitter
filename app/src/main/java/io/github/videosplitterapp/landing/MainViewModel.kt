package io.github.videosplitterapp.landing

import androidx.lifecycle.LiveData
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
class MainViewModel @Inject constructor(
    splitsManager: SplitsManager
) : ViewModel() {

    val showMigrationAlertDialog: LiveData<Boolean> =
        Transformations.switchMap(splitsManager.libraryMigration) { migrating ->
            return@switchMap SingleLiveEvent<Boolean>().also {
                it.value = migrating
            }
        }

    val showBottomNav: LiveData<Boolean> =
        Transformations.switchMap(splitsManager.state) { state ->
            return@switchMap SingleLiveEvent<Boolean>().also {
                it.value = when (state) {
                    IDLE -> false
                    PROCESSING_VIDEO -> false
                    READY_TO_SPLIT -> true
                    SPLITTING -> true
                    SPLITTING_DONE -> true
                    SPLITTING_ERROR -> true
                    else -> false
                }
            }
        }

    init {
        splitsManager.migrateStorageToPublicDir()
    }
}