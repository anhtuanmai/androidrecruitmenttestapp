package fr.leboncoin.core.data.domain.repo

import fr.leboncoin.core.data.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    suspend fun getAllSongs(): List<Song>
    suspend fun getSavedSongs(): List<Song>
    suspend fun getSongById(songId: Int): Song?

    suspend fun addSongs(songs: List<Song>)
    suspend fun deleteSongsByIds(ids: List<Int>)
    suspend fun getSavedSongIds(): List<Int>
    suspend fun syncSongs(songs: List<Song>)

    suspend fun addFavorite(songId: Int)
    suspend fun removeFavorite(songId: Int)
    suspend fun isFavorite(songId: Int): Boolean
    suspend fun toggleFavorite(songId: Int)
    fun observeFavoriteIds(): Flow<Set<Int>>
}
