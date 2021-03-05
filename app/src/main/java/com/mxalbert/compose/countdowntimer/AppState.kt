package com.mxalbert.compose.countdowntimer

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector

const val DigitCount = 6

class AppState(
    digits: List<Int> = List(DigitCount) { 0 }
) {
    val timeSegments: MutableList<Int> = mutableStateListOf<Int>().apply { addAll(digits) }
    var fabIcon: ImageVector? by mutableStateOf(null)
    var onFabPressed: () -> Unit by mutableStateOf({})
}

private val Saver: Saver<AppState, *> = listSaver(
    save = { it.timeSegments },
    restore = { AppState(it) }
)

@Composable
fun rememberAppState() = rememberSaveable(saver = Saver) { AppState() }
