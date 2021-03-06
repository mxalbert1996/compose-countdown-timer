package com.mxalbert.compose.countdowntimer.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.ui.graphics.vector.ImageVector
import com.mxalbert.compose.countdowntimer.R

enum class Icon(val image: ImageVector, @StringRes val contentDescription: Int) {
    Backspace(Icons.Outlined.Backspace, R.string.backspace),
    Start(Icons.Filled.PlayArrow, R.string.start),
    Pause(Icons.Filled.Pause, R.string.pause),
    Stop(Icons.Filled.Stop, R.string.stop),
    Reset(Icons.Filled.Replay, R.string.reset),
    Delete(Icons.Filled.Delete, R.string.delete)
}
