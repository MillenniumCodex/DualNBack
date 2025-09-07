package dualnback.millenniumCodex.dualnback.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameResultDao {
    @Insert
    suspend fun insert(gameResult: GameResult)

    @Query("SELECT * FROM game_results ORDER BY timestamp ASC")
    fun getAllResultsSortedByDate(): Flow<List<GameResult>>
}