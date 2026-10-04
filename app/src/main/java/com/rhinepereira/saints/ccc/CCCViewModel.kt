package com.rhinepereira.saints.ccc

import android.content.res.AssetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhinepereira.saints.data.model.UiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class CCCState(
    val loading: Boolean = true,
    val entries: List<CCCEntry> = emptyList(),
    val query: String = "",
    val filtered: List<CCCEntry> = emptyList(),
    val error: UiError? = null
)

class CCCViewModel(private val assets: AssetManager) : ViewModel() {

    private val _state = MutableStateFlow(CCCState())
    val state: StateFlow<CCCState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = CCCState()
        viewModelScope.launch {
            try {
                val entries = withContext(Dispatchers.IO) {
                    val arr = JSONArray(assets.open("ccc.json").bufferedReader().use { it.readText() })
                    List(arr.length()) { i -> arr.getJSONObject(i).run { CCCEntry(getInt("id"), getString("text")) } }
                }
                _state.value = CCCState(loading = false, entries = entries, filtered = entries)
            } catch (e: Exception) {
                _state.value = CCCState(loading = false, error = UiError.Unknown(e.message))
            }
        }
    }

    fun search(query: String) {
        val q = query.trim()
        _state.update { s ->
            s.copy(
                query = query,
                filtered = when {
                    q.isEmpty() -> s.entries
                    q.all(Char::isDigit) -> s.entries.filter { it.id.toString().startsWith(q) }
                    else -> s.entries.filter { it.text.contains(q, ignoreCase = true) }
                }
            )
        }
    }
}
