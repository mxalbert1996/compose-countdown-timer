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

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.mxalbert.compose.countdowntimer.ui.EditScreen
import com.mxalbert.compose.countdowntimer.ui.Preview
import com.mxalbert.compose.countdowntimer.ui.theme.MyTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import dev.chrisbanes.accompanist.insets.systemBarsPadding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStatusBar(resources.configuration)
        setContent {
            MyTheme {
                ProvideWindowInsets {
                    CountdownTimerApp()
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupStatusBar(newConfig)
    }

    private fun setupStatusBar(config: Configuration) {
        val isDarkMode =
            config.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = !isDarkMode
    }

}

@Composable
fun CountdownTimerApp() {
    val state = rememberAppState()
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

            EditScreen(state, modifier = Modifier.fillMaxHeight(fraction = 0.8f))

            Box(modifier = Modifier.fillMaxHeight()) {
                AnimatedFab(state)
            }
        }
    }
}

@Composable
private fun AnimatedFab(state: AppState) {
    with(LocalDensity.current) {
        val fabSize = 56.dp.toPx()
        val animatable = remember { Animatable(0f) }
        LaunchedEffect(state.fabIcon == null) {
            animatable.animateTo(if (state.fabIcon == null) 0f else fabSize)
        }

        val size = animatable.value.toDp()
        FloatingActionButton(
            onClick = state.onFabPressed,
            modifier = Modifier.padding((56.dp - size) / 2).size(size),
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Crossfade(state.fabIcon) { icon ->
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(R.string.start)
                    )
                }
            }
        }
    }
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
