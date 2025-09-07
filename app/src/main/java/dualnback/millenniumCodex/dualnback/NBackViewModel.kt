package dualnback.millenniumCodex.dualnback

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dualnback.millenniumCodex.dualnback.R
import dualnback.millenniumCodex.dualnback.data.GameResult
import dualnback.millenniumCodex.dualnback.data.GameResultDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

// ... NBackEvent and GameState data classes remain the same ...
data class NBackEvent(val position: Int, val soundId: Int)

data class GameState(
    val nBackLevel: Int = 2,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val gameOver: Boolean = false,
    val gameOverTitle: String = "",
    val gameResultText: String = "",
    val currentPosition: Int = -1,
    val events: List<NBackEvent> = emptyList(),
    val score: Int = 0,
    val turn: Int = 0,
    val totalRounds: Int = 20,
    val correctPositionGuesses: Int = 0,
    val correctSoundGuesses: Int = 0,
    val positionFeedback: String = "",
    val soundFeedback: String = "",
    val positionButtonPressed: Boolean = false,
    val soundButtonPressed: Boolean = false
)


@HiltViewModel
class NBackViewModel @Inject constructor(
    private val gameResultDao: GameResultDao,
    application: Application
) : AndroidViewModel(application) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private var gameLoopJob: Job? = null
    private val soundMap = mapOf(
        'a' to R.raw.a, 'b' to R.raw.b, 'c' to R.raw.c, 'd' to R.raw.d,
        'e' to R.raw.e, 'f' to R.raw.f, 'g' to R.raw.g, 'h' to R.raw.h
    )
    private val sounds = soundMap.values.toList()
    private var mediaPlayer: MediaPlayer? = null

    // NEW: Define a constant for the desired probability of a match
    private val MATCH_PROBABILITY = 0.30 // 30% chance of a match for each stimulus

    // ... startGame, runGameLoop, stopGame, pauseGame, resumeGame, resetGame remain the same ...

    fun startGame(nLevel: Int, totalRounds: Int) {
        if (_gameState.value.isRunning) return
        resetGame(nLevel, totalRounds)
        _gameState.update { it.copy(isRunning = true) }
        runGameLoop()
    }

    private fun runGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (_gameState.value.turn < _gameState.value.totalRounds) {
                if (!_gameState.value.isPaused) {
                    runGameStep()
                }
                delay(3000)
            }
            endGame()
        }
    }

    fun stopGame() {
        gameLoopJob?.cancel()
        if (_gameState.value.isRunning && !_gameState.value.gameOver) {
            saveResult(won = false, forfeited = true)
            _gameState.update { it.copy(isRunning = false, isPaused = false) }
        }
    }

    fun pauseGame() {
        if (!_gameState.value.isRunning || _gameState.value.isPaused) return
        _gameState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        if (!_gameState.value.isRunning || !_gameState.value.isPaused) return
        _gameState.update { it.copy(isPaused = false) }
    }

    private fun resetGame(nLevel: Int, totalRounds: Int) {
        _gameState.value = GameState(nBackLevel = nLevel, totalRounds = totalRounds)
    }

    // --- MODIFIED: This is the core of the new logic ---
    private fun runGameStep() {
        evaluateTurn()

        val state = _gameState.value
        val n = state.nBackLevel
        var newPosition = Random.nextInt(9)
        var newSoundId = sounds.random()

        // After the first N turns, we can start creating intentional matches
        if (state.turn >= n) {
            val nBackEvent = state.events[state.turn - n]

            // Decide if we should force a position match
            if (Random.nextDouble() < MATCH_PROBABILITY) {
                newPosition = nBackEvent.position
            }

            // Decide if we should force a sound match
            if (Random.nextDouble() < MATCH_PROBABILITY) {
                newSoundId = nBackEvent.soundId
            }
        }

        val newEvent = NBackEvent(position = newPosition, soundId = newSoundId)
        playSound(newSoundId)

        _gameState.update {
            it.copy(
                turn = it.turn + 1,
                currentPosition = newPosition,
                events = it.events + newEvent,
                positionButtonPressed = false,
                soundButtonPressed = false
            )
        }
    }

    // ... evaluateTurn, endGame, and the rest of the file remain exactly the same ...
    private fun evaluateTurn() {
        val state = _gameState.value
        val n = state.nBackLevel

        if (state.turn < n + 1) {
            _gameState.update { it.copy(positionFeedback = "", soundFeedback = "") }
            return
        }

        val lastEvent = state.events[state.turn - 1]
        val nBackEvent = state.events[state.turn - 1 - n]

        val isPositionMatch = lastEvent.position == nBackEvent.position
        val isSoundMatch = lastEvent.soundId == nBackEvent.soundId

        var posFeedback = ""
        var sndFeedback = ""
        var correctPos = state.correctPositionGuesses
        var correctSnd = state.correctSoundGuesses
        var turnScore = 0

        // Evaluate position
        if (isPositionMatch == state.positionButtonPressed) {
            correctPos++
            turnScore += if (isPositionMatch) 10 else 5
            posFeedback = if (isPositionMatch) "Correct!" else ""
        } else {
            turnScore -= 5
            posFeedback = "Incorrect"
        }

        // Evaluate sound
        if (isSoundMatch == state.soundButtonPressed) {
            correctSnd++
            turnScore += if (isSoundMatch) 10 else 5
            sndFeedback = if (isSoundMatch) "Correct!" else ""
        } else {
            turnScore -= 5
            sndFeedback = "Incorrect"
        }

        _gameState.update {
            it.copy(
                score = it.score + turnScore,
                correctPositionGuesses = correctPos,
                correctSoundGuesses = correctSnd,
                positionFeedback = posFeedback,
                soundFeedback = sndFeedback
            )
        }
    }

    private fun endGame() {
        gameLoopJob?.cancel()
        evaluateTurn()

        viewModelScope.launch {
            delay(100)
            val finalState = _gameState.value
            val n = finalState.nBackLevel

            val scorableTurns = finalState.totalRounds - n
            val totalPossibleCorrectGuesses = scorableTurns * 2
            val totalCorrectGuesses = finalState.correctPositionGuesses + finalState.correctSoundGuesses

            val accuracy = if (totalPossibleCorrectGuesses > 0) {
                totalCorrectGuesses.toDouble() / totalPossibleCorrectGuesses.toDouble()
            } else 0.0

            val won = accuracy >= 0.80
            val accuracyPercent = "%.0f".format(accuracy * 100)

            val title = if (won) "You Win!" else "You Lose"
            val resultText = "Accuracy: $accuracyPercent%" + if (!won) "\nTry this level again." else ""

            saveResult(won)
            _gameState.update {
                it.copy(
                    isRunning = false,
                    isPaused = false,
                    gameOver = true,
                    gameOverTitle = title,
                    gameResultText = resultText
                )
            }
        }
    }

    fun onPositionMatch() {
        if (!_gameState.value.isRunning || _gameState.value.isPaused || _gameState.value.positionButtonPressed) return
        _gameState.update { it.copy(positionButtonPressed = true) }
    }

    fun onSoundMatch() {
        if (!_gameState.value.isRunning || _gameState.value.isPaused || _gameState.value.soundButtonPressed) return
        _gameState.update { it.copy(soundButtonPressed = true) }
    }

    private fun saveResult(won: Boolean, forfeited: Boolean = false) {
        val finalState = _gameState.value
        if (finalState.turn > 0 || forfeited) {
            viewModelScope.launch {
                val result = GameResult(
                    timestamp = System.currentTimeMillis(),
                    nLevel = finalState.nBackLevel,
                    score = finalState.score,
                    totalTurns = finalState.totalRounds,
                    won = won
                )
                gameResultDao.insert(result)
            }
        }
    }

    private fun playSound(soundId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(getApplication(), soundId).apply {
            setOnCompletionListener { it.release() }
            start()
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        gameLoopJob?.cancel()
    }
}