package fr.leboncoin.core.data.repo

import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.data.domain.repo.AlbumRepository
import fr.leboncoin.core.data.utils.toDomain
import fr.leboncoin.core.data.utils.toDomainList
import fr.leboncoin.core.data.utils.toEntityList
import fr.leboncoin.core.database.AppDatabase
import fr.leboncoin.core.database.entity.FavoriteEntity
import fr.leboncoin.core.network.api.AlbumApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val albumApiService: AlbumApiService,
    private val database: AppDatabase
) : AlbumRepository {

    override suspend fun getAllSongs(): List<Song> {
        val songsDto = albumApiService.getSongs()
        val favoriteIds = database.favoriteSongDao().getAllFavoriteIds().toSet()
        return songsDto.map { dto ->
            Song(
                id = dto.id,
                albumId = dto.albumId,
                title = dto.title,
                url = dto.url,
                thumbnailUrl = dto.thumbnailUrl,
                isFavorite = dto.id in favoriteIds
            )
        }
    }

    override suspend fun getSavedSongs(): List<Song> =
        database.songDao().getAllSongsWithFavorites().toDomainList()

    override suspend fun getSongById(songId: Int): Song? =
        database.songDao().getSongByIdWithFavorite(songId)?.toDomain()

    override suspend fun addSongs(songs: List<Song>) =
        database.songDao().insertAll(songs.toEntityList())

    override suspend fun deleteSongsByIds(ids: List<Int>) {
        if (ids.isNotEmpty()) {
            database.songDao().deleteByIds(ids)
        }
    }

    override suspend fun getSavedSongIds(): List<Int> =
        database.songDao().getAllIds()

    override suspend fun syncSongs(songs: List<Song>) {
        val newIds = songs.map { it.id }.toSet()
        val existingIds = getSavedSongIds().toSet()
        val toAdd = songs.filter { it.id !in existingIds }
        val toRemove = existingIds.filter { it !in newIds }
        if (toRemove.isNotEmpty()) {
            deleteSongsByIds(toRemove)
        }
        if (toAdd.isNotEmpty()) {
            addSongs(toAdd)
        }
    }

    override suspend fun addFavorite(songId: Int) =
        database.favoriteSongDao().addFavorite(FavoriteEntity(songId))

    override suspend fun removeFavorite(songId: Int) =
        database.favoriteSongDao().removeFavorite(songId)

    override suspend fun isFavorite(songId: Int): Boolean =
        database.favoriteSongDao().isFavorite(songId)

    override suspend fun toggleFavorite(songId: Int) {
        if (isFavorite(songId)) {
            removeFavorite(songId)
        } else {
            addFavorite(songId)
        }
    }

    override fun observeFavoriteIds(): Flow<Set<Int>> =
        database.favoriteSongDao().observeFavoriteIds().map { it.toSet() }
}
