package fr.leboncoin.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.leboncoin.core.database.dao.FavoriteSongDao
import fr.leboncoin.core.database.dao.SongDao
import fr.leboncoin.core.database.entity.FavoriteEntity
import fr.leboncoin.core.database.entity.SongEntity

@Database(
    entities = [SongEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun favoriteSongDao(): FavoriteSongDao
}
