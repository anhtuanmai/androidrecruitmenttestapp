package fr.leboncoin.core.network.api

import fr.leboncoin.core.network.model.SongDto
import retrofit2.http.GET

interface AlbumApiService {
    
    @GET("img/shared/technical-test.json")
    suspend fun getSongs(): List<SongDto>
    
    companion object {
        const val BASE_URL = "https://static.leboncoin.fr/"
    }
}