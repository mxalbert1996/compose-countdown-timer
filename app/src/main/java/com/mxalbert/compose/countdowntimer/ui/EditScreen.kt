package com.mxalbert.compose.countdowntimer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastSumBy
import com.mxalbert.compose.countdowntimer.AppState
import com.mxalbert.compose.countdowntimer.DigitCount
import com.mxalbert.compose.countdowntimer.R
import com.mxalbert.compose.countdowntimer.rememberAppState
import timber.log.Timber

private const val LastIndex = DigitCount - 1

class EditScreenState(
    editingSegment: Int = -1
) {
    var editingSegment by mutableStateOf(editingSegment)
}

private val StateSaver: Saver<EditScreenState, *> = Saver(
    save = { it.editingSegment },
    restore = {
        EditScreenState(
            editingSegment = it
        )
    }
)

@Composable
fun EditScreen(state: AppState, modifier: Modifier = Modifier) {
    val screenState = rememberSaveable(saver = StateSaver) { EditScreenState() }
    val widths = remember { IntArray(3) }
    SubcomposeLayout(modifier = modifier) { constraints ->
        Timber.d("Constraints: $constraints")
        val relaxedConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val time = subcompose(Component.Time) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.timeSegments
                    .chunked(2) { it[0] * 10 + it[1] }
                    .forEachIndexed { i, number ->
                        if (i > 0) TimeDivider()
                        TimeSegment(screenState, i, number) { widths[i] = it }
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

        val buttonPad = subcompose(Component.ButtonPad) { ButtonPad(state, screenState) }
            .fastMap { it.measure(narrowConstraints) }

        val width = time.width
        val x = (constraints.maxWidth - width) / 2
        val contentHeight = time.height + labels.height + buttonPad.fastSumBy { it.height }
        val margin = (constraints.maxHeight - contentHeight) / 3

        layout(constraints.maxWidth, constraints.maxHeight) {
            var y = margin
            time.place(x, y)
            y += time.height
            labels.place(x, y)
            y += labels.height + margin
            buttonPad.fastForEach {
                it.place(x, y)
                y += it.height
            }
        }
    }
}

private enum class Component { Time, Labels, ButtonPad, Fab }

@Preview(widthDp = 360, heightDp = 640)
@Composable
fun EditScreenPreview() {
    Preview {
        EditScreen(state = rememberAppState())
    }
}

@Composable
private fun TimeSegment(
    screenState: EditScreenState,
    index: Int,
    number: Int,
    onLayout: (width: Int) -> Unit
) {
    Text(
        text = "%02d".format(number),
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
    screenState: EditScreenState
) {
    for (i in 0 until 4) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (j in 0 until 3) {
                if (i == 3 && j == 0) {
                    Spacer(modifier = Modifier.size(ButtonSize.toDp()))
                } else if (i == 3 && j == 2) {
                    RoundButton(
                        onLongClick = { screenState.onBackspaceLongPressed(state) },
                        onClick = { screenState.onBackspacePressed(state) }
                    ) {
                        Icon(
                            Icons.Outlined.Backspace,
                            contentDescription = stringResource(R.string.backspace),
                            modifier = Modifier.size(28.sp.toDp())
                        )
                    }
                } else {
                    val number = if (i == 3) 0 else i * 3 + j + 1
                    RoundButton(onClick = { screenState.onNumberPressed(state, number) }) {
                        Text(
                            text = number.toString(),
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
private fun Density.RoundButton(
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier.size(ButtonSize.toDp()).combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(
                bounded = false,
                radius = 40.sp.toDp()
            ),
            role = Role.Button,
            onLongClick = onLongClick,
            onClick = onClick
        ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

private fun EditScreenState.calculateIndices(state: AppState): Triple<Int, Int, Int> {
    val leftMostIndex = if (editingSegment >= 0) editingSegment * 2 else 0
    val rightMostIndex = if (editingSegment >= 0) leftMostIndex + 1 else LastIndex
    var index = leftMostIndex
    while (index <= rightMostIndex && state.timeSegments[index] == 0) index++
    return Triple(leftMostIndex, rightMostIndex, index)
}

private fun EditScreenState.onNumberPressed(state: AppState, number: Int) {
    val (leftMostIndex, rightMostIndex, index) = calculateIndices(state)
    if (index > leftMostIndex) {
        for (i in index - 1 until rightMostIndex) {
            state.timeSegments[i] = state.timeSegments[i + 1]
        }
        state.timeSegments[rightMostIndex] = number
        if (number > 0) {
            state.fabIcon = Icons.Filled.PlayArrow
        }
    }
}

private fun EditScreenState.onBackspacePressed(state: AppState) {
    val (_, rightMostIndex, index) = calculateIndices(state)
    if (index <= rightMostIndex) {
        for (i in rightMostIndex downTo index + 1) {
            state.timeSegments[i] = state.timeSegments[i - 1]
        }
        state.timeSegments[index] = 0
        if (state.timeSegments.all { it == 0 }) {
            editingSegment = -1
            state.fabIcon = null
        }
    }
}

private fun EditScreenState.onBackspaceLongPressed(state: AppState) {
    for (i in state.timeSegments.indices) {
        state.timeSegments[i] = 0
    }
    editingSegment = -1
    state.fabIcon = null
}

private val Labels = intArrayOf(
    R.string.hour,
    R.string.minute,
    R.string.second
)

private val ButtonSize = 64.sp
