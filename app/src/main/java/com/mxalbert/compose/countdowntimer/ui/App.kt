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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxalbert.compose.countdowntimer.AppState
import com.mxalbert.compose.countdowntimer.R
import com.mxalbert.compose.countdowntimer.Screen
import com.mxalbert.compose.countdowntimer.currentTime
import com.mxalbert.compose.countdowntimer.rememberAppState
import dev.chrisbanes.accompanist.insets.systemBarsPadding

@Composable
fun CountdownTimerApp(state: AppState = rememberAppState()) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier.systemBarsPadding().fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.h6
                )
            }

            Crossfade(state.screen, modifier = Modifier.fillMaxHeight(fraction = 0.8f)) {
                when (it) {
                    Screen.Edit -> EditScreen(state)
                    Screen.Timer -> TimerScreen(state)
                }
            }

            BottomButtonArea(state)
        }
    }
}

@Composable
private fun BottomButtonArea(state: AppState) {
    Row(
        modifier = Modifier.fillMaxWidth(fraction = 0.75f),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val showExtraButtons = state.screen == Screen.Timer
        val targetAlpha = if (showExtraButtons) 1f else 0f
        val alpha = remember { Animatable(targetAlpha) }
        LaunchedEffect(targetAlpha) {
            alpha.animateTo(targetAlpha)
        }

        IconButton(
            onClick = {
                state.millisPassed = 0
            },
            modifier = Modifier.alpha(alpha.value),
            enabled = showExtraButtons
        ) {
            Icon(
                imageVector = Icon.Reset.image,
                contentDescription = stringResource(Icon.Reset.contentDescription)
            )
        }
        AnimatedFab(state)
        IconButton(
            onClick = {
                state.isCountingDown = false
                state.resetMillisPassed = true
                state.resetCountdownTime = true
                state.screen = Screen.Edit
            },
            modifier = Modifier.alpha(alpha.value),
            enabled = showExtraButtons
        ) {
            Icon(
                imageVector = Icon.Delete.image,
                contentDescription = stringResource(Icon.Delete.contentDescription)
            )
        }
    }
}

@Composable
private fun AnimatedFab(state: AppState) {
    with(LocalDensity.current) {
        val fabSizePx = FabSize.toPx()
        val timeIsPositive = state.currentTime > 0
        val action = when (state.screen) {
            Screen.Edit -> if (timeIsPositive) FabAction.Start else null
            Screen.Timer -> if (!state.isCountingDown) FabAction.Resume else {
                if (timeIsPositive) FabAction.Pause else FabAction.Stop
            }
        }
        val icon = action?.icon
        val animatable = remember { Animatable(0f) }
        LaunchedEffect(icon == null) {
            animatable.animateTo(if (icon == null) 0f else fabSizePx)
        }

        val size = animatable.value.toDp()
        FloatingActionButton(
            onClick = { action?.execute(state) },
            modifier = Modifier.padding((FabSize - size) / 2).size(size),
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Crossfade(icon) { icon ->
                if (icon != null) {
                    Icon(
                        imageVector = icon.image,
                        contentDescription = stringResource(icon.contentDescription)
                    )
                }
            }
        }
    }
}

private val FabSize = 56.dp

private enum class FabAction(val icon: Icon) {
    Start(Icon.Start) {
        override fun execute(state: AppState) {
            state.screen = Screen.Timer
            state.isCountingDown = true
        }
    },

    Resume(Icon.Start) {
        override fun execute(state: AppState) {
            state.isCountingDown = true
        }
    },

    Pause(Icon.Pause) {
        override fun execute(state: AppState) {
            state.isCountingDown = false
        }
    },

    Stop(Icon.Stop) {
        override fun execute(state: AppState) {
            state.isCountingDown = false
            state.resetMillisPassed = true
        }
    };

    abstract fun execute(state: AppState)
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    Preview {
        CountdownTimerApp()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    Preview(darkTheme = true) {
        CountdownTimerApp()
    }
}
