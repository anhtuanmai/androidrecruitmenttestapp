package fr.leboncoin.feature.details.domain.usecase

import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.data.domain.repo.AlbumRepository
import javax.inject.Inject

class GetSongByIdUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke(songId: Int): Song? {
        return repository.getSongById(songId)
    }
}
