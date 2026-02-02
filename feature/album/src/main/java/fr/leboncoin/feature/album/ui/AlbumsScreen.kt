package fr.leboncoin.feature.album.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.buttons.ButtonFilled
import com.adevinta.spark.components.buttons.ButtonOutlined
import com.adevinta.spark.components.progress.Spinner
import com.adevinta.spark.components.scaffold.Scaffold
import com.adevinta.spark.components.text.Text
import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.domain.R

@Composable
fun AlbumsScreen(
    onItemSelected: (Song) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.initLoad()
        viewModel.onScreenViewed()
    }

    AlbumsContent(
        uiState = uiState,
        imageLoader = viewModel.imageLoader,
        onItemSelected = { item ->
            viewModel.trackSelection(item)
            onItemSelected(item)
        },
        onToggleFavorite = viewModel::onToggleFavorite,
        onPreviousClick = viewModel::previousPage,
        onNextClick = viewModel::nextPage,
        onRetryClick = viewModel::retry,
        modifier = modifier
    )
}

@Composable
private fun AlbumsContent(
    uiState: AlbumsUiState,
    imageLoader: ImageLoader,
    onItemSelected: (Song) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        when (uiState) {
            is AlbumsUiState.Init, AlbumsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Spinner()
                }
            }

            is AlbumsUiState.Success -> {
                Column {
                    if (uiState.isOffline) {
                        // Red banner extends into status bar, content padded below it
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Red)
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_internet_connection),
                                color = Color.White
                            )
                        }
                    }
                    LazyColumn(
                        contentPadding = if (uiState.isOffline) {
                            PaddingValues(0.dp)
                        } else {
                            PaddingValues(
                                top = paddingValues.calculateTopPadding()
                            )
                        },
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(
                            items = uiState.songs,
                            contentType = { "song" },
                            key = { song -> song.id }
                        ) { song ->
                            SongItem(
                                song = song,
                                imageLoader = imageLoader,
                                onToggleFavorite = onToggleFavorite,
                                onItemSelected = onItemSelected,
                            )
                        }
                    }
                    PaginationIndicator(
                        currentPage = uiState.currentPage,
                        hasPrevious = uiState.hasPrevious,
                        hasNext = uiState.hasNext,
                        onPreviousClick = onPreviousClick,
                        onNextClick = onNextClick,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    )
                }
            }

            is AlbumsUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_songs_available))
                }
            }

            is AlbumsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.oops_an_error_has_occurred))
                        ButtonFilled(onClick = onRetryClick) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaginationIndicator(
    currentPage: Int,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ButtonOutlined(
            onClick = onPreviousClick,
            enabled = hasPrevious
        ) {
            Text(stringResource(R.string.previous))
        }

        Text(text = currentPage.toString())

        ButtonOutlined(
            onClick = onNextClick,
            enabled = hasNext
        ) {
            Text(stringResource(R.string.next))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumsScreenSuccessPreview() {
    val sampleSongs = List(5) { index ->
        Song(
            id = index,
            albumId = 1,
            title = "Sample Song ${index + 1}",
            url = "",
            thumbnailUrl = "",
            isFavorite = index % 2 == 0
        )
    }
    SparkTheme {
        AlbumsContent(
            uiState = AlbumsUiState.Success(
                songs = sampleSongs,
                currentPage = 1,
                totalPages = 3,
                isOffline = false
            ),
            imageLoader = ImageLoader(LocalContext.current),
            onItemSelected = {},
            onToggleFavorite = {},
            onPreviousClick = {},
            onNextClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumsScreenOfflinePreview() {
    val sampleSongs = List(3) { index ->
        Song(
            id = index,
            albumId = 1,
            title = "Cached Song ${index + 1}",
            url = "",
            thumbnailUrl = "",
            isFavorite = false
        )
    }
    SparkTheme {
        AlbumsContent(
            uiState = AlbumsUiState.Success(
                songs = sampleSongs,
                currentPage = 1,
                totalPages = 1,
                isOffline = true
            ),
            imageLoader = ImageLoader(LocalContext.current),
            onItemSelected = {},
            onToggleFavorite = {},
            onPreviousClick = {},
            onNextClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumsScreenLoadingPreview() {
    SparkTheme {
        AlbumsContent(
            uiState = AlbumsUiState.Loading,
            imageLoader = ImageLoader(LocalContext.current),
            onItemSelected = {},
            onToggleFavorite = {},
            onPreviousClick = {},
            onNextClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumsScreenEmptyPreview() {
    SparkTheme {
        AlbumsContent(
            uiState = AlbumsUiState.Empty,
            imageLoader = ImageLoader(LocalContext.current),
            onItemSelected = {},
            onToggleFavorite = {},
            onPreviousClick = {},
            onNextClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumsScreenErrorPreview() {
    SparkTheme {
        AlbumsContent(
            uiState = AlbumsUiState.Error("Network error"),
            imageLoader = ImageLoader(LocalContext.current),
            onItemSelected = {},
            onToggleFavorite = {},
            onPreviousClick = {},
            onNextClick = {},
            onRetryClick = {}
        )
    }
}
