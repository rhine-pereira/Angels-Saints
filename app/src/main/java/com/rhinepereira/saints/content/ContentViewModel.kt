package com.rhinepereira.saints.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhinepereira.saints.data.model.UiError
import com.rhinepereira.saints.data.remote.ContentResponse
import com.rhinepereira.saints.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ContentState(
    val loading: Boolean = true,
    val content: ContentResponse? = null,
    val error: UiError? = null
)

class ContentViewModel(
    private val repository: ContentRepository,
    val categoryId: String,
    private val contentId: String
) : ViewModel() {

    private val _state = MutableStateFlow(ContentState())
    val state: StateFlow<ContentState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = ContentState()
        viewModelScope.launch {
            repository.getContent(contentId)
                .catch { _state.value = ContentState(loading = false, error = UiError.from(it)) }
                .collect { _state.value = ContentState(loading = false, content = it) }
        }
    }
}
