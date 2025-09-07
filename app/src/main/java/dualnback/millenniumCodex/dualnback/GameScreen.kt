package com.millenniumCodex.dualnback.uinterface

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dualnback.millenniumCodex.dualnback.NBackViewModel

@Composable
fun GameScreen(
    nLevel: Int,
    totalRounds: Int, // NEW: Receive totalRounds
    onGameEnd: () -> Unit,
    viewModel: NBackViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    // MODIFIED: Pass totalRounds to the ViewModel when the game starts
    LaunchedEffect(key1 = nLevel, key2 = totalRounds) {
        viewModel.startGame(nLevel, totalRounds)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopGame()
        }
    }

    if (gameState.gameOver) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismissing by clicking outside */ },
            title = { Text(text = gameState.gameOverTitle) },
            text = { Text(text = gameState.gameResultText) },
            confirmButton = {
                Button(onClick = onGameEnd) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // MODIFIED: Pass the totalRounds to the Header
        Header(
            score = gameState.score,
            turn = gameState.turn,
            nLevel = gameState.nBackLevel,
            totalRounds = gameState.totalRounds
        )
        GameBoard(currentPosition = gameState.currentPosition)
        Feedback(
            positionFeedback = gameState.positionFeedback,
            soundFeedback = gameState.soundFeedback
        )
        Controls(
            isRunning = gameState.isRunning,
            isPaused = gameState.isPaused,
            onStopClick = onGameEnd,
            onPositionMatch = { viewModel.onPositionMatch() },
            onSoundMatch = { viewModel.onSoundMatch() },
            onPauseClick = { viewModel.pauseGame() },
            onResumeClick = { viewModel.resumeGame() }
        )
    }
}

@Composable
fun Header(score: Int, turn: Int, nLevel: Int, totalRounds: Int) { // MODIFIED
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Score: $score", fontSize = 20.sp)
        Text("N = $nLevel", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        // MODIFIED: Display turn out of total rounds
        Text("Turn: $turn / $totalRounds", fontSize = 20.sp)
    }
}

@Composable
fun GameBoard(currentPosition: Int) {
    Box(
        modifier = Modifier
            .size(300.dp)
            .border(2.dp, MaterialTheme.colorScheme.onBackground)
    ) {
        Column(Modifier.fillMaxSize()) {
            for (i in 0..2) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (j in 0..2) {
                        val index = i * 3 + j
                        GridCell(isLit = (index == currentPosition))
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.GridCell(isLit: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .border(1.dp, Color.Gray)
            .background(if (isLit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
    )
}

@Composable
fun Feedback(positionFeedback: String, soundFeedback: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.height(60.dp)
    ) {
        val feedbackText = @Composable { text: String ->
            val color = when {
                text.contains("Incorrect") -> Color.Red
                text.contains("Correct") -> Color(0xFF4CAF50)
                else -> MaterialTheme.colorScheme.onBackground
            }
            Text(
                text = text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        if (positionFeedback.isNotBlank()) {
            feedbackText("Pos: $positionFeedback")
        }
        if (soundFeedback.isNotBlank()) {
            feedbackText("Sound: $soundFeedback")
        }
    }
}

@Composable
fun Controls(
    isRunning: Boolean,
    isPaused: Boolean,
    onStopClick: () -> Unit,
    onPositionMatch: () -> Unit,
    onSoundMatch: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    val buttonsEnabled = isRunning && !isPaused
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = onPositionMatch, enabled = buttonsEnabled, modifier = Modifier.width(150.dp)) {
                Text("Position")
            }
            Button(onClick = onSoundMatch, enabled = buttonsEnabled, modifier = Modifier.width(150.dp)) {
                Text("Sound")
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { if (isPaused) onResumeClick() else onPauseClick() },
                enabled = isRunning,
                modifier = Modifier.width(150.dp)
            ) {
                Text(if (isPaused) "Resume" else "Pause")
            }
            Button(onClick = onStopClick, modifier = Modifier.width(150.dp)) {
                Text("End Game")
            }
        }
    }
}