package com.rhinepereira.saints.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rhinepereira.saints.R
import com.rhinepereira.saints.data.bible.ReadingPlate
import com.rhinepereira.saints.data.bible.ReadingsSource
import com.rhinepereira.saints.data.calendar.LiturgicalDay
import com.rhinepereira.saints.data.calendar.Rank
import com.rhinepereira.saints.data.calendar.Season
import com.rhinepereira.saints.data.calendar.SeasonSpan
import com.rhinepereira.saints.data.calendar.Vestment
import com.rhinepereira.saints.data.model.UiError
import com.rhinepereira.saints.ui.components.ErrorState
import com.rhinepereira.saints.ui.components.KurusTopBar
import com.rhinepereira.saints.ui.components.SeasonTrim
import com.rhinepereira.saints.ui.components.TrimRule
import com.rhinepereira.saints.ui.components.rememberNotificationGate
import com.rhinepereira.saints.ui.components.seasonName
import com.rhinepereira.saints.ui.theme.Kurus
import com.rhinepereira.saints.ui.theme.ReadingText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    locale: Locale,
    onToggleLanguage: () -> Unit,
    onOpenNameDays: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val askNotifications = rememberNotificationGate(snackbar, askOnFirstLaunch = true)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var explaining by remember { mutableStateOf<SeasonSpan?>(null) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            KurusTopBar(stringResource(R.string.brand), scrollBehavior = scrollBehavior) {
                IconButton(onClick = onToggleLanguage) {
                    Icon(Icons.Outlined.Translate, stringResource(R.string.action_language))
                }
                IconButton(onClick = onOpenNameDays) {
                    Icon(Icons.Outlined.Cake, stringResource(R.string.action_namedays))
                }
                IconButton(onClick = askNotifications) {
                    Icon(Icons.Outlined.Notifications, stringResource(R.string.action_notifications))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 40.dp
            )
        ) {
            item {
                DateHeader(
                    date = state.selected,
                    isToday = state.selected == state.today,
                    locale = locale,
                    onShift = viewModel::shift,
                    onBackToToday = viewModel::backToToday
                )
            }
            item { SeasonLine(state.day) }
            items(state.nameDays.size) { i ->
                val n = state.nameDays[i]
                NameDayBanner(n.person, n.saint)
            }
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    YearWheel(
                        spans = state.spans,
                        daysInYear = state.selected.lengthOfYear(),
                        selectedDay = state.selected.dayOfYear,
                        todayDay = state.today.dayOfYear.takeIf { state.today.year == state.selected.year },
                        description = stringResource(R.string.wheel_description),
                        onSelect = { viewModel.select(state.selected.withDayOfYear(it)) },
                        onSeasonTap = { explaining = it },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        WheelCenter(state.selected, state.nextSeason, locale)
                    }
                }
            }
            item { FeastPlaque(state.day) }
            item {
                Text(
                    stringResource(R.string.readings_title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 8.dp)
                        .semantics { heading() }
                )
                TrimRule(Modifier.padding(horizontal = 24.dp))
            }
            when (val r = state.readings) {
                ReadingsUi.Loading -> item {
                    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                ReadingsUi.None -> item { QuietNote(stringResource(R.string.readings_none)) }
                is ReadingsUi.Failed -> item {
                    if (r.error == UiError.Network) {
                        QuietNote(stringResource(R.string.readings_offline)) {
                            OutlinedButton(onClick = viewModel::retryReadings) { Text(stringResource(R.string.action_retry)) }
                        }
                    } else {
                        ErrorState(r.error, viewModel::retryReadings, Modifier.heightIn(max = 320.dp))
                    }
                }
                is ReadingsUi.Ready -> {
                    itemsIndexed(r.readings.plates) { i, plate -> ReadingPlateView(i + 1, plate) }
                    if (r.readings.source == ReadingsSource.DOUAY_RHEIMS) {
                        item { QuietNote(stringResource(R.string.readings_source_drb)) }
                    }
                }
            }
        }
    }

    explaining?.let { span ->
        ModalBottomSheet(onDismissRequest = { explaining = null }) {
            SeasonExplainer(span, locale)
        }
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    isToday: Boolean,
    locale: Locale,
    onShift: (Long) -> Unit,
    onBackToToday: () -> Unit
) {
    val ghost = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    Column(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            GhostNumeral(date.minusDays(1).dayOfMonth, ghost, stringResource(R.string.previous_day)) { onShift(-1) }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = 140.dp)) {
                Text(
                    date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Marigold garland under today; nothing under any other day.
                Box(
                    Modifier
                        .width(72.dp)
                        .height(6.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(if (isToday) Kurus.colors.marigold else ghost.copy(alpha = 0f))
                )
            }
            GhostNumeral(date.plusDays(1).dayOfMonth, ghost, stringResource(R.string.next_day)) { onShift(1) }
        }
        Text(
            date.format(DateTimeFormatter.ofPattern("EEEE · d MMMM yyyy", locale)),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
        if (!isToday) {
            AssistChip(
                onClick = onBackToToday,
                label = { Text(stringResource(R.string.back_to_today)) },
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun GhostNumeral(n: Int, color: androidx.compose.ui.graphics.Color, label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .sizeIn(minWidth = 72.dp, minHeight = 72.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(n.toString(), style = MaterialTheme.typography.displayMedium, color = color)
    }
}

@Composable
private fun SeasonLine(day: LiturgicalDay?) {
    if (day == null) {
        QuietNote(stringResource(R.string.calendar_missing))
        return
    }
    val colour = Kurus.colors.vestment(day.vestment)
    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        SeasonTrim(day.season, colour)
        Spacer(Modifier.height(8.dp))
        val name = seasonName(day.season)
        Text(
            if (day.week > 0) stringResource(R.string.season_week, name, day.week) else name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NameDayBanner(person: String, saint: String) {
    Column(
        Modifier
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            stringResource(R.string.nameday_greeting, person),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            stringResource(R.string.nameday_patron, saint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun WheelCenter(selected: LocalDate, next: LiturgicalDay?, locale: Locale) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(max = 168.dp)) {
        Text(
            selected.month.getDisplayName(TextStyle.FULL, locale),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        if (next != null) {
            val days = ChronoUnit.DAYS.between(selected, next.date).toInt()
            Spacer(Modifier.height(6.dp))
            Text(
                pluralStringResource(R.plurals.countdown_days, days, days, seasonName(next.season)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeastPlaque(day: LiturgicalDay?) {
    day ?: return
    Column(
        Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .fillMaxWidth()
            .border(BorderStroke(1.5.dp, Kurus.colors.trim), MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(day.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.rank_and_colour, rankName(day.rank), vestmentName(day.vestment)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (day.others.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.also_today, day.others.joinToString(" · ")),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private val ROMAN = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X")

@Composable
private fun ReadingPlateView(n: Int, plate: ReadingPlate) {
    val rubric = Kurus.colors.rubric
    val responseLabel = stringResource(R.string.label_response)
    val acclamationLabel = stringResource(R.string.label_acclamation)
    val rubrics = listOf("Acclamation", "Response", "घोषणा", "प्रतिसाद")

    @Composable
    fun Rubric(label: String, text: String) = Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = rubric, fontWeight = FontWeight.Bold)) { append("$label  ") }
            append(text)
        },
        style = ReadingText
    )

    Row(Modifier.padding(start = 16.dp, end = 24.dp, top = 28.dp)) {
        Text(
            ROMAN.getOrElse(n - 1) { n.toString() },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp)
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column {
                Text(plate.kind, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                Text(plate.reference, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            }
            plate.response?.let { Rubric(responseLabel, it) }
            plate.verses.forEach { verse ->
                val match = rubrics.firstOrNull { verse.trimStart().startsWith(it, ignoreCase = true) }
                if (match != null) {
                    Rubric(match, verse.trimStart().substring(match.length).trimStart(':', ' '))
                } else {
                    val num = Regex("""^(\d+)\s+""").find(verse)
                    Text(
                        buildAnnotatedString {
                            if (num != null) {
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)) {
                                    append(num.groupValues[1])
                                }
                                append("  ")
                                append(verse.substring(num.range.last + 1))
                            } else {
                                append(verse)
                            }
                        },
                        style = ReadingText
                    )
                }
            }
            plate.acclamation?.let { Rubric(acclamationLabel, it) }
        }
    }
}

@Composable
private fun QuietNote(text: String, action: (@Composable () -> Unit)? = null) {
    Column(Modifier.padding(horizontal = 24.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        action?.invoke()
    }
}

@Composable
private fun SeasonExplainer(span: SeasonSpan, locale: Locale) {
    val fmt = DateTimeFormatter.ofPattern("d MMMM", locale)
    Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(seasonName(span.season), style = MaterialTheme.typography.headlineLarge)
        SeasonTrim(span.season, Kurus.colors.vestment(span.season.vestment()))
        Text(
            "${span.start.format(fmt)} – ${span.end.format(fmt)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            stringResource(
                when (span.season) {
                    Season.ADVENT -> R.string.season_about_advent
                    Season.CHRISTMAS -> R.string.season_about_christmas
                    Season.LENT -> R.string.season_about_lent
                    Season.PASCHAL_TRIDUUM -> R.string.season_about_triduum
                    Season.EASTER -> R.string.season_about_easter
                    Season.ORDINARY_TIME -> R.string.season_about_ordinary
                }
            ),
            style = ReadingText
        )
    }
}

@Composable
private fun rankName(rank: Rank) = stringResource(
    when (rank) {
        Rank.SOLEMNITY -> R.string.rank_solemnity
        Rank.SUNDAY -> R.string.rank_sunday
        Rank.FEAST -> R.string.rank_feast
        Rank.MEMORIAL -> R.string.rank_memorial
        Rank.OPTIONAL_MEMORIAL -> R.string.rank_optional_memorial
        Rank.WEEKDAY -> R.string.rank_weekday
    }
)

@Composable
private fun vestmentName(v: Vestment) = stringResource(
    when (v) {
        Vestment.GREEN -> R.string.vest_green
        Vestment.PURPLE -> R.string.vest_purple
        Vestment.WHITE -> R.string.vest_white
        Vestment.RED -> R.string.vest_red
        Vestment.ROSE -> R.string.vest_rose
        Vestment.BLACK -> R.string.vest_black
        Vestment.GOLD -> R.string.vest_gold
    }
)
