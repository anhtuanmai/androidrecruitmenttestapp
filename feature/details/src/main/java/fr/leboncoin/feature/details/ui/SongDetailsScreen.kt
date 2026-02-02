package fr.leboncoin.feature.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import fr.leboncoin.core.data.domain.model.Song
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.buttons.ButtonOutlined
import com.adevinta.spark.components.progress.Spinner
import com.adevinta.spark.components.scaffold.Scaffold
import com.adevinta.spark.components.text.Text
import fr.leboncoin.core.domain.R

@Composable
fun SongDetailsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.initLoad()
        viewModel.onScreenViewed()
    }

    SongDetailsContent(
        uiState = uiState,
        imageLoader = viewModel.imageLoader,
        onBackClick = onBackClick,
        onToggleFavorite = viewModel::toggleFavorite,
        modifier = modifier
    )
}

@Composable
private fun SongDetailsContent(
    uiState: DetailsUiState,
    imageLoader: ImageLoader,
    onBackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        when (uiState) {
            is DetailsUiState.Init, DetailsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Spinner()
                }
            }

            is DetailsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    ButtonOutlined(
                        modifier = Modifier.padding(16.dp),
                        onClick = onBackClick
                    ) {
                        Text(stringResource(R.string.back))
                    }
                    SongDetailsItem(
                        song = uiState.song,
                        onToggleFavorite = onToggleFavorite,
                        imageLoader = imageLoader
                    )
                }
            }

            is DetailsUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.oops_an_error_has_occurred))
                    Spacer(modifier = Modifier.height(16.dp))
                    ButtonOutlined(onClick = onBackClick) {
                        Text(stringResource(R.string.back))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SongDetailsScreenSuccessPreview() {
    val context = LocalContext.current
    val previewImageLoader = ImageLoader.Builder(context).build()

    SparkTheme {
        SongDetailsContent(
            uiState = DetailsUiState.Success(
                song = Song(
                    id = 1,
                    albumId = 1,
                    title = "accusamus beatae ad facilis cum similique qui sunt",
                    url = "https://placehold.co/600x600/92c952/white/png",
                    thumbnailUrl = "https://placehold.co/150x150/92c952/white/png",
                    isFavorite = false
                )
            ),
            imageLoader = previewImageLoader,
            onBackClick = {},
            onToggleFavorite = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SongDetailsScreenLoadingPreview() {
    SparkTheme {
        SongDetailsContent(
            uiState = DetailsUiState.Loading,
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            onBackClick = {},
            onToggleFavorite = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SongDetailsScreenErrorPreview() {
    SparkTheme {
        SongDetailsContent(
            uiState = DetailsUiState.Error("Song not found"),
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            onBackClick = {},
            onToggleFavorite = {}
        )
    }
}
