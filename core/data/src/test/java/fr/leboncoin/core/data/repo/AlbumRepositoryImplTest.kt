package fr.leboncoin.core.data.repo

import app.cash.turbine.test
import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.database.AppDatabase
import fr.leboncoin.core.database.dao.FavoriteSongDao
import fr.leboncoin.core.database.dao.SongDao
import fr.leboncoin.core.database.entity.FavoriteEntity
import fr.leboncoin.core.database.entity.SongEntity
import fr.leboncoin.core.database.entity.SongWithFavoriteEntity
import fr.leboncoin.core.network.api.AlbumApiService
import fr.leboncoin.core.network.model.SongDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AlbumRepositoryImplTest {

    private lateinit var albumApiService: AlbumApiService
    private lateinit var database: AppDatabase
    private lateinit var songDao: SongDao
    private lateinit var favoriteSongDao: FavoriteSongDao

    private lateinit var repository: AlbumRepositoryImpl

    @Before
    fun setUp() {
        albumApiService = mockk()
        database = mockk()
        songDao = mockk(relaxed = true)
        favoriteSongDao = mockk(relaxed = true)

        every { database.songDao() } returns songDao
        every { database.favoriteSongDao() } returns favoriteSongDao

        repository = AlbumRepositoryImpl(albumApiService, database)
    }

    @Test
    fun `getAllSongs should fetch from api and combine with favorites`() = runTest {
        // Given
        val dtos = listOf(SongDto(1, 1, "title", "url", "thumb"))
        coEvery { albumApiService.getSongs() } returns dtos
        coEvery { favoriteSongDao.getAllFavoriteIds() } returns listOf(1)

        // When
        val result = repository.getAllSongs()

        // Then
        assertEquals(1, result.size)
        assertTrue(result.first().isFavorite)
    }

    @Test
    fun `getSavedSongs should return songs from database`() = runTest {
        // Given
        val songEntity = SongEntity(1, 1, "title", "url", "thumb")
        val songWithFavorite = SongWithFavoriteEntity(songEntity, isFavorite = true)
        coEvery { songDao.getAllSongsWithFavorites() } returns listOf(songWithFavorite)

        // When
        val result = repository.getSavedSongs()

        // Then
        assertEquals(1, result.size)
        assertTrue(result.first().isFavorite)
    }

    @Test
    fun `getSongById should return song from database`() = runTest {
        // Given
        val songEntity = SongEntity(1, 1, "title", "url", "thumb")
        val songWithFavorite = SongWithFavoriteEntity(songEntity, isFavorite = true)
        coEvery { songDao.getSongByIdWithFavorite(1) } returns songWithFavorite

        // When
        val result = repository.getSongById(1)

        // Then
        assertTrue(result != null)
        assertTrue(result!!.isFavorite)
    }

    @Test
    fun `getSongById should return null when song not in database`() = runTest {
        // Given
        coEvery { songDao.getSongByIdWithFavorite(1) } returns null

        // When
        val result = repository.getSongById(1)

        // Then
        assertNull(result)
    }

    @Test
    fun `addSongs should call dao`() = runTest {
        // Given
        val songs = listOf(Song(1, 1, "title", "url", "thumb", false))

        // When
        repository.addSongs(songs)

        // Then
        coVerify { songDao.insertAll(any()) }
    }

    @Test
    fun `deleteSongsByIds should call dao`() = runTest {
        // Given
        val ids = listOf(1)

        // When
        repository.deleteSongsByIds(ids)

        // Then
        coVerify { songDao.deleteByIds(ids) }
    }

    @Test
    fun `deleteSongsByIds should not call dao when list is empty`() = runTest {
        // Given
        val ids = emptyList<Int>()

        // When
        repository.deleteSongsByIds(ids)

        // Then
        coVerify(exactly = 0) { songDao.deleteByIds(any()) }
    }

    @Test
    fun `getSavedSongIds should return ids from dao`() = runTest {
        // Given
        val ids = listOf(1, 2, 3)
        coEvery { songDao.getAllIds() } returns ids

        // When
        val result = repository.getSavedSongIds()

        // Then
        assertEquals(ids, result)
    }

    @Test
    fun `syncSongs should only add new songs`() = runTest {
        // Given
        val songs = listOf(Song(1, 1, "t", "u", "th", false))
        coEvery { songDao.getAllIds() } returns emptyList()

        // When
        repository.syncSongs(songs)

        // Then
        coVerify { songDao.insertAll(any()) }
        coVerify(exactly = 0) { songDao.deleteByIds(any()) }
    }

    @Test
    fun `syncSongs should only remove old songs`() = runTest {
        // Given
        coEvery { songDao.getAllIds() } returns listOf(1)

        // When
        repository.syncSongs(emptyList())

        // Then
        coVerify(exactly = 0) { songDao.insertAll(any()) }
        coVerify { songDao.deleteByIds(listOf(1)) }
    }

    @Test
    fun `syncSongs should add and remove songs`() = runTest {
        // Given
        val newSongs = listOf(Song(2, 1, "t", "u", "th", false))
        coEvery { songDao.getAllIds() } returns listOf(1)

        // When
        repository.syncSongs(newSongs)

        // Then
        coVerify { songDao.insertAll(any()) }
        coVerify { songDao.deleteByIds(listOf(1)) }
    }

    @Test
    fun `addFavorite should call dao`() = runTest {
        // When
        repository.addFavorite(1)

        // Then
        coVerify { favoriteSongDao.addFavorite(FavoriteEntity(1)) }
    }

    @Test
    fun `removeFavorite should call dao`() = runTest {
        // When
        repository.removeFavorite(1)

        // Then
        coVerify { favoriteSongDao.removeFavorite(1) }
    }

    @Test
    fun `isFavorite should return value from dao`() = runTest {
        // Given
        coEvery { favoriteSongDao.isFavorite(1) } returns true

        // When
        val result = repository.isFavorite(1)

        // Then
        assertTrue(result)
    }

    @Test
    fun `toggleFavorite should add when not favorite`() = runTest {
        // Given
        coEvery { favoriteSongDao.isFavorite(1) } returns false

        // When
        repository.toggleFavorite(1)

        // Then
        coVerify { favoriteSongDao.addFavorite(FavoriteEntity(1)) }
    }

    @Test
    fun `toggleFavorite should remove when favorite`() = runTest {
        // Given
        coEvery { favoriteSongDao.isFavorite(1) } returns true

        // When
        repository.toggleFavorite(1)

        // Then
        coVerify { favoriteSongDao.removeFavorite(1) }
    }

    @Test
    fun `observeFavoriteIds should return flow from dao`() = runTest {
        // Given
        val ids = listOf(1, 2, 3)
        every { favoriteSongDao.observeFavoriteIds() } returns flowOf(ids)

        // When & Then
        repository.observeFavoriteIds().test {
            assertEquals(ids.toSet(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}