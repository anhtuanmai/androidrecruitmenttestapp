package fr.leboncoin.core.data.utils

import fr.leboncoin.core.data.domain.model.Song
import fr.leboncoin.core.database.entity.SongEntity
import fr.leboncoin.core.database.entity.SongWithFavoriteEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class SongMapperTest {

    @Test
    fun `SongWithFavoriteEntity toDomain mapping is correct`() {
        // Given
        val songEntity = SongEntity(1, 10, "title", "url", "thumb")
        val entity = SongWithFavoriteEntity(songEntity, isFavorite = true)

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(1, domain.id)
        assertEquals(10, domain.albumId)
        assertEquals("title", domain.title)
        assertEquals("url", domain.url)
        assertEquals("thumb", domain.thumbnailUrl)
        assertEquals(true, domain.isFavorite)
    }

    @Test
    fun `Song toEntity mapping is correct`() {
        // Given
        val domain = Song(1, 10, "title", "url", "thumb", true)

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals(1, entity.id)
        assertEquals(10, entity.albumId)
        assertEquals("title", entity.title)
        assertEquals("url", entity.url)
        assertEquals("thumb", entity.thumbnailUrl)
    }

    @Test
    fun `List of SongWithFavoriteEntity toDomainList mapping is correct`() {
        // Given
        val songEntity1 = SongEntity(1, 10, "title1", "url1", "thumb1")
        val entity1 = SongWithFavoriteEntity(songEntity1, isFavorite = true)
        val songEntity2 = SongEntity(2, 20, "title2", "url2", "thumb2")
        val entity2 = SongWithFavoriteEntity(songEntity2, isFavorite = false)
        val entityList = listOf(entity1, entity2)

        // When
        val domainList = entityList.toDomainList()

        // Then
        assertEquals(2, domainList.size)
        assertEquals(1, domainList[0].id)
        assertEquals(true, domainList[0].isFavorite)
        assertEquals(2, domainList[1].id)
        assertEquals(false, domainList[1].isFavorite)
    }

    @Test
    fun `List of Song toEntityList mapping is correct`() {
        // Given
        val domain1 = Song(1, 10, "title1", "url1", "thumb1", true)
        val domain2 = Song(2, 20, "title2", "url2", "thumb2", false)
        val domainList = listOf(domain1, domain2)

        // When
        val entityList = domainList.toEntityList()

        // Then
        assertEquals(2, entityList.size)
        assertEquals(1, entityList[0].id)
        assertEquals(10, entityList[0].albumId)
        assertEquals(2, entityList[1].id)
        assertEquals(20, entityList[1].albumId)
    }
}
