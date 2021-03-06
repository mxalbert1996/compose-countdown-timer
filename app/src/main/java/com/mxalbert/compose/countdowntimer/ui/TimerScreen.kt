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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxalbert.compose.countdowntimer.AppState
import com.mxalbert.compose.countdowntimer.currentTime
import com.mxalbert.compose.countdowntimer.progress
import com.mxalbert.compose.countdowntimer.timeSegments
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(state: AppState) {
    LaunchedEffect(state.isCountingDown) {
        with(state) {
            if (isCountingDown) {
                updatedTime = System.currentTimeMillis()

                fun updateTime() {
                    millisPassed += (System.currentTimeMillis() - updatedTime).toInt()
                }

                try {
                    while (true) {
                        updateTime()
                        delay(1000L - millisPassed % 1000)
                    }
                } catch (e: CancellationException) {
                    if (resetMillisPassed) {
                        if (resetCountdownTime) {
                            countdownTime = 0
                            resetCountdownTime = false
                        }
                        millisPassed = 0
                        resetMillisPassed = false
                    } else {
                        updateTime()
                    }
                    throw e
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.64f).aspectRatio(1f),
            shape = CircleShape,
            elevation = 4.dp
        ) {
            Countdown(state)
        }
    }
}

@Composable
private fun Countdown(state: AppState) {
    BoxWithConstraints(contentAlignment = Alignment.Center) {
        val alpha by produceState(1f) {
            while (true) {
                delay(500)
                value = 1 - value
            }
        }

        val color = MaterialTheme.colors.primary
        val fraction by produceState(0f, state.isCountingDown, state.updateProgress) {
            value = if (state.countdownTime == 0) 0f else state.progress
            if (state.isCountingDown) {
                val millisFraction = 1f / state.countdownTime / 1000
                while (true) {
                    withFrameMillis {
                        val currentTime = System.currentTimeMillis()
                        val progress =
                            state.progress + (currentTime - state.updatedTime) * millisFraction
                        value = progress.coerceAtMost(1f)
                    }
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize().alpha(if (fraction < 1) 1f else alpha)) {
            val width = ProgressStrokeWidth.toPx()
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = -360f * fraction,
                useCenter = false,
                topLeft = Offset(width, width),
                size = Size(size.width - width * 2, size.height - width * 2),
                style = Stroke(width = width, cap = StrokeCap.Round)
            )
        }

        val sign = if (state.currentTime < 0) "-" else ""
        val flicker = !state.isCountingDown && state.millisPassed != 0
        Text(
            text = sign + "%d:%02d:%02d".format(*state.timeSegments.toTypedArray()),
            modifier = Modifier.alpha(if (flicker) alpha else 1f),
            style = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = with(LocalDensity.current) {
                    (constraints.maxWidth * 0.18f).toSp()
                },
                letterSpacing = 0.sp
            )
        )
    }
}

private val ProgressStrokeWidth = 6.dp
