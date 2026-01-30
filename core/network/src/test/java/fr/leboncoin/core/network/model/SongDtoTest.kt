package fr.leboncoin.core.network.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Test

class SongDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `deserialize valid JSON to SongDto`() {
        val jsonString = """
            {
                "id": 1,
                "albumId": 100,
                "title": "Test Song",
                "url": "https://example.com/song.mp3",
                "thumbnailUrl": "https://example.com/thumb.jpg"
            }
        """.trimIndent()

        val result = json.decodeFromString<SongDto>(jsonString)

        assertEquals(1, result.id)
        assertEquals(100, result.albumId)
        assertEquals("Test Song", result.title)
        assertEquals("https://example.com/song.mp3", result.url)
        assertEquals("https://example.com/thumb.jpg", result.thumbnailUrl)
    }

    @Test
    fun `deserialize JSON list to List of SongDto`() {
        val jsonString = """
            [
                {
                    "id": 1,
                    "albumId": 100,
                    "title": "Song 1",
                    "url": "https://example.com/song1.mp3",
                    "thumbnailUrl": "https://example.com/thumb1.jpg"
                },
                {
                    "id": 2,
                    "albumId": 100,
                    "title": "Song 2",
                    "url": "https://example.com/song2.mp3",
                    "thumbnailUrl": "https://example.com/thumb2.jpg"
                }
            ]
        """.trimIndent()

        val result = json.decodeFromString<List<SongDto>>(jsonString)

        assertEquals(2, result.size)
        assertEquals(1, result[0].id)
        assertEquals(2, result[1].id)
    }

    @Test
    fun `deserialize JSON with unknown keys ignores them`() {
        val jsonString = """
            {
                "id": 1,
                "albumId": 100,
                "title": "Test Song",
                "url": "https://example.com/song.mp3",
                "thumbnailUrl": "https://example.com/thumb.jpg",
                "unknownField": "should be ignored",
                "anotherUnknown": 123
            }
        """.trimIndent()

        val result = json.decodeFromString<SongDto>(jsonString)

        assertEquals(1, result.id)
        assertEquals("Test Song", result.title)
    }

    @Test
    fun `serialize SongDto to JSON`() {
        val songDto = SongDto(
            id = 1,
            albumId = 100,
            title = "Test Song",
            url = "https://example.com/song.mp3",
            thumbnailUrl = "https://example.com/thumb.jpg"
        )

        val result = json.encodeToString(songDto)

        val decoded = json.decodeFromString<SongDto>(result)
        assertEquals(songDto, decoded)
    }

    @Test
    fun `SongDto equals works correctly`() {
        val song1 = SongDto(1, 100, "Title", "url", "thumb")
        val song2 = SongDto(1, 100, "Title", "url", "thumb")
        val song3 = SongDto(2, 100, "Title", "url", "thumb")

        assertEquals(song1, song2)
        assertEquals(song1.hashCode(), song2.hashCode())
        assert(song1 != song3)
    }

    @Test
    fun `SongDto copy works correctly`() {
        val original = SongDto(1, 100, "Original", "url", "thumb")
        val copied = original.copy(title = "Copied")

        assertEquals(1, copied.id)
        assertEquals(100, copied.albumId)
        assertEquals("Copied", copied.title)
        assertEquals("url", copied.url)
        assertEquals("thumb", copied.thumbnailUrl)
    }
}
