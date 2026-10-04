package com.rhinepereira.saints.nameday

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.rhinepereira.saints.R
import com.rhinepereira.saints.data.calendar.Saint
import com.rhinepereira.saints.data.nameday.NameDay
import com.rhinepereira.saints.data.nameday.NameDayStore
import com.rhinepereira.saints.ui.components.KurusTopBar
import com.rhinepereira.saints.ui.components.TrimRule
import com.rhinepereira.saints.ui.components.rememberNotificationGate
import com.rhinepereira.saints.ui.theme.Kurus
import java.text.Normalizer
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun String.fold(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")

/** Saints whose name contains every word typed, e.g. "francis" → Francis of Assisi, Francis Xavier, … */
fun matchSaints(saints: List<Saint>, query: String): List<Saint> {
    val words = query.fold().split(' ').filter { it.length >= 2 }
    if (words.isEmpty()) return emptyList()
    return saints.filter { s -> words.all { it in s.name.fold() } }.take(30)
}

@Composable
fun NameDayScreen(
    store: NameDayStore,
    saints: List<Saint>,
    locale: Locale,
    onBack: () -> Unit
) {
    val saved by store.all.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val askNotifications = rememberNotificationGate(snackbar)
    var person by rememberSaveable { mutableStateOf("") }
    var patron by rememberSaveable { mutableStateOf("") }
    val query = patron.ifBlank { person }
    val matches = remember(query, saints) { matchSaints(saints, query) }
    val dateFmt = remember(locale) { DateTimeFormatter.ofPattern("d MMMM", locale) }
    val fieldColors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Kurus.colors.trim)

    Scaffold(
        topBar = { KurusTopBar(stringResource(R.string.nameday_title), onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.nameday_intro),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (saved.isEmpty()) item {
                Text(stringResource(R.string.nameday_empty), style = MaterialTheme.typography.bodyMedium)
            }
            items(saved) { n ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(n.person, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${n.saint} · ${MonthDay.of(n.month, n.day).format(dateFmt)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { store.remove(n) }) {
                        Icon(Icons.Outlined.Delete, stringResource(R.string.nameday_remove, n.person))
                    }
                }
            }
            item { TrimRule(Modifier.padding(vertical = 8.dp)) }
            item {
                OutlinedTextField(
                    value = person,
                    onValueChange = { person = it },
                    label = { Text(stringResource(R.string.nameday_person)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = patron,
                    onValueChange = { patron = it },
                    label = { Text(stringResource(R.string.nameday_saint)) },
                    placeholder = { Text(stringResource(R.string.nameday_saint_hint)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (person.isNotBlank() && query.length >= 2) {
                item {
                    Text(
                        if (matches.isEmpty()) stringResource(R.string.nameday_no_match)
                        else stringResource(R.string.nameday_pick, person.trim()),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                items(matches) { s ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                store.add(NameDay(person.trim(), s.title, s.month, s.day))
                                person = ""
                                patron = ""
                                askNotifications()
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(s.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            MonthDay.of(s.month, s.day).format(dateFmt),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
