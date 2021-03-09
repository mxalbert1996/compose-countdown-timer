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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastSumBy
import com.mxalbert.compose.countdowntimer.AppState
import com.mxalbert.compose.countdowntimer.DigitCount
import com.mxalbert.compose.countdowntimer.R
import com.mxalbert.compose.countdowntimer.SegmentCount
import com.mxalbert.compose.countdowntimer.rememberAppState
import com.mxalbert.compose.countdowntimer.setTimeFrom
import kotlin.math.min
import kotlin.math.roundToInt

class EditScreenState(
    editingSegment: Int = -1,
    segments: Collection<Int> = List(SegmentCount) { 0 }
) {
    var editingSegment by mutableStateOf(editingSegment)

    // This is against SSOT principle but is here to support inputting minute/second greater than 60
    val segments: MutableList<Int> = mutableStateListOf<Int>().apply { addAll(segments) }
}

private val StateSaver: Saver<EditScreenState, *> = listSaver(
    save = { listOf(it.editingSegment) + it.segments },
    restore = {
        EditScreenState(
            editingSegment = it[0],
            segments = it.subList(1, it.size)
        )
    }
)

val EditScreenState.digits: MutableList<Int>
    get() = mutableListOf<Int>().also { list ->
        segments.flatMapTo(list) { listOf(it / 10, it % 10) }
    }

fun EditScreenState.setSegmentsFrom(digits: List<Int>) {
    for (i in segments.indices) {
        segments[i] = digits[i * 2] * 10 + digits[i * 2 + 1]
    }
}

fun EditScreenState.clearSegments() {
    for (i in segments.indices) {
        segments[i] = 0
    }
}

@Composable
fun EditScreen(state: AppState) {
    val screenState = rememberSaveable(saver = StateSaver) { EditScreenState() }
    val widths = remember { IntArray(3) }
    SubcomposeLayout { constraints ->
        val isPortrait = constraints.maxHeight >= constraints.maxWidth
        val relaxedConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val time = subcompose(Component.Time) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                screenState.segments.forEachIndexed { i, digit ->
                    if (i > 0) TimeDivider()
                    TimeSegment(screenState, i, digit) { widths[i] = it }
                }
            }
        }.first().measure(relaxedConstraints)
        val narrowConstraints = relaxedConstraints.copy(maxWidth = time.width)

        val labels = subcompose(Component.Labels) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Labels.forEachIndexed { i, label ->
                        Text(
                            text = stringResource(label),
                            modifier = Modifier.width(widths[i].toDp()),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                }
            }
        }.first().measure(narrowConstraints)

        val buttonPadConstraints = if (isPortrait) narrowConstraints else
            relaxedConstraints.copy(maxWidth = (constraints.maxWidth / 2 * 0.8f).roundToInt())
        val buttonPad = subcompose(Component.ButtonPad) {
            ButtonPad(state, screenState, buttonPadConstraints)
        }.fastMap { it.measure(buttonPadConstraints) }
        val buttonPadHeight = buttonPad.fastSumBy { it.height }

        layout(constraints.maxWidth, constraints.maxHeight) {
            if (isPortrait) {
                val width = time.width
                val x = (constraints.maxWidth - width) / 2
                val contentHeight = time.height + labels.height + buttonPadHeight
                val margin = (constraints.maxHeight - contentHeight) / 3
                var y = margin
                time.place(x, y)
                y += time.height
                labels.place(x, y)
                y += labels.height + margin
                buttonPad.fastForEach {
                    it.place(x, y)
                    y += it.height
                }
            } else {
                val halfWidth = constraints.maxWidth / 2
                val height = constraints.maxHeight
                val margin = ((height - time.height - labels.height) * 0.4).roundToInt()
                var x = ((halfWidth - time.width) * 0.75).roundToInt()
                time.place(x, margin)
                labels.place(x, margin + time.height)
                x = (halfWidth + (halfWidth - buttonPadConstraints.maxWidth) * 0.25).roundToInt()
                var y = ((height - buttonPadHeight) * 0.6).roundToInt()
                buttonPad.fastForEach {
                    it.place(x, y)
                    y += it.height
                }
            }
        }
    }
}

