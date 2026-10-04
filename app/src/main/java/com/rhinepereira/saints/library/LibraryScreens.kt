package com.rhinepereira.saints.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.rhinepereira.saints.R
import com.rhinepereira.saints.data.model.Category
import com.rhinepereira.saints.data.model.Item
import com.rhinepereira.saints.data.model.UiError
import com.rhinepereira.saints.data.repository.ContentRepository
import com.rhinepereira.saints.data.repository.isLibrary
import com.rhinepereira.saints.ui.components.ErrorState
import com.rhinepereira.saints.ui.components.KurusTopBar
import com.rhinepereira.saints.ui.components.LoadingState
import com.rhinepereira.saints.ui.components.TrimRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Loading / failed / loaded, shared by the library lists. */
data class ListState<T>(val loading: Boolean = true, val title: String = "", val items: List<T> = emptyList(), val error: UiError? = null)

class LibraryViewModel(private val repo: ContentRepository) : ViewModel() {
    private val _state = MutableStateFlow(ListState<Category>())
    val state: StateFlow<ListState<Category>> = _state.asStateFlow()

    init { load(); viewModelScope.launch { repo.prefetchEverything() } }

    fun load() {
        _state.value = ListState()
        viewModelScope.launch {
            repo.getCategories()
                .catch { _state.value = ListState(loading = false, error = UiError.from(it)) }
                .collect { cats -> _state.value = ListState(loading = false, items = cats.filter { it.isLibrary }) }
        }
    }
}

class CategoryViewModel(private val repo: ContentRepository, private val categoryId: String) : ViewModel() {
    private val _state = MutableStateFlow(ListState<Item>())
    val state: StateFlow<ListState<Item>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = ListState()
        viewModelScope.launch {
            combine(repo.getCategories(), repo.getItemsByCategory(categoryId)) { cats, items ->
                ListState(loading = false, title = cats.find { it.id == categoryId }?.title.orEmpty(), items = items)
            }
                .catch { _state.value = ListState(loading = false, error = UiError.from(it)) }
                .collect { _state.value = it }
        }
    }
}

// Bundled art for instant first paint; remote imageUrl is the fallback.
private fun localArt(categoryId: String): Int? = when (categoryId) {
    "angels" -> R.drawable.angels_icon_1
    "saints" -> R.drawable.joseph_thumb
    "prayers" -> R.drawable.hands
    "child_saints" -> R.drawable.dominicsavio_thumb
    "apostles" -> R.drawable.peter_thumb
    else -> null
}

@Composable
fun LibraryScreen(viewModel: LibraryViewModel, onOpen: (String) -> Unit) {
    val state by viewModel.state.collectAsState()
    Scaffold(topBar = { KurusTopBar(stringResource(R.string.library_title)) }) { padding ->
        ListBody(state, padding, viewModel::load) { cat ->
            ArtRow(
                title = cat.title,
                body = cat.description,
                art = localArt(cat.id),
                artUrl = cat.imageUrl,
                artWidth = 84.dp,
                onClick = { onOpen(cat.id) }
            )
        }
    }
}

@Composable
fun CategoryScreen(viewModel: CategoryViewModel, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val state by viewModel.state.collectAsState()
    Scaffold(topBar = { KurusTopBar(state.title, onBack = onBack) }) { padding ->
        ListBody(state, padding, viewModel::load) { item ->
            ArtRow(
                title = item.title,
                body = item.summary,
                art = null,
                artUrl = item.imageUrl,
                artWidth = 72.dp,
                onClick = { onOpen(item.id) }
            )
        }
    }
}

@Composable
private fun <T> ListBody(
    state: ListState<T>,
    padding: PaddingValues,
    onRetry: () -> Unit,
    row: @Composable (T) -> Unit
) {
    val modifier = Modifier.fillMaxSize().padding(padding)
    when {
        state.loading -> LoadingState(modifier)
        state.error != null -> ErrorState(state.error, onRetry, modifier)
        else -> LazyColumn(modifier, contentPadding = PaddingValues(bottom = 24.dp)) {
            items(state.items) { item ->
                row(item)
                TrimRule(Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

/** Portrait art beside a title and a line or two of description. */
@Composable
private fun ArtRow(title: String, body: String?, art: Int?, artUrl: String, artWidth: Dp, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val artModifier = Modifier.size(artWidth, artWidth * 1.3f).clip(MaterialTheme.shapes.small)
        if (art != null) {
            androidx.compose.foundation.Image(
                painterResource(art), contentDescription = null, contentScale = ContentScale.Crop, modifier = artModifier
            )
        } else {
            AsyncImage(
                model = artUrl, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = artModifier,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHigh),
                error = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (!body.isNullOrBlank()) {
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
