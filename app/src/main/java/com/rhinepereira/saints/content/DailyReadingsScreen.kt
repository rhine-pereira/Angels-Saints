package com.rhinepereira.saints.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.rhinepereira.saints.ui.components.ErrorState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReadingsScreen(
    viewModel: DailyReadingsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Logic: Allow navigating to tomorrow at most
    val canGoForward = remember(uiState.date) {
        val tomorrow = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        uiState.date.before(tomorrow.time)
    }

    val canGoBackward = remember(uiState.date) {
        val minDate = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 1, 0, 0, 0)
        }
        uiState.date.after(minDate.time)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Readings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            DateNavigationRow(
                date = uiState.date,
                showPrev = canGoBackward,
                showNext = canGoForward,
                onPrev = { viewModel.previousDay() },
                onNext = { viewModel.nextDay() }
            )

            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.error != null -> {
                        ErrorState(
                            error = uiState.error!!,
                            onRetry = { viewModel.retry() }
                        )
                    }
                    uiState.data != null -> {
                        val data = uiState.data!!
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            item {
                                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                    Text(
                                        text = data.title ?: "Daily Reading",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!data.feast.isNullOrBlank()) {
                                        Text(
                                            text = data.feast!!,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            items(data.readings ?: emptyList()) { reading ->
                                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                    // Removed type header completely
                                    
                                    Text(
                                        text = reading.heading ?: "",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = reading.reference ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                                    )
                                    
                                    reading.verses?.forEach { verse ->
                                        // Refined highlighting logic
                                        val annotatedVerse = buildAnnotatedString {
                                            val highlights = listOf("Acclamation", "Response", "घोषणा", "प्रतिसाद")
                                            var match: String? = null
                                            
                                            val trimmedVerse = verse.trim()
                                            highlights.forEach { keyword ->
                                                if (trimmedVerse.startsWith(keyword, ignoreCase = true)) {
                                                    match = keyword
                                                }
                                            }
                                            
                                            if (match != null) {
                                                withStyle(style = SpanStyle(
                                                    color = MaterialTheme.colorScheme.error,
                                                    fontWeight = FontWeight.Bold
                                                )) {
                                                    append(match!!)
                                                }
                                                // Append everything including punctuation after the match
                                                append(trimmedVerse.substring(match!!.length))
                                            } else {
                                                append(verse)
                                            }
                                        }

                                        Text(
                                            text = annotatedVerse,
                                            style = MaterialTheme.typography.bodyLarge,
                                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }

                                    // Highlighted Acclamation and Response
                                    if (!reading.acclamation.isNullOrBlank()) {
                                        HighlightedLiturgyText(
                                            label = if (uiState.language == "Marathi") "घोषणा" else "Acclamation",
                                            value = reading.acclamation
                                        )
                                    }

                                    if (!reading.response.isNullOrBlank()) {
                                        HighlightedLiturgyText(
                                            label = if (uiState.language == "Marathi") "प्रतिसाद" else "Response",
                                            value = reading.response
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightedLiturgyText(label: String, value: String) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )) {
            append("$label: ")
        }
        append(value)
    }
    
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun DateNavigationRow(
    date: Date,
    showPrev: Boolean,
    showNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showPrev) {
            IconButton(onClick = onPrev) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        val displayDate = remember(date) {
            SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(date)
        }

        Text(
            text = displayDate,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (showNext) {
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}
