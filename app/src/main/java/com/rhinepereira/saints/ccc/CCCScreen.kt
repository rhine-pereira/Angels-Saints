package com.rhinepereira.saints.ccc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rhinepereira.saints.R
import com.rhinepereira.saints.ui.components.ErrorState
import com.rhinepereira.saints.ui.components.KurusTopBar
import com.rhinepereira.saints.ui.components.LoadingState
import com.rhinepereira.saints.ui.components.MessageState
import com.rhinepereira.saints.ui.components.TrimRule
import com.rhinepereira.saints.ui.theme.Kurus
import com.rhinepereira.saints.ui.theme.ReadingText

@Composable
fun CCCScreen(viewModel: CCCViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = { KurusTopBar(stringResource(R.string.ccc_title)) }) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when {
            state.loading -> LoadingState(modifier)
            state.error != null -> ErrorState(state.error!!, viewModel::load, modifier)
            else -> LazyColumn(modifier, contentPadding = PaddingValues(bottom = 32.dp)) {
                item {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::search,
                        placeholder = { Text(stringResource(R.string.ccc_search_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.search("") }) {
                                    Icon(Icons.Default.Close, stringResource(R.string.action_clear))
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Kurus.colors.trim,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
                if (state.filtered.isEmpty()) item {
                    MessageState(
                        stringResource(R.string.ccc_title),
                        stringResource(R.string.ccc_no_results, state.query.trim())
                    )
                }
                items(state.filtered, key = { it.id }) { entry ->
                    Row(Modifier.padding(start = 12.dp, end = 24.dp, top = 20.dp, bottom = 20.dp)) {
                        Text(
                            entry.id.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(52.dp).padding(end = 12.dp, top = 4.dp)
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(entry.text, style = ReadingText)
                        }
                    }
                    TrimRule(Modifier.padding(start = 64.dp, end = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
