package fr.leboncoin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import fr.leboncoin.core.database.entity.SongEntity
import fr.leboncoin.core.database.entity.SongWithFavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("SELECT id FROM songs")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("""
        SELECT s.*, CASE WHEN f.songId IS NOT NULL THEN 1 ELSE 0 END AS isFavorite
        FROM songs s
        LEFT JOIN favorite_songs f ON s.id = f.songId
    """)
    suspend fun getAllSongsWithFavorites(): List<SongWithFavoriteEntity>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Int): SongEntity?

    @Query("""
        SELECT s.*, CASE WHEN f.songId IS NOT NULL THEN 1 ELSE 0 END AS isFavorite
        FROM songs s
        LEFT JOIN favorite_songs f ON s.id = f.songId
        WHERE s.id = :id
    """)
    suspend fun getSongByIdWithFavorite(id: Int): SongWithFavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: SongEntity)

    @Query("DELETE FROM songs")
    suspend fun deleteAll()
}
