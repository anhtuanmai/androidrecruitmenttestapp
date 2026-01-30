package fr.leboncoin.core.data.utils

import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.database.entity.SongEntity
import fr.leboncoin.core.database.entity.SongWithFavoriteEntity
import fr.leboncoin.core.network.model.SongDto

// SongWithFavoriteEntity -> Domain
fun SongWithFavoriteEntity.toDomain(): Song = Song(
    id = song.id,
    albumId = song.albumId,
    title = song.title,
    url = song.url,
    thumbnailUrl = song.thumbnailUrl,
    isFavorite = isFavorite
)

// Domain -> Entity
fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    albumId = albumId,
    title = title,
    url = url,
    thumbnailUrl = thumbnailUrl
)

@JvmName("songWithFavoriteListToDomain")
fun List<SongWithFavoriteEntity>.toDomainList(): List<Song> = map { it.toDomain() }

@JvmName("domainListToEntity")
fun List<Song>.toEntityList(): List<SongEntity> = map { it.toEntity() }
