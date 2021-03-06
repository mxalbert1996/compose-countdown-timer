package com.mxalbert.compose.countdowntimer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
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
import com.mxalbert.compose.countdowntimer.*
import com.mxalbert.compose.countdowntimer.R

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
                            Icon.Backspace.image,
                            contentDescription = stringResource(Icon.Backspace.contentDescription),
                            modifier = Modifier.size(28.sp.toDp())
                        )
                    }
                } else {
                    val digit = if (i == 3) 0 else i * 3 + j + 1
                    RoundButton(onClick = { screenState.onDigitPressed(state, digit) }) {
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

private val ButtonSize = 64.sp

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
