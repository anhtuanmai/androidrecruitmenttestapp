package fr.leboncoin.feature.album.domain.usecase

import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.data.domain.repo.AlbumRepository
import javax.inject.Inject

class GetSavedSongsUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke(): List<Song> {
        return repository.getSavedSongs()
    }
}
