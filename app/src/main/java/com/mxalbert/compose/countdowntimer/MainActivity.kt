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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mxalbert.compose.countdowntimer.ui.CountdownTimerApp
import com.mxalbert.compose.countdowntimer.ui.theme.MyTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import timber.log.Timber

class MainActivity : ComponentActivity() {
    companion object {
        init {
            if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
