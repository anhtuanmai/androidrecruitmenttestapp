package fr.leboncoin.core.network.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import fr.leboncoin.core.network.model.SongDto
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.HttpURLConnection

class AlbumApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: AlbumApiService

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        apiService = retrofit.create(AlbumApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getSongs returns list of songs on success`() = runTest {
        val responseBody = """
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

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json")
        )

        val result = apiService.getSongs()

        assertEquals(2, result.size)
        assertEquals(1, result[0].id)
        assertEquals("Song 1", result[0].title)
        assertEquals(2, result[1].id)
        assertEquals("Song 2", result[1].title)
    }

    @Test
    fun `getSongs returns empty list when response is empty array`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("[]")
                .setHeader("Content-Type", "application/json")
        )

        val result = apiService.getSongs()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getSongs makes correct request path`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("[]")
                .setHeader("Content-Type", "application/json")
        )

        apiService.getSongs()

        val request = mockWebServer.takeRequest()
        assertEquals("/img/shared/technical-test.json", request.path)
        assertEquals("GET", request.method)
    }

    @Test(expected = HttpException::class)
    fun `getSongs throws HttpException on 404`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                .setBody("""{"error": "Not found"}""")
        )

        apiService.getSongs()
    }

    @Test(expected = HttpException::class)
    fun `getSongs throws HttpException on 500`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody("""{"error": "Server error"}""")
        )

        apiService.getSongs()
    }

    @Test
    fun `getSongs parses response with extra fields`() = runTest {
        val responseBody = """
            [
                {
                    "id": 1,
                    "albumId": 100,
                    "title": "Song 1",
                    "url": "https://example.com/song1.mp3",
                    "thumbnailUrl": "https://example.com/thumb1.jpg",
                    "extraField": "ignored",
                    "anotherExtra": 123
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json")
        )

        val result = apiService.getSongs()

        assertEquals(1, result.size)
        assertEquals(1, result[0].id)
    }

    @Test
    fun `getSongs handles large response`() = runTest {
        val songs = (1..100).map { i ->
            """
                {
                    "id": $i,
                    "albumId": ${i / 10},
                    "title": "Song $i",
                    "url": "https://example.com/song$i.mp3",
                    "thumbnailUrl": "https://example.com/thumb$i.jpg"
                }
            """.trimIndent()
        }
        val responseBody = "[${songs.joinToString(",")}]"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json")
        )

        val result = apiService.getSongs()

        assertEquals(100, result.size)
        assertEquals(1, result.first().id)
        assertEquals(100, result.last().id)
    }

    @Test
    fun `BASE_URL is correct`() {
        assertEquals("https://static.leboncoin.fr/", AlbumApiService.BASE_URL)
    }
}
