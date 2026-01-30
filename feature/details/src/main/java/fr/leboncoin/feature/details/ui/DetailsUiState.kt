package fr.leboncoin.feature.details.ui

import fr.leboncoin.core.data.domain.model.Song

sealed interface DetailsUiState {
    data object Init : DetailsUiState
    data object Loading : DetailsUiState
    data class Success(val song: Song) : DetailsUiState
    data class Error(val message: String) : DetailsUiState
}
