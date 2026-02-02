package fr.leboncoin.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.core.database.AppDatabase
import fr.leboncoin.core.database.dao.FavoriteSongDao
import fr.leboncoin.core.database.dao.SongDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .build()

    @Provides
    @Singleton
    fun provideSongDao(database: AppDatabase): SongDao =
        database.songDao()

    @Provides
    @Singleton
    fun provideFavoriteSongDao(database: AppDatabase): FavoriteSongDao =
        database.favoriteSongDao()
}
