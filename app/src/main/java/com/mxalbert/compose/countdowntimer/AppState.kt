/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mxalbert.compose.countdowntimer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.math.abs

enum class Screen { Edit, Timer }

const val SegmentCount = 3
const val DigitCount = 6

class AppState(
    screen: Screen = Screen.Edit,
    countdownTime: Int = 0,
    millisPassed: Int = 0
) {
    var screen: Screen by mutableStateOf(screen)
    var isCountingDown: Boolean by mutableStateOf(false)
    var countdownTime: Int by mutableStateOf(countdownTime)

    private var _millisPassed: Int by mutableStateOf(millisPassed)
    var millisPassed: Int
        get() = _millisPassed
        set(value) {
            _millisPassed = value
            updatedTime = System.currentTimeMillis()
        }

    var updatedTime: Long = 0

    // To reset millisPassed/countdownTime after timer coroutine is canceled
    var resetMillisPassed: Boolean = false
    var resetCountdownTime: Boolean = false
}

private val Saver: Saver<AppState, *> = listSaver(
    save = {
        listOf(
            it.screen.ordinal,
            it.countdownTime,
            it.millisPassed
        )
    },
    restore = {
        AppState(
            screen = Screen.values()[it[0]],
            countdownTime = it[1],
            millisPassed = it[2]
        )
    }
)

@Composable
fun rememberAppState() = rememberSaveable(saver = Saver) { AppState() }

val AppState.currentTime: Int
    get() = countdownTime - millisPassed / 1000

val AppState.timeSegments: List<Int>
    get() {
        var time = abs(currentTime)
        return mutableListOf<Int>().apply {
            repeat(SegmentCount) { i ->
                val segment = if (i == SegmentCount - 1) time else time % 60
                add(0, segment)
                time /= 60
            }
        }
    }

fun AppState.setTimeFrom(segments: List<Int>) {
    var time = 0
    segments.forEach {
        time = time * 60 + it
    }
    countdownTime = time
}

val AppState.progress: Float
    get() = (millisPassed / 1000f / countdownTime).coerceAtMost(1f)
