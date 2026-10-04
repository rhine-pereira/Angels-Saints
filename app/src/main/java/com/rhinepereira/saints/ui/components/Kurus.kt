package com.rhinepereira.saints.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rhinepereira.saints.R
import com.rhinepereira.saints.data.calendar.Season
import com.rhinepereira.saints.ui.theme.Kurus

/** The one app bar: limewash ground, indigo ink, signpainted title. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KurusTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                title,
                style = if (onBack == null) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            }
        },
        actions = { actions() },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/** Hairline indigo trim, like the painted edge of a cross's plinth. */
@Composable
fun TrimRule(modifier: Modifier = Modifier, color: Color = Kurus.colors.trim) {
    Canvas(modifier.fillMaxWidth().height(1.5.dp)) {
        drawLine(color, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), size.height)
    }
}

/**
 * Season trim band: each season has its own pattern so colour is never the only signal.
 * Advent dashes, Christmas double line, Lent dots, Triduum thick solid, Easter long dashes, Ordinary solid.
 */
@Composable
fun SeasonTrim(season: Season, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(8.dp)) {
        val y = size.height / 2
        val w = size.width
        val px = 1.dp.toPx()
        when (season) {
            Season.ADVENT -> drawLine(color, Offset(0f, y), Offset(w, y), 4 * px,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14 * px, 6 * px)))
            Season.CHRISTMAS -> {
                drawLine(color, Offset(0f, y - 2.5f * px), Offset(w, y - 2.5f * px), 2 * px)
                drawLine(color, Offset(0f, y + 2.5f * px), Offset(w, y + 2.5f * px), 2 * px)
            }
            Season.LENT -> drawLine(color, Offset(2 * px, y), Offset(w, y), 4 * px, cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.01f, 9 * px)))
            Season.PASCHAL_TRIDUUM -> drawLine(color, Offset(0f, y), Offset(w, y), 7 * px)
            Season.EASTER -> drawLine(color, Offset(0f, y), Offset(w, y), 4 * px,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(32 * px, 6 * px)))
            Season.ORDINARY_TIME -> drawLine(color, Offset(0f, y), Offset(w, y), 4 * px)
        }
    }
}

@Composable
fun seasonName(season: Season): String = stringResource(
    when (season) {
        Season.ADVENT -> R.string.season_advent
        Season.CHRISTMAS -> R.string.season_christmas
        Season.LENT -> R.string.season_lent
        Season.PASCHAL_TRIDUUM -> R.string.season_triduum
        Season.EASTER -> R.string.season_easter
        Season.ORDINARY_TIME -> R.string.season_ordinary
    }
)
