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
package com.mxalbert.compose.countdowntimer.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
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
