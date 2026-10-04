package com.rhinepereira.saints.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.rhinepereira.saints.AppLanguage
import com.rhinepereira.saints.R
import com.rhinepereira.saints.ui.components.ContentBlockRenderer
import com.rhinepereira.saints.ui.components.ErrorState
import com.rhinepereira.saints.ui.components.KurusTopBar
import com.rhinepereira.saints.ui.components.LoadingState
import com.rhinepereira.saints.ui.components.MessageState
import com.rhinepereira.saints.ui.components.TrimRule

@Composable
fun ContentScreen(viewModel: ContentViewModel, language: AppLanguage, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val content = state.content

    Scaffold(topBar = { KurusTopBar(content?.title.orEmpty(), onBack = onBack) }) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when {
            state.loading -> LoadingState(modifier)
            state.error != null -> ErrorState(state.error!!, viewModel::load, modifier)
            content != null -> {
                val versions = content.versions.orEmpty()
                val preferred = versions.indexOfFirst {
                    val l = it.label.orEmpty().lowercase()
                    if (language == AppLanguage.MARATHI) "marathi" in l || "मराठी" in l else "english" in l
                }.coerceAtLeast(0)
                var tab by rememberSaveable(versions.size, language) { mutableIntStateOf(preferred) }
                val blocks = if (versions.isNotEmpty()) versions[tab.coerceIn(versions.indices)].blocks.orEmpty()
                else content.blocks.orEmpty()
                val image = content.imageUrl?.trim()?.takeIf { it.isNotBlank() && viewModel.categoryId != "prayers" }
                var showImage by remember(image) { mutableStateOf(image != null) }

                LazyColumn(modifier, contentPadding = PaddingValues(bottom = 40.dp)) {
                    if (showImage) item {
                        AsyncImage(
                            model = image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            onState = { if (it is AsyncImagePainter.State.Error) showImage = false },
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth()
                                .heightIn(max = 360.dp)
                                .clip(MaterialTheme.shapes.small)
                        )
                    }
                    item {
                        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)) {
                            Text(content.title.orEmpty(), style = MaterialTheme.typography.headlineLarge)
                            TrimRule(Modifier.padding(top = 12.dp))
                        }
                    }
                    if (versions.size > 1) item {
                        PrimaryTabRow(
                            selectedTabIndex = tab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            versions.forEachIndexed { i, v ->
                                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(v.label.orEmpty()) })
                            }
                        }
                    }
                    if (blocks.isEmpty()) item {
                        MessageState(
                            stringResource(R.string.content_empty_title),
                            stringResource(R.string.content_empty_body),
                            Modifier.heightIn(max = 360.dp)
                        )
                    }
                    items(blocks) { block ->
                        Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ContentBlockRenderer(block)
                        }
                    }
                }
            }
        }
    }
}
