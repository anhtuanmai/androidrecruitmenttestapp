package fr.leboncoin.feature.album.ui

import fr.leboncoin.core.data.domain.model.Song


sealed interface AlbumsUiState {
    data object Init : AlbumsUiState
    data object Loading : AlbumsUiState
    data class Success(
        val songs: List<Song>,
        val currentPage: Int,
        val totalPages: Int,
        val isOffline: Boolean = false
    ) : AlbumsUiState {
        val hasPrevious: Boolean get() = currentPage > 1
        val hasNext: Boolean get() = currentPage < totalPages
    }
    data class Error(val message: String) : AlbumsUiState
    data object Empty : AlbumsUiState
}
