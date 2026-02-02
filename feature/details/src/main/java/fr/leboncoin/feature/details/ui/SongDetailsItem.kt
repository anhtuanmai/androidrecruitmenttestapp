package fr.leboncoin.feature.details.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import fr.leboncoin.core.domain.R
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.text.Text
import fr.leboncoin.core.data.domain.model.Song

@OptIn(ExperimentalSparkApi::class)
@Composable
fun SongDetailsItem(
    song: Song,
    imageLoader: ImageLoader,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(song.url)
                .httpHeaders(
                    NetworkHeaders.Builder()
                        .add("User-Agent", "LeboncoinApp/1.0")
                        .build()
                )
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .crossfade(true)
                .build(),
            imageLoader = imageLoader,
            contentDescription = song.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${song.id}. ${song.title}",
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onToggleFavorite() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Album #${song.albumId}")
    }
}

@Preview(showBackground = true)
@Composable
private fun SongDetailsItemPreview() {
    val context = LocalContext.current
    val previewImageLoader = remember {
        ImageLoader.Builder(context).build()
    }

    SparkTheme {
        SongDetailsItem(
            song = Song(
                id = 1,
                albumId = 1,
                title = "accusamus beatae ad facilis cum similique qui sunt",
                url = "https://placehold.co/600x600/92c952/white/png",
                thumbnailUrl = "https://placehold.co/150x150/92c952/white/png",
                isFavorite = false
            ),
            imageLoader = previewImageLoader,
            onToggleFavorite = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SongDetailsItemFavoritePreview() {
    val context = LocalContext.current
    val previewImageLoader = remember {
        ImageLoader.Builder(context).build()
    }

    SparkTheme {
        SongDetailsItem(
            song = Song(
                id = 1,
                albumId = 1,
                title = "accusamus beatae ad facilis cum similique qui sunt",
                url = "https://placehold.co/600x600/92c952/white/png",
                thumbnailUrl = "https://placehold.co/150x150/92c952/white/png",
                isFavorite = true
            ),
            imageLoader = previewImageLoader,
            onToggleFavorite = {}
        )
    }
}
