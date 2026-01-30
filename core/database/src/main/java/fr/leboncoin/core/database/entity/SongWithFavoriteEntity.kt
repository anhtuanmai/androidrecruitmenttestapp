package fr.leboncoin.core.database.entity

import androidx.room.Embedded

data class SongWithFavoriteEntity(
    @Embedded val song: SongEntity,
    val isFavorite: Boolean
)
