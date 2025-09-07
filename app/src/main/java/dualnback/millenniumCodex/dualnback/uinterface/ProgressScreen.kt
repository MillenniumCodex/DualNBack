package dualnback.millenniumCodex.dualnback.uinterface

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// --- FIX IS HERE: Add the correct import for the ViewModel ---
import dualnback.millenniumCodex.dualnback.ProgressViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val gameResults by viewModel.gameResults.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Progress") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (gameResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Play a game to see your progress!")
                }
            } else {
                val chartData = gameResults.groupBy { it.nLevel }
                chartData.keys.sorted().forEach { nLevel ->
                    val results = chartData[nLevel]!!
                    Text(
                        "N-Level: $nLevel",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(top = 24.dp, bottom = 8.dp)
                            .align(Alignment.Start)
                    )

                    val totalGames = results.size
                    val wins = results.count { it.won }
                    val winRate = if (totalGames > 0) (wins.toDouble() / totalGames.toDouble()) * 100 else 0.0

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem("Games", "$totalGames")
                            StatItem("Wins", "$wins")
                            StatItem("Win Rate", "${"%.0f".format(winRate)}%")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Score Progression",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .align(Alignment.Start)
                    )

                    val scores = results.map { it.score }
                    val maxScore = scores.maxOrNull() ?: 0
                    val minScore = scores.minOrNull() ?: 0
                    val yAxisMax = (maxScore + 5).toFloat()
                    val yAxisMin = min((minScore - 5).toFloat(), 0f)
                    val axisValuesOverrider = AxisValuesOverrider.fixed(minY = yAxisMin, maxY = yAxisMax)
                    val entries = results.mapIndexed { index, result -> entryOf(index.toFloat(), result.score.toFloat()) }
                    val entryProducer = ChartEntryModelProducer(entries)
                    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                        results.getOrNull(value.toInt())?.let {
                            dateFormat.format(Date(it.timestamp))
                        } ?: ""
                    }

                    Chart(
                        chart = lineChart(axisValuesOverrider = axisValuesOverrider),
                        chartModelProducer = entryProducer,
                        startAxis = rememberStartAxis(title = "Score"),
                        bottomAxis = rememberBottomAxis(
                            title = "Date",
                            valueFormatter = bottomAxisValueFormatter,
                            labelRotationDegrees = 45f
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}