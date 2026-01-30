package fr.leboncoin.feature.album.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.card.Card
import com.adevinta.spark.components.chips.ChipTinted
import com.adevinta.spark.components.text.Text
import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.domain.R

@OptIn(ExperimentalSparkApi::class)
@Composable
fun SongItem(
    song: Song,
    imageLoader: ImageLoader,
    onItemSelected: (Song) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageRequest = remember(song.thumbnailUrl) {
        ImageRequest.Builder(context)
            .data(song.thumbnailUrl)
            .httpHeaders(
                NetworkHeaders.Builder()
                    .add("User-Agent", "LeboncoinApp/1.0")
                    .build()
            )
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .crossfade(true)
            .build()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 16.dp),
        onClick = { onItemSelected(song) },
    ) {
        Row {
            AsyncImage(
                model = imageRequest,
                imageLoader = imageLoader,
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = song.title,
                        style = SparkTheme.typography.caption,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onToggleFavorite(song.id) }
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChipTinted(
                        text = "Album #${song.albumId}",
                    )
                    ChipTinted(
                        text = "Track #${song.id}",
                        onClick =  { onItemSelected(song) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SongItemPreview() {
    val context = LocalContext.current
    val previewImageLoader = remember {
        ImageLoader.Builder(context).build()
    }

    SparkTheme {
        SongItem(
            song = Song(
                id = 1,
                albumId = 1,
                title = "accusamus beatae ad facilis cum similique qui sunt",
                url = "https://placehold.co/600x600/92c952/white/png",
                thumbnailUrl = "https://placehold.co/150x150/92c952/white/png",
                isFavorite = false
            ),
            imageLoader = previewImageLoader,
            onItemSelected = {},
            onToggleFavorite = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SongItemFavoritePreview() {
    val context = LocalContext.current
    val previewImageLoader = remember {
        ImageLoader.Builder(context).build()
    }

    SparkTheme {
        SongItem(
            song = Song(
                id = 1,
                albumId = 1,
                title = "accusamus beatae ad facilis cum similique qui sunt",
                url = "https://placehold.co/600x600/92c952/white/png",
                thumbnailUrl = "https://placehold.co/150x150/92c952/white/png",
                isFavorite = true
            ),
            imageLoader = previewImageLoader,
            onItemSelected = {},
            onToggleFavorite = {}
        )
    }
}