private enum class Component { Time, Labels, ButtonPad }

@Composable
private fun TimeSegment(
    screenState: EditScreenState,
    index: Int,
    digit: Int,
    onLayout: (width: Int) -> Unit
) {
    Text(
        text = "%02d".format(digit),
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                screenState.editingSegment = index
            }
        ),
        color = if (screenState.editingSegment.let { it == index || it == -1 })
            MaterialTheme.colors.primary else Color.Unspecified,
        onTextLayout = { onLayout(it.size.width) },
        style = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 68.sp
        )
    )
}

@Composable
private fun TimeDivider() {
    Text(
        text = ":",
        color = MaterialTheme.colors.primary,
        style = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    )
}

@Composable
private fun Density.ButtonPad(
    state: AppState,
    screenState: EditScreenState,
    constraints: Constraints
) {
    val buttonSize = min(64f, (constraints.maxHeight / 4).toDp().value).dp
    for (i in 0 until 4) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (j in 0 until 3) {
                if (i == 3 && j == 0) {
                    Spacer(modifier = Modifier.size(buttonSize))
                } else if (i == 3 && j == 2) {
                    RoundButton(
                        size = buttonSize,
                        onLongClick = { screenState.onBackspaceLongPressed(state) },
                        onClick = { screenState.onBackspacePressed(state) }
                    ) {
                        Icon(
                            Icon.Backspace.image,
                            contentDescription = stringResource(Icon.Backspace.contentDescription),
                            modifier = Modifier.size(28.sp.toDp())
                        )
                    }
                } else {
                    val digit = if (i == 3) 0 else i * 3 + j + 1
                    RoundButton(
                        size = buttonSize,
                        onClick = { screenState.onDigitPressed(state, digit) }
                    ) {
                        Text(
                            text = digit.toString(),
                            style = MaterialTheme.typography.h4
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoundButton(
    size: Dp,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier.size(size).combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(
                bounded = false,
                radius = size * 0.6f
            ),
            role = Role.Button,
            onLongClick = onLongClick,
            onClick = onClick
        ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

private fun EditScreenState.calculateIndices(digits: List<Int>): Triple<Int, Int, Int> {
    val leftMostIndex = if (editingSegment >= 0) editingSegment * 2 else 0
    val rightMostIndex = if (editingSegment >= 0) leftMostIndex + 1 else DigitCount - 1
    var index = leftMostIndex
    while (index <= rightMostIndex && digits[index] == 0) index++
    return Triple(leftMostIndex, rightMostIndex, index)
}

private fun EditScreenState.onDigitPressed(state: AppState, digit: Int) {
    val digits = digits
    val (leftMostIndex, rightMostIndex, index) = calculateIndices(digits)
    if (index > leftMostIndex) {
        for (i in index - 1 until rightMostIndex) {
            digits[i] = digits[i + 1]
        }
        digits[rightMostIndex] = digit
        setSegmentsFrom(digits)
        state.setTimeFrom(segments)
    }
}

private fun EditScreenState.onBackspacePressed(state: AppState) {
    val digits = digits
    val (_, rightMostIndex, index) = calculateIndices(digits)
    if (index <= rightMostIndex) {
        for (i in rightMostIndex downTo index + 1) {
            digits[i] = digits[i - 1]
        }
        digits[index] = 0
        setSegmentsFrom(digits)
        state.setTimeFrom(segments)
    }
    if (state.countdownTime == 0) editingSegment = -1
}

private fun EditScreenState.onBackspaceLongPressed(state: AppState) {
    clearSegments()
    state.countdownTime = 0
    editingSegment = -1
}

private val Labels = intArrayOf(
    R.string.hour,
    R.string.minute,
    R.string.second
)

@Preview(widthDp = 360, heightDp = 640)
@Composable
fun EditScreenPreview() {
    Preview {
        EditScreen(state = rememberAppState())
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
fun EditScreenDarkPreview() {
    Preview(darkTheme = true) {
        EditScreen(state = rememberAppState())
    }
}
