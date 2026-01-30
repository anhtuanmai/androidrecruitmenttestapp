package fr.leboncoin.feature.details.domain.usecase

import fr.leboncoin.core.data.domain.repo.AlbumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private lateinit var repository: AlbumRepository
    private lateinit var useCase: ToggleFavoriteUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = ToggleFavoriteUseCase(repository)
    }

    @Test
    fun `invoke calls repository toggleFavorite with correct songId`() = runTest {
        // Given
        val songId = 1
        coEvery { repository.toggleFavorite(songId) } just runs

        // When
        useCase(songId)

        // Then
        coVerify(exactly = 1) { repository.toggleFavorite(songId) }
    }

    @Test
    fun `invoke calls repository toggleFavorite for different songIds`() = runTest {
        // Given
        val songId1 = 1
        val songId2 = 2
        coEvery { repository.toggleFavorite(any()) } just runs

        // When
        useCase(songId1)
        useCase(songId2)

        // Then
        coVerify(exactly = 1) { repository.toggleFavorite(songId1) }
        coVerify(exactly = 1) { repository.toggleFavorite(songId2) }
    }

    @Test
    fun `invoke can be called multiple times for same songId`() = runTest {
        // Given
        val songId = 1
        coEvery { repository.toggleFavorite(songId) } just runs

        // When
        useCase(songId)
        useCase(songId)

        // Then
        coVerify(exactly = 2) { repository.toggleFavorite(songId) }
    }
}
