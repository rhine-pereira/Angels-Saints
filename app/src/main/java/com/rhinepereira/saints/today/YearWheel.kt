package com.rhinepereira.saints.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rhinepereira.saints.data.calendar.Season
import com.rhinepereira.saints.data.calendar.SeasonSpan
import com.rhinepereira.saints.data.calendar.Vestment
import com.rhinepereira.saints.ui.theme.Kurus
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** The colour a season wears on the ring. */
fun Season.vestment(): Vestment = when (this) {
    Season.ADVENT, Season.LENT -> Vestment.PURPLE
    Season.CHRISTMAS, Season.EASTER -> Vestment.WHITE
    Season.PASCHAL_TRIDUUM -> Vestment.RED
    Season.ORDINARY_TIME -> Vestment.GREEN
}

/**
 * The liturgical year as a ring, January 1 at twelve o'clock, clockwise.
 * Drag to scrub days (haptic tick per day); tap a season arc to ask about it.
 */
@Composable
fun YearWheel(
    spans: List<SeasonSpan>,
    daysInYear: Int,
    selectedDay: Int,
    todayDay: Int?,
    description: String,
    onSelect: (dayOfYear: Int) -> Unit,
    onSeasonTap: (SeasonSpan) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val kurus = Kurus.colors
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val currentSelected by rememberUpdatedState(selectedDay)
    val currentSpans by rememberUpdatedState(spans)

    fun dayAt(pos: Offset, size: Size): Int {
        val angle = atan2(pos.y - size.height / 2, pos.x - size.width / 2) + PI / 2
        val turn = ((angle / (2 * PI)) % 1.0 + 1.0) % 1.0
        return (turn * daysInYear).toInt().coerceIn(0, daysInYear - 1) + 1
    }

    // Knob centre in the Box's coordinates; mirrors the geometry drawn below.
    fun knobAt(day: Int, box: Size): Offset {
        val inset = 12.dp.value * density.density
        val radius = (box.minDimension - 2 * inset) / 2 - (22 + 12) / 2f * density.density
        val a = ((day - 0.5f) * 360f / daysInYear - 90f) * (PI / 180).toFloat()
        return Offset(box.width / 2 + radius * cos(a), box.height / 2 + radius * sin(a))
    }

    Box(
        modifier
            .fillMaxWidth()
            .widthIn(max = 340.dp)
            .aspectRatio(1f)
            .semantics { contentDescription = description }
            .pointerInput(daysInYear) {
                // Only a press on the knob scrubs; everywhere else the page keeps scrolling.
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val box = Size(size.width.toFloat(), size.height.toFloat())
                    if ((down.position - knobAt(currentSelected, box)).getDistance() > 36.dp.toPx()) return@awaitEachGesture
                    down.consume()
                    drag(down.id) { change ->
                        change.consume()
                        val d = dayAt(change.position, box)
                        if (d != currentSelected) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelect(d)
                        }
                    }
                }
            }
            .pointerInput(daysInYear) {
                detectTapGestures { pos ->
                    val r = hypot(pos.x - size.width / 2f, pos.y - size.height / 2f)
                    if (r > size.width * 0.32f) {
                        val d = dayAt(pos, Size(size.width.toFloat(), size.height.toFloat()))
                        currentSpans.firstOrNull { d in it.start.dayOfYear..it.end.dayOfYear }?.let(onSeasonTap)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val ring = 22.dp.toPx()
            val radius = size.minDimension / 2 - ring / 2 - 6.dp.toPx()
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)
            val perDay = 360f / daysInYear
            val gap = 0.6f

            // Season arcs
            spans.forEach { s ->
                val start = (s.start.dayOfYear - 1) * perDay - 90f
                val sweep = (s.end.dayOfYear - s.start.dayOfYear + 1) * perDay
                drawArc(
                    color = kurus.vestment(s.season.vestment()),
                    startAngle = start + gap / 2,
                    sweepAngle = (sweep - gap).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(ring, cap = StrokeCap.Butt)
                )
            }

            fun pointAt(day: Int, r: Float): Offset {
                val a = ((day - 0.5f) * perDay - 90f) * (PI / 180).toFloat()
                return Offset(center.x + r * cos(a), center.y + r * sin(a))
            }

            // Month ticks, inside the ring
            val monthStarts = intArrayOf(1, 32, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335)
                .map { if (daysInYear == 366 && it > 59) it + 1 else it }
            monthStarts.forEach { d ->
                drawLine(
                    scheme.outline,
                    pointAt(d, radius - ring / 2 - 4.dp.toPx()),
                    pointAt(d, radius - ring / 2 - 10.dp.toPx()),
                    1.5.dp.toPx()
                )
            }

            // Today: a marigold garland tick across the ring
            todayDay?.let { d ->
                drawLine(
                    kurus.marigold,
                    pointAt(d, radius - ring / 2 - 2.dp.toPx()),
                    pointAt(d, radius + ring / 2 + 6.dp.toPx()),
                    4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Selected day: an indigo knob sitting on the ring
            val knob = pointAt(selectedDay, radius)
            drawCircle(scheme.surface, radius = ring * 0.62f, center = knob)
            drawCircle(scheme.primary, radius = ring * 0.62f, center = knob, style = Stroke(3.dp.toPx()))
            drawCircle(scheme.primary, radius = ring * 0.22f, center = knob)
        }
        content()
    }
}
