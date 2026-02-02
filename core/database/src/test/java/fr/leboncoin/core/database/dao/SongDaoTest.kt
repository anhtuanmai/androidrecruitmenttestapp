package fr.leboncoin.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import fr.leboncoin.core.database.AppDatabase
import fr.leboncoin.core.database.entity.FavoriteEntity
import fr.leboncoin.core.database.entity.SongEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SongDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var songDao: SongDao
    private lateinit var favoriteSongDao: FavoriteSongDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        songDao = database.songDao()
        favoriteSongDao = database.favoriteSongDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetAllSongs() = runTest {
        val songs = createTestSongs(3)
        songDao.insertAll(songs)

        val result = songDao.getAllSongs()

        assertEquals(3, result.size)
        assertEquals(songs, result)
    }

    @Test
    fun insertSingleSong() = runTest {
        val song = createTestSong(1)
        songDao.insert(song)

        val result = songDao.getSongById(1)

        assertNotNull(result)
        assertEquals(song, result)
    }

    @Test
    fun insertReplacesDuplicates() = runTest {
        val original = createTestSong(1, title = "Original")
        val updated = createTestSong(1, title = "Updated")

        songDao.insert(original)
        songDao.insert(updated)

        val result = songDao.getSongById(1)

        assertEquals("Updated", result?.title)
    }

    @Test
    fun getAllIds() = runTest {
        val songs = createTestSongs(5)
        songDao.insertAll(songs)

        val ids = songDao.getAllIds()

        assertEquals(listOf(1, 2, 3, 4, 5), ids)
    }

    @Test
    fun getSongByIdReturnsNullWhenNotFound() = runTest {
        val result = songDao.getSongById(999)

        assertNull(result)
    }

    @Test
    fun deleteByIds() = runTest {
        val songs = createTestSongs(5)
        songDao.insertAll(songs)

        songDao.deleteByIds(listOf(2, 4))

        val remaining = songDao.getAllSongs()
        assertEquals(3, remaining.size)
        assertEquals(listOf(1, 3, 5), remaining.map { it.id })
    }

    @Test
    fun deleteAll() = runTest {
        val songs = createTestSongs(3)
        songDao.insertAll(songs)

        songDao.deleteAll()

        val result = songDao.getAllSongs()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getAllSongsWithFavorites_noFavorites() = runTest {
        val songs = createTestSongs(3)
        songDao.insertAll(songs)

        val result = songDao.getAllSongsWithFavorites()

        assertEquals(3, result.size)
        assertTrue(result.all { !it.isFavorite })
    }

    @Test
    fun getAllSongsWithFavorites_withFavorites() = runTest {
        val songs = createTestSongs(3)
        songDao.insertAll(songs)
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 3))

        val result = songDao.getAllSongsWithFavorites()

        assertEquals(3, result.size)
        assertTrue(result.find { it.song.id == 1 }!!.isFavorite)
        assertFalse(result.find { it.song.id == 2 }!!.isFavorite)
        assertTrue(result.find { it.song.id == 3 }!!.isFavorite)
    }

    @Test
    fun getSongByIdWithFavorite_notFavorite() = runTest {
        val song = createTestSong(1)
        songDao.insert(song)

        val result = songDao.getSongByIdWithFavorite(1)

        assertNotNull(result)
        assertEquals(song, result?.song)
        assertFalse(result!!.isFavorite)
    }

    @Test
    fun getSongByIdWithFavorite_isFavorite() = runTest {
        val song = createTestSong(1)
        songDao.insert(song)
        favoriteSongDao.addFavorite(FavoriteEntity(songId = 1))

        val result = songDao.getSongByIdWithFavorite(1)

        assertNotNull(result)
        assertTrue(result!!.isFavorite)
    }

    @Test
    fun getSongByIdWithFavorite_notFound() = runTest {
        val result = songDao.getSongByIdWithFavorite(999)

        assertNull(result)
    }

    private fun createTestSong(
        id: Int,
        albumId: Int = 1,
        title: String = "Song $id"
    ) = SongEntity(
        id = id,
        albumId = albumId,
        title = title,
        url = "https://example.com/song$id.mp3",
        thumbnailUrl = "https://example.com/thumb$id.jpg"
    )

    private fun createTestSongs(count: Int) =
        (1..count).map { createTestSong(it) }
}
