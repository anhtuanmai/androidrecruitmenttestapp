package fr.leboncoin.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class SongDto(
    val id: Int,
    val albumId: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String
)