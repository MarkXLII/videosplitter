package io.github.videosplitterapp.screens.landing

/**
 * @author [Swapnil](https://github.com/MarkXLII)
 * @since 3/12/22
 */
sealed class LandingUiState {
    object Idle : LandingUiState()
    object ProcessingVideo : LandingUiState()
}
