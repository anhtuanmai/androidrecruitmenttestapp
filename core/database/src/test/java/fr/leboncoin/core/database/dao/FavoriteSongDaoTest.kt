package fr.leboncoin.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import fr.leboncoin.core.database.AppDatabase
import fr.leboncoin.core.database.entity.FavoriteEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FavoriteSongDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var favoriteSongDao: FavoriteSongDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        favoriteSongDao = database.favoriteSongDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addAndGetAllFavorites() = runTest {
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 2))
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 3))

        val result = favoriteSongDao.getAllFavorites()

        assertEquals(3, result.size)
        assertEquals(listOf(1, 2, 3), result.map { it.songId })
    }

    @Test
    fun getAllFavoriteIds() = runTest {
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 10))
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 20))

        val ids = favoriteSongDao.getAllFavoriteIds()

        assertEquals(listOf(10, 20), ids)
    }

    @Test
    fun isFavorite_returnsTrue() = runTest {
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))

        val result = favoriteSongDao.isFavorite(1)

        assertTrue(result)
    }

    @Test
    fun isFavorite_returnsFalse() = runTest {
        val result = favoriteSongDao.isFavorite(999)

        assertFalse(result)
    }

    @Test
    fun addFavorite_replacesDuplicate() = runTest {
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))

        val result = favoriteSongDao.getAllFavorites()

        assertEquals(1, result.size)
    }

    @Test
    fun removeFavorite() = runTest {
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 2))

        favoriteSongDao.removeFavorite(1)

        val result = favoriteSongDao.getAllFavorites()
        assertEquals(1, result.size)
        assertEquals(2, result.first().songId)
    }

    @Test
    fun removeFavorite_nonExistent_noError() = runTest {
        favoriteSongDao.removeFavorite(999)

        val result = favoriteSongDao.getAllFavorites()
        assertTrue(result.isEmpty())
    }

    @Test
    fun clearAll() = runTest {
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 2))
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 3))

        favoriteSongDao.clearAll()

        val result = favoriteSongDao.getAllFavorites()
        assertTrue(result.isEmpty())
    }

    @Test
    fun observeFavoriteIds_emitsInitialValue() = runTest {
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 2))

        favoriteSongDao.observeFavoriteIds().test {
            val emission = awaitItem()
            assertEquals(listOf(1, 2), emission)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeFavoriteIds_emitsOnAdd() = runTest {
        favoriteSongDao.observeFavoriteIds().test {
            assertEquals(emptyList<Int>(), awaitItem())

            favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))
            assertEquals(listOf(1), awaitItem())

            favoriteSongDao.addFavorite(FavoriteEntity(songId = 2))
            assertEquals(listOf(1, 2), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeFavoriteIds_emitsOnRemove() = runTest {
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 2))

        favoriteSongDao.observeFavoriteIds().test {
            assertEquals(listOf(1, 2), awaitItem())

            favoriteSongDao.removeFavorite(1)
            assertEquals(listOf(2), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeFavoriteIds_emitsOnClearAll() = runTest {
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 2))

        favoriteSongDao.observeFavoriteIds().test {
            assertEquals(listOf(1, 2), awaitItem())

            favoriteSongDao.clearAll()
            assertEquals(emptyList<Int>(), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
