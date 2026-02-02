package fr.leboncoin.feature.album.domain.usecase

import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.data.domain.repo.AlbumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAllSongsUseCaseTest {

    private lateinit var repository: AlbumRepository
    private lateinit var useCase: GetAllSongsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetAllSongsUseCase(repository)
    }

    @Test
    fun `invoke returns list of songs from repository`() = runTest {
        // Given
        val expectedSongs = listOf(
            Song(
                id = 1,
                albumId = 1,
                title = "Song 1",
                url = "https://example.com/1.png",
                thumbnailUrl = "https://example.com/thumb1.png",
                isFavorite = false
            ),
            Song(
                id = 2,
                albumId = 1,
                title = "Song 2",
                url = "https://example.com/2.png",
                thumbnailUrl = "https://example.com/thumb2.png",
                isFavorite = true
            )
        )
        coEvery { repository.getAllSongs() } returns expectedSongs

        // When
        val result = useCase()

        // Then
        assertEquals(expectedSongs, result)
        assertEquals(2, result.size)
        coVerify(exactly = 1) { repository.getAllSongs() }
    }

    @Test
    fun `invoke returns empty list when repository has no songs`() = runTest {
        // Given
        coEvery { repository.getAllSongs() } returns emptyList()

        // When
        val result = useCase()

        // Then
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { repository.getAllSongs() }
    }

    @Test
    fun `invoke preserves favorite status from repository`() = runTest {
        // Given
        val songs = listOf(
            Song(id = 1, albumId = 1, title = "Song 1", url = "", thumbnailUrl = "", isFavorite = true),
            Song(id = 2, albumId = 1, title = "Song 2", url = "", thumbnailUrl = "", isFavorite = false)
        )
        coEvery { repository.getAllSongs() } returns songs

        // When
        val result = useCase()

        // Then
        assertEquals(true, result[0].isFavorite)
        assertEquals(false, result[1].isFavorite)
    }
}
