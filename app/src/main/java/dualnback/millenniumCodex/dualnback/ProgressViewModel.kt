package dualnback.millenniumCodex.dualnback

import androidx.lifecycle.ViewModel
import dualnback.millenniumCodex.dualnback.data.GameResultDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    gameResultDao: GameResultDao
) : ViewModel() {
    val gameResults = gameResultDao.getAllResultsSortedByDate()
}