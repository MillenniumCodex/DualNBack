package dualnback.millenniumCodex.dualnback.di

import android.content.Context
import androidx.room.Room
import dualnback.millenniumCodex.dualnback.data.AppDatabase
import dualnback.millenniumCodex.dualnback.data.GameResultDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "nback_database"
        ).fallbackToDestructiveMigration() // Added for easier DB versioning
            .build()
    }

    @Provides
    fun provideGameResultDao(database: AppDatabase): GameResultDao {
        return database.gameResultDao()
    }
}