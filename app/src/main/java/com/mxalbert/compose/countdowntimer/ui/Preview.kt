package com.mxalbert.compose.countdowntimer.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import com.mxalbert.compose.countdowntimer.ui.theme.MyTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

@Composable
fun Preview(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MyTheme(darkTheme = darkTheme) {
        ProvideWindowInsets {
            Surface(color = MaterialTheme.colors.background, content = content)
        }
    }
}
