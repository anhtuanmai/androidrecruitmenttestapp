package fr.leboncoin.feature.details.domain.usecase

import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.data.domain.repo.AlbumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetSongByIdUseCaseTest {

    private lateinit var repository: AlbumRepository
    private lateinit var useCase: GetSongByIdUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetSongByIdUseCase(repository)
    }

    @Test
    fun `invoke returns song when repository finds it`() = runTest {
        // Given
        val songId = 1
        val expectedSong = Song(
            id = songId,
            albumId = 1,
            title = "accusamus beatae ad facilis cum similique qui sunt",
            url = "https://placehold.co/600x600/92c952/white/png",
            thumbnailUrl = "https://placehold.co/150x150/92c952/white/png",
            isFavorite = false
        )
        coEvery { repository.getSongById(songId) } returns expectedSong

        // When
        val result = useCase(songId)

        // Then
        assertEquals(expectedSong, result)
        coVerify(exactly = 1) { repository.getSongById(songId) }
    }

    @Test
    fun `invoke returns null when repository does not find song`() = runTest {
        // Given
        val songId = 999
        coEvery { repository.getSongById(songId) } returns null

        // When
        val result = useCase(songId)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { repository.getSongById(songId) }
    }

    @Test
    fun `invoke returns song with isFavorite true`() = runTest {
        // Given
        val songId = 1
        val favoriteSong = Song(
            id = songId,
            albumId = 1,
            title = "accusamus beatae ad facilis cum similique qui sunt",
            url = "https://placehold.co/600x600/92c952/white/png",
            thumbnailUrl = "https://placehold.co/150x150/92c952/white/png",
            isFavorite = true
        )
        coEvery { repository.getSongById(songId) } returns favoriteSong

        // When
        val result = useCase(songId)

        // Then
        assertEquals(true, result?.isFavorite)
    }
}
