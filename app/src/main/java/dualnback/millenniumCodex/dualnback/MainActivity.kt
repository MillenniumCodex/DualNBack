package dualnback.millenniumCodex.dualnback

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.millenniumCodex.dualnback.uinterface.GameScreen
import dualnback.millenniumCodex.dualnback.uinterface.ProgressScreen
import dualnback.millenniumCodex.dualnback.uinterface.StartScreen
import dualnback.millenniumCodex.dualnback.ui.theme.DualNBackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DualNBackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NBackApp()
                }
            }
        }
    }
}

@Composable
fun NBackApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "start") {
        composable("start") {
            StartScreen(
                // MODIFIED: onStartGame now provides both nLevel and rounds
                onStartGame = { nLevel, rounds ->
                    navController.navigate("game/$nLevel/$rounds")
                },
                onShowProgress = {
                    navController.navigate("progress")
                }
            )
        }
        composable(
            // MODIFIED: Route now includes rounds
            route = "game/{nLevel}/{rounds}",
            // MODIFIED: Add an argument for rounds
            arguments = listOf(
                navArgument("nLevel") { type = NavType.IntType },
                navArgument("rounds") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val nLevel = backStackEntry.arguments?.getInt("nLevel") ?: 2
            val rounds = backStackEntry.arguments?.getInt("rounds") ?: 20 // Retrieve rounds
            GameScreen(
                nLevel = nLevel,
                totalRounds = rounds, // Pass rounds to the game screen
                onGameEnd = {
                    navController.popBackStack()
                }
            )
        }
        composable("progress") {
            ProgressScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}