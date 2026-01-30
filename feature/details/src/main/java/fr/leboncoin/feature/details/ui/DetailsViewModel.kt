package fr.leboncoin.feature.details.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.core.data.di.IoDispatcher
import fr.leboncoin.core.data.utils.AnalyticsHelper
import fr.leboncoin.feature.details.domain.usecase.GetSongByIdUseCase
import fr.leboncoin.feature.details.domain.usecase.ToggleFavoriteUseCase
import fr.leboncoin.core.network.di.PersistentImageLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSongByIdUseCase: GetSongByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val analyticsHelper: AnalyticsHelper,
    @PersistentImageLoader val imageLoader: ImageLoader,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val songId: Int? = savedStateHandle.get<String>("songId")?.toIntOrNull()



    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Init)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    fun initLoad() {
        if (_uiState.value is DetailsUiState.Init) {
            Timber.d("DetailsViewModel: Initializing load")
            loadSong()
        }
    }

    private fun loadSong() {
        val id = songId
        if (id == null) {
            _uiState.value = DetailsUiState.Error("Invalid song ID")
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.value = DetailsUiState.Loading
            val song = getSongByIdUseCase(id)
            _uiState.value = if (song != null) {
                DetailsUiState.Success(song)
            } else {
                DetailsUiState.Error("Song not found")
            }
        }
    }

    fun onScreenViewed() {
        analyticsHelper.trackScreenView("Details")
    }

    fun toggleFavorite() {
        val id = songId ?: return
        viewModelScope.launch(ioDispatcher) {
            toggleFavoriteUseCase(id)
            loadSong()
        }
    }
}

