package com.mxalbert.compose.countdowntimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
