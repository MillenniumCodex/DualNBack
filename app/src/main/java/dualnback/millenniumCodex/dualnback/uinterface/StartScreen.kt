package dualnback.millenniumCodex.dualnback.uinterface

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StartScreen(
    // MODIFIED: Pass both nLevel and rounds
    onStartGame: (nLevel: Int, rounds: Int) -> Unit,
    onShowProgress: () -> Unit
) {
    var nLevel by remember { mutableStateOf(2) }
    var rounds by remember { mutableStateOf(20) } // NEW: State for rounds

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Dual N-Back", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(48.dp))

        // N-Level Selector
        SettingSelector(
            label = "Select N-Level",
            value = nLevel,
            onDecrement = { if (nLevel > 1) nLevel-- },
            onIncrement = { nLevel++ },
            decrementEnabled = nLevel > 1
        )

        Spacer(modifier = Modifier.height(24.dp))

        // NEW: Rounds Selector
        SettingSelector(
            label = "Select Rounds",
            value = rounds,
            onDecrement = { if (rounds > 10) rounds -= 5 },
            onIncrement = { if (rounds < 50) rounds += 5 },
            decrementEnabled = rounds > 10,
            incrementEnabled = rounds < 50
        )


        Spacer(modifier = Modifier.height(32.dp))

        Button(
            // MODIFIED: Pass both values to the callback
            onClick = { onStartGame(nLevel, rounds) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Start Game")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onShowProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("View Progress")
        }
    }
}

// NEW: A reusable composable for the +/- selectors to keep the code clean
@Composable
fun SettingSelector(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    decrementEnabled: Boolean = true,
    incrementEnabled: Boolean = true
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Button(onClick = onDecrement, enabled = decrementEnabled) {
                Text("-", fontSize = 24.sp)
            }
            Text(
                text = "$value",
                fontSize = 32.sp,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(60.dp) // Give it a fixed width to prevent UI jumps
            )
            Button(onClick = onIncrement, enabled = incrementEnabled) {
                Text("+", fontSize = 24.sp)
            }
        }
    }
}