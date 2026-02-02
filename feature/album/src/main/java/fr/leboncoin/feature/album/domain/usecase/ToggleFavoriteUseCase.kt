package fr.leboncoin.feature.album.domain.usecase

import fr.leboncoin.core.data.domain.repo.AlbumRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke(songId: Int) {
        repository.toggleFavorite(songId)
    }
}
