package fr.leboncoin.feature.album.ui

import app.cash.turbine.test
import coil3.ImageLoader
import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.data.domain.repo.AlbumRepository
import fr.leboncoin.core.data.utils.AnalyticsHelper
import fr.leboncoin.core.data.utils.NetworkMonitor
import fr.leboncoin.feature.album.domain.usecase.GetAllSongsUseCase
import fr.leboncoin.feature.album.domain.usecase.GetSavedSongsUseCase
import fr.leboncoin.feature.album.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getAllSongsUseCase: GetAllSongsUseCase
    private lateinit var getSavedSongsUseCase: GetSavedSongsUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var repository: AlbumRepository
    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var imageLoader: ImageLoader
    private lateinit var networkStatusFlow: MutableSharedFlow<Boolean>

    private val testSongs = List(35) { index ->
        Song(
            id = index,
            albumId = 1,
            title = "Song $index",
            url = "https://example.com/$index.png",
            thumbnailUrl = "https://example.com/thumb$index.png",
            isFavorite = false
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAllSongsUseCase = mockk()
        getSavedSongsUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        repository = mockk()
        analyticsHelper = mockk(relaxed = true)
        networkMonitor = mockk()
        imageLoader = mockk(relaxed = true)

        every { repository.observeFavoriteIds() } returns flowOf(emptySet())
        coEvery { repository.syncSongs(any()) } just runs
        networkStatusFlow = MutableSharedFlow(replay = 1)
        every { networkMonitor.isOnline } returns networkStatusFlow
        // Default: isCurrentlyOnline() to true for most tests unless overridden
        every { networkMonitor.isCurrentlyOnline() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AlbumsViewModel {
        return AlbumsViewModel(
            getAllSongsUseCase = getAllSongsUseCase,
            getSavedSongsUseCase = getSavedSongsUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            repository = repository,
            analyticsHelper = analyticsHelper,
            networkMonitor = networkMonitor,
            imageLoader = imageLoader,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `state is Success when online and songs are found`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs

        // When & Then
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())

            val successState = awaitItem() as AlbumsUiState.Success
            assertEquals(30, successState.songs.size)
            assertEquals(1, successState.currentPage)
            assertEquals(2, successState.totalPages)
            assertFalse(successState.isOffline)
        }
    }

    @Test
    fun `state is Empty when online and no songs`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns emptyList()

        // When & Then
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())
            assertEquals(AlbumsUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `state is Error when online and exception thrown`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } throws RuntimeException("Network error")

        // When & Then
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())

            val errorState = awaitItem() as AlbumsUiState.Error
            assertEquals("Network error", errorState.message)
        }
    }

    @Test
    fun `state is Success with offline flag when offline with saved songs`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns false
        coEvery { getSavedSongsUseCase() } returns testSongs.take(10)

        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            // Emit false for the network status flow *before* initLoad()
            // so observeNetworkStatus() picks it up correctly
            networkStatusFlow.emit(false)

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())

            val successState = awaitItem() as AlbumsUiState.Success
            assertEquals(10, successState.songs.size)
            assertTrue(successState.isOffline)
        }
    }

    @Test
    fun `state is Error when offline and no saved songs`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns false
        coEvery { getSavedSongsUseCase() } returns emptyList()

        // When & Then
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())
            assertTrue(awaitItem() is AlbumsUiState.Error)
        }
    }

    @Test
    fun `nextPage increments page and shows next items`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs

        // When & Then
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())
            awaitItem() // Initial Success page 1

            viewModel.nextPage()

            val state = awaitItem() as AlbumsUiState.Success
            assertEquals(2, state.currentPage)
            assertEquals(5, state.songs.size)
        }
    }

    @Test
    fun `previousPage decrements page`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs

        // When & Then
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())
            awaitItem() // Initial Success page 1

            viewModel.nextPage()
            awaitItem() // Page 2

            viewModel.previousPage()

            val state = awaitItem() as AlbumsUiState.Success
            assertEquals(1, state.currentPage)
            assertEquals(30, state.songs.size)
        }
    }

    @Test
    fun `previousPage does nothing on first page`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs

        // When & Then
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())
            val initialState = awaitItem() as AlbumsUiState.Success
            assertEquals(1, initialState.currentPage)

            viewModel.previousPage()

            // No new emission expected, state should remain the same
            expectNoEvents()
        }
    }

    @Test
    fun `nextPage does nothing on last page`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs

        // When & Then
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())
            awaitItem() // Page 1

            viewModel.nextPage()
            val page2 = awaitItem() as AlbumsUiState.Success
            assertEquals(2, page2.currentPage)

            viewModel.nextPage()

            // No new emission expected
            expectNoEvents()
        }
    }

    @Test
    fun `hasPrevious is false on first page`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs

        // When & Then
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())

            val state = awaitItem() as AlbumsUiState.Success
            assertFalse(state.hasPrevious)
        }
    }

    @Test
    fun `hasNext is false on last page`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs

        // When & Then
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())
            awaitItem() // Page 1

            viewModel.nextPage()

            val state = awaitItem() as AlbumsUiState.Success
            assertFalse(state.hasNext)
        }
    }

    @Test
    fun `onScreenViewed tracks analytics`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs
        val viewModel = createViewModel()

        // When
        viewModel.onScreenViewed()

        // Then
        verify { analyticsHelper.trackScreenView("Albums") }
    }

    @Test
    fun `trackSelection tracks analytics with song id`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs
        val viewModel = createViewModel()
        val song = testSongs.first()

        // When
        viewModel.trackSelection(song)

        // Then
        verify { analyticsHelper.trackSelection("0") }
    }

    @Test
    fun `onToggleFavorite calls use case`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs
        coEvery { toggleFavoriteUseCase(any()) } just runs
        val viewModel = createViewModel()

        // When
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()

            assertEquals(AlbumsUiState.Loading, awaitItem())
            awaitItem() // Success

            viewModel.onToggleFavorite(1)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            coVerify { toggleFavoriteUseCase(1) }
        }
    }

    @Test
    fun `songs sync in background after successful load`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs

        // When & Then
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())
            viewModel.initLoad()
            assertEquals(AlbumsUiState.Loading, awaitItem())
            awaitItem() // Success

            coVerify { repository.syncSongs(testSongs) }
        }
    }

    // ==================== NetworkMonitor Flow Tests ====================

    @Test
    fun `shows offline banner when network goes offline`() = runTest {
        // Given
        val networkFlow = MutableSharedFlow<Boolean>()
        every { networkMonitor.isOnline } returns networkFlow
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs

        val viewModel = createViewModel()

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()
            assertEquals(AlbumsUiState.Loading, awaitItem())

            val onlineState = awaitItem() as AlbumsUiState.Success
            assertFalse(onlineState.isOffline)

            // Simulate network going offline
            networkFlow.emit(false)
            testDispatcher.scheduler.advanceUntilIdle()

            val offlineState = awaitItem() as AlbumsUiState.Success
            assertTrue(offlineState.isOffline)
            assertEquals(onlineState.songs, offlineState.songs) // Same data, just offline flag changed
        }
    }

    @Test
    fun `hides offline banner when network comes back online`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns false
        coEvery { getSavedSongsUseCase() } returns testSongs.take(10)

        val viewModel = createViewModel()

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            // Start offline
            networkStatusFlow.emit(false)
            viewModel.initLoad()
            assertEquals(AlbumsUiState.Loading, awaitItem())

            // Initially offline with saved songs
            val offlineState = awaitItem() as AlbumsUiState.Success
            assertTrue(offlineState.isOffline)
            assertEquals(10, offlineState.songs.size)

            // Simulate network coming back online
            networkStatusFlow.emit(true)
            testDispatcher.scheduler.advanceUntilIdle()

            // Should show online state (banner hidden)
            val onlineState = awaitItem() as AlbumsUiState.Success
            assertFalse(onlineState.isOffline)
        }
    }

    @Test
    fun `does not show offline banner when no songs loaded yet`() = runTest {
        // Given
        val networkFlow = MutableSharedFlow<Boolean>()
        every { networkMonitor.isOnline } returns networkFlow
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns emptyList()

        val viewModel = createViewModel()

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()
            assertEquals(AlbumsUiState.Loading, awaitItem())
            assertEquals(AlbumsUiState.Empty, awaitItem())

            // Simulate network going offline - should not emit new state since no songs
            networkFlow.emit(false)
            testDispatcher.scheduler.advanceUntilIdle()

            // No new emission expected
            expectNoEvents()
        }
    }

    @Test
    fun `network status change does not affect state when already in same status`() = runTest {
        // Given
        val networkFlow = MutableSharedFlow<Boolean>()
        every { networkMonitor.isOnline } returns networkFlow
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs

        val viewModel = createViewModel()

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()
            assertEquals(AlbumsUiState.Loading, awaitItem())

            val onlineState = awaitItem() as AlbumsUiState.Success
            assertFalse(onlineState.isOffline)

            // Emit online again (no change)
            networkFlow.emit(true)
            testDispatcher.scheduler.advanceUntilIdle()

            // No new emission expected since already online
            expectNoEvents()
        }
    }

    @Test
    fun `multiple network status changes are handled correctly`() = runTest {
        // Given
        val networkFlow = MutableSharedFlow<Boolean>()
        every { networkMonitor.isOnline } returns networkFlow
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { getAllSongsUseCase() } returns testSongs

        val viewModel = createViewModel()

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Init, awaitItem())

            viewModel.initLoad()
            assertEquals(AlbumsUiState.Loading, awaitItem())

            val initialState = awaitItem() as AlbumsUiState.Success
            assertFalse(initialState.isOffline)

            // Go offline
            networkFlow.emit(false)
            testDispatcher.scheduler.advanceUntilIdle()
            val offlineState = awaitItem() as AlbumsUiState.Success
            assertTrue(offlineState.isOffline)

            // Go online
            networkFlow.emit(true)
            testDispatcher.scheduler.advanceUntilIdle()
            val backOnlineState = awaitItem() as AlbumsUiState.Success
            assertFalse(backOnlineState.isOffline)

            // Go offline again
            networkFlow.emit(false)
            testDispatcher.scheduler.advanceUntilIdle()
            val offlineAgainState = awaitItem() as AlbumsUiState.Success
            assertTrue(offlineAgainState.isOffline)
        }
    }
}