package fr.leboncoin.feature.details.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import coil3.ImageLoader
import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.data.utils.AnalyticsHelper
import fr.leboncoin.feature.details.domain.usecase.GetSongByIdUseCase
import fr.leboncoin.feature.details.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var getSongByIdUseCase: GetSongByIdUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var imageLoader: ImageLoader

    private val testSong = Song(
        id = 1,
        albumId = 99,
        title = "accusamus beatae ad facilis cum similique qui sunt",
        url = "https://placehold.co/600x600/92c952/white/png",
        thumbnailUrl = "https://placehold.co/150x150/92c952/white/png",
        isFavorite = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getSongByIdUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        analyticsHelper = mockk(relaxed = true)
        imageLoader = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(songId: String? = "1"): DetailsViewModel {
        savedStateHandle = SavedStateHandle().apply {
            songId?.let { set("songId", it) }
        }
        return DetailsViewModel(
            savedStateHandle = savedStateHandle,
            getSongByIdUseCase = getSongByIdUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            analyticsHelper = analyticsHelper,
            imageLoader = imageLoader,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `state is Success when song is found`() = runTest {
        // Given
        coEvery { getSongByIdUseCase(1) } returns testSong
        val viewModel = createViewModel()

        // When & Then
        viewModel.uiState.test {
            assertEquals(DetailsUiState.Init, awaitItem())
            viewModel.initLoad()
            assertEquals(DetailsUiState.Loading, awaitItem())
            val successState = awaitItem() as DetailsUiState.Success
            assertEquals(testSong, successState.song)
        }
    }

    @Test
    fun `state is Error when song is not found`() = runTest {
        // Given
        coEvery { getSongByIdUseCase(1) } returns null
        val viewModel = createViewModel()

        // When & Then
        viewModel.uiState.test {
            assertEquals(DetailsUiState.Init, awaitItem())
            viewModel.initLoad()
            assertEquals(DetailsUiState.Loading, awaitItem())
            val errorState = awaitItem() as DetailsUiState.Error
            assertEquals("Song not found", errorState.message)
        }
    }

    @Test
    fun `state is Error when songId is invalid`() = runTest {
        // Given
        val viewModel = createViewModel(songId = "invalid")

        // When & Then
        viewModel.uiState.test {
            assertEquals(DetailsUiState.Init, awaitItem())
            viewModel.initLoad()
            val errorState = awaitItem() as DetailsUiState.Error
            assertEquals("Invalid song ID", errorState.message)
        }
    }

    @Test
    fun `state is Error when songId is null`() = runTest {
        // Given
        val viewModel = createViewModel(songId = null)

        // When & Then
        viewModel.uiState.test {
            assertEquals(DetailsUiState.Init, awaitItem())
            viewModel.initLoad()
            val errorState = awaitItem() as DetailsUiState.Error
            assertEquals("Invalid song ID", errorState.message)
        }
    }

    @Test
    fun `onScreenViewed tracks screen view`() = runTest {
        // Given
        coEvery { getSongByIdUseCase(1) } returns testSong
        val viewModel = createViewModel()

        // When
        viewModel.onScreenViewed()

        // Then
        verify { analyticsHelper.trackScreenView("Details") }
    }

    @Test
    fun `toggleFavorite calls use case and reloads song`() = runTest {
        // Given
        coEvery { getSongByIdUseCase(1) } returns testSong
        coEvery { toggleFavoriteUseCase(1) } just runs
        val viewModel = createViewModel()

        // When & Then
        viewModel.uiState.test {
            assertEquals(DetailsUiState.Init, awaitItem())
            viewModel.initLoad()
            assertEquals(DetailsUiState.Loading, awaitItem())
            awaitItem() // consume success state
            viewModel.toggleFavorite()
            assertEquals(DetailsUiState.Loading, awaitItem())
            awaitItem() // consume the new success state
            coVerify { toggleFavoriteUseCase(1) }
            coVerify(atLeast = 2) { getSongByIdUseCase(1) } // Initial load + reload after toggle
        }
    }
}