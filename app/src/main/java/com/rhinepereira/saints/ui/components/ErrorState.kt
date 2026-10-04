package com.rhinepereira.saints.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rhinepereira.saints.R
import com.rhinepereira.saints.data.model.UiError

@Composable
fun ErrorState(
    error: UiError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (title, body) = when (error) {
        UiError.Network -> R.string.error_network_title to R.string.error_network_body
        UiError.NotFound -> R.string.error_notfound_title to R.string.error_notfound_body
        is UiError.Unknown -> R.string.error_unknown_title to R.string.error_unknown_body
    }
    MessageState(
        title = stringResource(title),
        body = stringResource(body),
        modifier = modifier,
        action = {
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
        }
    )
}

/** Calm, centred message: empty, offline and error states all share it. */
@Composable
fun MessageState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TrimRule(Modifier.widthIn(max = 48.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(
            body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 360.dp)
        )
        action?.invoke()
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
