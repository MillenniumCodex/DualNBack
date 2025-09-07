package dualnback.millenniumCodex.dualnback.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GameResult::class], version = 2, exportSchema = false) // Version incremented
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameResultDao(): GameResultDao
}