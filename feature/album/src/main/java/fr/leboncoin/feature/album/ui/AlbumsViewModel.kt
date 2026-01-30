package fr.leboncoin.feature.album.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.core.data.di.IoDispatcher
import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.data.domain.repo.AlbumRepository
import fr.leboncoin.core.data.utils.AnalyticsHelper
import fr.leboncoin.core.data.utils.NetworkMonitor
import fr.leboncoin.core.network.di.PersistentImageLoader
import fr.leboncoin.feature.album.domain.usecase.GetAllSongsUseCase
import fr.leboncoin.feature.album.domain.usecase.GetSavedSongsUseCase
import fr.leboncoin.feature.album.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getSavedSongsUseCase: GetSavedSongsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val repository: AlbumRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val networkMonitor: NetworkMonitor,
    @PersistentImageLoader val imageLoader: ImageLoader,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumsUiState>(AlbumsUiState.Init)
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    private val isLoading = AtomicBoolean(false)


    private var allSongs: List<Song> = emptyList()
    private var currentPage: Int = 1
    private var isOffline: Boolean = false

    companion object {
        private const val PAGE_SIZE = 30
    }

    fun initLoad() {
        if (_uiState.value is AlbumsUiState.Init) {
            fetchSongs()
            observeFavorites()
            observeNetworkStatus()
        }
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                Timber.d("Network status changed: isOnline=$isOnline, wasOffline=$isOffline")

                if (isOnline && isOffline) {
                    Timber.d("Back online, refreshing data from network")
                    isOffline = false
                    if (_uiState.value is AlbumsUiState.Success) {
                        _uiState.value = (_uiState.value as AlbumsUiState.Success).copy(isOffline = isOffline)
                    }
                } else if (!isOnline && !isOffline && allSongs.isNotEmpty()) {
                    Timber.d("Gone offline, showing offline banner")
                    isOffline = true
                    if (_uiState.value is AlbumsUiState.Success) {
                        _uiState.value = (_uiState.value as AlbumsUiState.Success).copy(isOffline = isOffline)
                    }
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.observeFavoriteIds().collectLatest { favoriteIds ->
                if (allSongs.isNotEmpty()) {
                    allSongs = allSongs.map { song ->
                        song.copy(isFavorite = song.id in favoriteIds)
                    }
                    emitCurrentPage()
                }
            }
        }
    }

    fun onScreenViewed() {
        analyticsHelper.trackScreenView("Albums")
    }

    fun trackSelection(item: Song) {
        analyticsHelper.trackSelection(item.id.toString())
    }

    fun onToggleFavorite(songId: Int) {
        viewModelScope.launch(ioDispatcher) {
            toggleFavoriteUseCase(songId)
            // Automatically update via observeFavorites()
        }
    }

    fun retry() {
        fetchSongs()
    }

    private fun fetchSongs() {
        if (!isLoading.compareAndSet(false, true)) {
            Timber.d("loadSongs already in progress, skipping")
            return
        }

        viewModelScope.launch(ioDispatcher) {
            try {
                _uiState.value = AlbumsUiState.Loading

                if (networkMonitor.isCurrentlyOnline()) {
                    try {
                        val songs = getAllSongsUseCase()
                        if (songs.isEmpty()) {
                            _uiState.value = AlbumsUiState.Empty
                        } else {
                            allSongs = songs
                            currentPage = 1
                            isOffline = false
                            emitCurrentPage()
                            syncSongsInBackground(songs)
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                        _uiState.value = AlbumsUiState.Error(e.message ?: "Unknown error")
                    }
                } else {
                    val savedSongs = getSavedSongsUseCase()
                    if (savedSongs.isEmpty()) {
                        _uiState.value = AlbumsUiState.Error("No internet connection and no saved songs")
                    } else {
                        allSongs = savedSongs
                        currentPage = 1
                        isOffline = true
                        emitCurrentPage()
                    }
                }
            } finally {
                isLoading.set(false)
            }
        }
    }

    fun previousPage() {
        if (currentPage > 1) {
            currentPage--
            emitCurrentPage()
        }
    }

    fun nextPage() {
        val totalPages = calculateTotalPages()
        if (currentPage < totalPages) {
            currentPage++
            emitCurrentPage()
        }
    }

    private fun emitCurrentPage() {
        val totalPages = calculateTotalPages()
        val startIndex = (currentPage - 1) * PAGE_SIZE
        val endIndex = minOf(startIndex + PAGE_SIZE, allSongs.size)
        val paginatedSongs = allSongs.subList(startIndex, endIndex)

        _uiState.value = AlbumsUiState.Success(
            songs = paginatedSongs,
            currentPage = currentPage,
            totalPages = totalPages,
            isOffline = isOffline
        )
    }

    private fun calculateTotalPages(): Int {
        return if (allSongs.isEmpty()) 1 else (allSongs.size + PAGE_SIZE - 1) / PAGE_SIZE
    }

    private fun syncSongsInBackground(songs: List<Song>) {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.syncSongs(songs)
                Timber.d("Synced ${songs.size} songs to database")
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync songs to database")
            }
        }
    }
}
