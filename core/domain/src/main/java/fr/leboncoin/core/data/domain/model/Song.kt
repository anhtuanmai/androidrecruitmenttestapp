package fr.leboncoin.core.data.domain.model

data class Song(
    val id: Int,
    val albumId: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String,
    val isFavorite: Boolean = false
)
