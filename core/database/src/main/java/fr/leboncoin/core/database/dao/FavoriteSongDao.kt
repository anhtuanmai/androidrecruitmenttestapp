package fr.leboncoin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.leboncoin.core.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteSongDao {

    @Query("SELECT * FROM favorite_songs")
    suspend fun getAllFavorites(): List<FavoriteEntity>

    @Query("SELECT songId FROM favorite_songs")
    suspend fun getAllFavoriteIds(): List<Int>

    @Query("SELECT songId FROM favorite_songs")
    fun observeFavoriteIds(): Flow<List<Int>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)")
    suspend fun isFavorite(songId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun removeFavorite(songId: Int)

    @Query("DELETE FROM favorite_songs")
    suspend fun clearAll()
}
