package dualnback.millenniumCodex.dualnback.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_results")
data class GameResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val nLevel: Int,
    val score: Int,
    val totalTurns: Int,
    val won: Boolean // New field to track win/loss status
)